package de.tudresden.inf.verdatas.xapitools.datasim;

import de.tudresden.inf.verdatas.xapitools.datasim.persistence.*;
import de.tudresden.inf.verdatas.xapitools.datasim.validators.AlignmentWeight;
import de.tudresden.inf.verdatas.xapitools.datasim.validators.NonFinalized;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This service handles all Entity-related concerns to parameterize a Datasim Simulation.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Service
@Validated  // Enable Validation on all methods
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimSimulationService {
    private final DatasimSimulationRepository simulationRepository;
    private final DatasimProfileRepository profileRepository;
    private final DatasimPersonaRepository personaRepository;
    private final DatasimAlignmentRepository alignmentRepository;

    /**
     * Get all xAPI-Profiles known to the system.
     * They are seeded by {@link DatasimEntitySeeder} in dev-Mode or {@link DatasimProfileSeeder} in production.
     */
    public Stream<DatasimProfile> getProfiles() {
        return this.profileRepository.findAll().stream().sorted(Comparator.comparing(DatasimProfile::getName));
    }

    /**
     * Get an xAPI-Profile by its ID.
     *
     * @throws IllegalArgumentException when the ID is not known to the system.
     */
    public DatasimProfile getProfile(UUID profileId) {
        return this.profileRepository.findById(profileId).orElseThrow(() -> new NoSuchElementException("No such profile."));
    }

    /**
     * Get all Simulation descriptions saved in the System.
     */
    public Stream<DatasimSimulation> getAllSimulations() {
        return this.simulationRepository.findAll().stream();
    }

    /**
     * Get a Simulation description by its ID.
     *
     * @throws IllegalArgumentException when the ID is not known to the system.
     */
    public DatasimSimulation getSimulation(UUID simulationId) {
        return this.simulationRepository.findById(simulationId).orElseThrow(() -> new NoSuchElementException("No such simulation."));
    }

    /**
     * Get a Simulation description by its ID ensuring it hasn't been marked as finalized.
     *
     * @throws IllegalArgumentException when the ID is not known to the system.
     */
    @NonFinalized
    public DatasimSimulation getUnfinalizedSimulation(UUID simulationId) {
        // Validation is done by annotation
        return this.getSimulation(simulationId);
    }

    /**
     * Get a Persona by its ID.
     *
     * @throws IllegalArgumentException when the ID is not known to the system.
     */
    public DatasimPersona getPersona(UUID personaId) {
        return this.personaRepository.findById(personaId).orElseThrow(() -> new NoSuchElementException("No such persona."));
    }

    /**
     * Get all Personae known to the system with a flag stating whether they are participating in the given Simulation description.
     */
    public Map<DatasimPersona, Boolean> getPersonasWithSelected(DatasimSimulation simulation) {
        Map<DatasimPersona, Boolean> personaSelects = new TreeMap<>();
        this.personaRepository.findAll()
                .forEach(
                        (persona) ->
                                personaSelects.put(
                                        persona,
                                        simulation.getPersonaGroups().stream().anyMatch((group) -> group.getMember().contains(persona)))
                );
        return personaSelects;
    }

    /**
     * Extract and Group alignment data by the component URL.
     * You will receive a Set of Tuples mapping Personae to their selected weights, which is again mapped to the referenced component.
     */
    public static Map<URL, Map<DatasimPersona, Float>> getComponentAlignsByUrl(Map<DatasimAlignment, DatasimPersona> alignments) {
        Map<URL, Map<DatasimPersona, Float>> out = new TreeMap<>(Comparator.comparing(URL::toString));
        alignments.forEach(
                (align, value) -> {
                    Map<DatasimPersona, Float> pairs = out.getOrDefault(align.getComponent(), new TreeMap<>());
                    pairs.put(value, align.getWeight());
                    out.put(
                            align.getComponent(),
                            pairs
                    );
                }
        );
        return out;
    }

    /**
     * Retrieve an alignment from a Simulation description using its component and Persona as keys
     *
     * @throws NoSuchElementException when there is no such alignment
     */
    public static DatasimAlignment getAlignment(DatasimSimulation simulation, URL componentUrl, DatasimPersona persona) {
        return simulation.getAlignments().entrySet().stream()
                .filter((entry) -> entry.getKey().getComponent().equals(componentUrl))
                .filter((entry) -> entry.getValue().equals(persona))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No such alignment."));
    }


    /**
     * Create a Simulation description skeleton.
     * The created entity has nothing attached to it and its parameters are chosen to have somewhat reasonable values.
     */
    @Transactional
    @NonFinalized
    public DatasimSimulation createEmptySimulation() {
        DatasimPersonaGroup personaGroup = DatasimPersonaGroupTO.empty("Default Group").toNewPersonaGroup();
        DatasimSimulation created = new DatasimSimulation("", new HashSet<>(Set.of(personaGroup)), new HashMap<>(), DatasimSimulationParamsTO.empty().toNewSimulationParams(), null);
        this.simulationRepository.save(created);
        return created;
    }

    /**
     * Set and persist the remark of the Simulation description.
     */
    @Transactional
    public void setSimulationRemark(@NonFinalized DatasimSimulation simulation, String remark) {
        simulation.setRemark(remark);
        this.simulationRepository.save(simulation);
    }

    /**
     * Set and persist the xAPI profile of the Simulation description.
     */
    @Transactional
    public void updateSimulationProfile(@NonFinalized DatasimSimulation simulation, DatasimProfile profile) {
        simulation.setProfiles(new LinkedList<>(List.of(profile)));
        this.simulationRepository.save(simulation);
    }

    /**
     * Persist a Persona. You would probably use {@link DatasimPersonaTO#toNewDatasimPersona()} to create the object inserted.
     */
    @Transactional
    public DatasimPersona createPersona(DatasimPersona persona) {
        return this.personaRepository.save(persona);
    }

    /**
     * Add a persona to the Simulation description and create default alignments.
     * You need to persist the Persona object first using {@link #createPersona(DatasimPersona)}.
     */
    @Transactional
    public void addPersonaToSimulation(@NonFinalized DatasimSimulation simulation, DatasimPersona persona) {
        // For now, it is enough to have one Group of Personae. The Group must exist.
        DatasimPersonaGroup group = simulation.getPersonaGroups().stream().findFirst().orElseThrow(IllegalStateException::new);
        group.getMember().add(persona);
        // Create default alignments
        DatasimSimulationService.getComponentAlignsByUrl(simulation.getAlignments()).keySet().forEach(
                (url) -> {
                    DatasimAlignment created = DatasimAlignmentTO.neutral(url).toNewDatasimAlignment();
                    this.alignmentRepository.save(created);
                    simulation.getAlignments().put(created, persona);
                }
        );
        this.simulationRepository.save(simulation);
    }

    /**
     * Add or Delete personae to/from the Simulation description and create/delete default alignments.
     * You need to persist the Persona object first using {@link #createPersona(DatasimPersona)} if this has not happened before.
     */
    @Transactional
    public void setPersonaeOfSimulation(@NonFinalized DatasimSimulation simulation, Set<DatasimPersona> personae) {
        // For now, it is enough to have one Group of Personae. The Group must exist.
        DatasimPersonaGroup group = simulation.getPersonaGroups().stream().findFirst().orElseThrow(IllegalStateException::new);
        group.setMember(personae);
        // Delete for deselected personae
        simulation.getAlignments().entrySet().removeIf((entry) -> !personae.contains(entry.getValue()));
        // Create neutral alignments for all components for all selected personae if there is no alignment
        Map<URL, Map<DatasimPersona, Float>> componentAligns = DatasimSimulationService.getComponentAlignsByUrl(simulation.getAlignments());
        Map<URL, Set<DatasimPersona>> componentPersonae = componentAligns.entrySet().stream()
                .map((entry) ->
                        Pair.of(
                                entry.getKey(),
                                entry.getValue().keySet()
                        )
                ).collect(Pair.toMap());
        componentPersonae.forEach((key, value) -> {
                    // Create for newly selected personae
                    personae.forEach((persona) -> {
                        if (!value.contains(persona)) {
                            DatasimAlignment created = DatasimAlignmentTO.neutral(key).toNewDatasimAlignment();
                            this.alignmentRepository.save(created);
                            simulation.getAlignments().put(created, persona);
                        }
                    });
                }
        );
        this.simulationRepository.save(simulation);
    }

    /**
     * Add a component to this Simulation description, creating Alignments for all assigned Personae
     */
    @Transactional
    public void addComponentToSimulationWithNeutralWeight(@NonFinalized DatasimSimulation simulation, URL componentUrl) {
        simulation.getPersonaGroups().stream()
                .map(DatasimPersonaGroup::getMember)
                .flatMap(Collection::stream)
                .forEach((persona) -> {
                    DatasimAlignment created = DatasimAlignmentTO.neutral(componentUrl).toNewDatasimAlignment();
                    this.alignmentRepository.save(created);
                    simulation.getAlignments().put(created, persona);
                });
        simulation.setAlignments(new HashMap<>(simulation.getAlignments()));
        this.simulationRepository.save(simulation); // Cascades
    }

    /**
     * Remove a component from this Simulation description, deleting Alignments for this component
     */
    @Transactional
    public void removeComponentFromSimulation(@NonFinalized DatasimSimulation simulation, URL componentUrl) {
        Set<DatasimAlignment> toDelete = simulation.getAlignments().keySet().stream().filter((align) -> align.getComponent().equals(componentUrl)).collect(Collectors.toSet());
        simulation.getAlignments().entrySet().removeIf((entry) -> toDelete.contains(entry.getKey()));
        // Remove orphans
        this.simulationRepository.save(simulation);
        this.alignmentRepository.deleteAll(toDelete);
    }

    /**
     * Set and persist the weight of an alignment
     */
    @Transactional
    public void setAlignmentWeight(DatasimAlignment alignment, @AlignmentWeight Float weight) {
        alignment.setWeight(weight);
        this.alignmentRepository.save(alignment);
    }

    /**
     * Update and persist the Parameters of a simulation.
     * You probably want to create the parameters object using {@link DatasimSimulationParamsTO#toExistingSimulationParams()}.
     */
    @Transactional
    public void setSimulationParams(@NonFinalized DatasimSimulation simulation, DatasimSimulationParams params) {
        if (!simulation.getParameters().getId().equals(params.getId())) throw new IllegalArgumentException("Params do not match Simulation.");
        simulation.setParameters(params);
        this.simulationRepository.save(simulation);
    }

    /**
     * Mark a simulation description as finalized.
     */
    @Transactional
    public void finalizeSimulation(@NonFinalized DatasimSimulation simulation) {
        simulation.setFinalized(true);
        this.simulationRepository.save(simulation);
    }

    /**
     * Delete a simulation description from the system.
     * Retains the Personae and the Profile, but nothing else.
     */
    @Transactional
    public void deleteSimulation(DatasimSimulation simulation) {
        this.simulationRepository.delete(simulation);
    }

    /**
     * Create a fully standalone copy of a simulation description.
     * The only coupling points are xAPI profiles (mutable only in File System for now) and Personae (immutable by missing implementation /haha).
     */
    @Transactional
    @NonFinalized
    public DatasimSimulation copySimulation(DatasimSimulation simulation) {
        DatasimSimulationParamsTO copiedParams = DatasimSimulationParamsTO.of(simulation.getParameters());
        copiedParams.setId(Optional.empty());
        Set<DatasimPersonaGroup> copiedPersonaGroups = simulation
                .getPersonaGroups()
                .stream()
                .map(DatasimPersonaGroupTO::of)
                .peek((group) -> group.setId(Optional.empty()))
                .map(DatasimPersonaGroupTO::toNewPersonaGroup)
                .collect(Collectors.toSet());
        Map<DatasimAlignment, DatasimPersona> copiedAlignments = new HashMap<>(
                simulation
                        .getAlignments()
                        .entrySet()
                        .stream()
                        .map((entry) -> Pair.of(DatasimAlignmentTO.of(entry.getKey()), entry.getValue()))
                        .peek((pair) -> pair.getFirst().setId(Optional.empty()))
                        .map((pair) -> Pair.of(pair.getFirst().toNewDatasimAlignment(), pair.getSecond()))
                        .peek((pair) -> this.alignmentRepository.save(pair.getFirst()))
                        .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
        );
        DatasimSimulation copy = new DatasimSimulation(
                "Copy of " + simulation.getRemark(),
                copiedPersonaGroups,
                copiedAlignments,
                copiedParams.toNewSimulationParams(),
                new LinkedList<>(simulation.getProfiles())); // Decouple
        this.simulationRepository.save(copy);
        return copy;
    }
}
