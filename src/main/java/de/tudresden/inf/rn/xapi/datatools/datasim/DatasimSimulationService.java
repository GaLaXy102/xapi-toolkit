package de.tudresden.inf.rn.xapi.datatools.datasim;

import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.*;
import de.tudresden.inf.rn.xapi.datatools.datasim.validators.AlignmentWeight;
import de.tudresden.inf.rn.xapi.datatools.datasim.validators.NonFinalized;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This service handles all Entity-related concerns to parameterize a Datasim Simulation
 */
@Service
@Validated  // Enable Validation on all methods
public class DatasimSimulationService {
    private final DatasimSimulationRepository simulationRepository;
    private final DatasimProfileRepository profileRepository;
    private final DatasimPersonaRepository personaRepository;
    private final DatasimAlignmentRepository alignmentRepository;

    /**
     * Instantiate the Service. This is done automagically by Spring through the @Service annotation.
     */
    public DatasimSimulationService(DatasimSimulationRepository simulationRepository, DatasimProfileRepository profileRepository,
                                    DatasimPersonaRepository personaRepository, DatasimAlignmentRepository alignmentRepository) {
        this.simulationRepository = simulationRepository;
        this.profileRepository = profileRepository;
        this.personaRepository = personaRepository;
        this.alignmentRepository = alignmentRepository;
    }

    /**
     * Get all xAPI-Profiles known to the system.
     * They are seeded by {@link DatasimEntitySeeder} in dev-Mode or {@link DatasimProfileSeeder} in production.
     */
    public Streamable<DatasimProfile> getProfiles() {
        return this.profileRepository.findAll();
    }

    /**
     * Get an xAPI-Profile by its ID.
     * @throws IllegalArgumentException when the ID is not known to the system.
     */
    public DatasimProfile getProfile(UUID profileId) {
        return this.profileRepository.findById(profileId).orElseThrow(() -> new IllegalArgumentException("No such profile."));
    }

    /**
     * Get all Simulation descriptions saved in the System.
     */
    public Stream<DatasimSimulation> getAllSimulations() {
        return this.simulationRepository.findAll().stream();
    }

    /**
     * Get a Simulation description by its ID.
     * @throws IllegalArgumentException when the ID is not known to the system.
     */
    public DatasimSimulation getSimulation(UUID simulationId) {
        return this.simulationRepository.findById(simulationId).orElseThrow(() -> new IllegalArgumentException("No such simulation."));
    }

    /**
     * Get a Simulation description by its ID ensuring it hasn't been marked as finalized.
     * @throws IllegalArgumentException when the ID is not known to the system.
     * @throws IllegalStateException when the Simulation description exists, but it is marked as finalized.
     */
    @NonFinalized
    public DatasimSimulation getUnfinalizedSimulation(UUID simulationId) {
        DatasimSimulation found = this.getSimulation(simulationId);
        if (found.isFinalized()) {
            throw new IllegalStateException("The given Simulation has been finalized.");
        }
        return found;
    }

    /**
     * Get a Persona by its ID.
     * @throws IllegalArgumentException when the ID is not known to the system.
     */
    public DatasimPersona getPersona(UUID personaId) {
        return this.personaRepository.findById(personaId).orElseThrow(() -> new IllegalArgumentException("No such persona."));
    }

    /**
     * Get all Personae known to the system with a flag stating whether they are participating in the given Simulation description.
     */
    public Map<DatasimPersonaTO, Boolean> getPersonasWithSelected(DatasimSimulation simulation) {
        Map<DatasimPersonaTO, Boolean> personaSelects = new HashMap<>();
        this.personaRepository.findAll()
                .forEach(
                        (persona) ->
                                personaSelects.put(
                                        DatasimPersonaTO.of(persona),
                                        simulation.getPersonaGroups().stream().anyMatch((group) -> group.getMember().contains(persona)))
                );
        return personaSelects;
    }

    /**
     * Extract and Group alignment data by the component URL.
     * You will receive a Set of Tuples mapping Personae to their selected weights, which is again mapped to the referenced component.
     */
    public static Map<URL, Set<Pair<DatasimPersona, Float>>> getComponentAlignsByUrl(Map<DatasimAlignment, DatasimPersona> alignments) {
        Map<URL, Set<Pair<DatasimPersona, Float>>> out = new HashMap<>();
        alignments.forEach(
                (align, value) -> {
                    Set<Pair<DatasimPersona, Float>> pairs = out.getOrDefault(align.getComponent(), new HashSet<>());
                    pairs.add(Pair.of(value, align.getWeight()));
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
     * @throws IllegalStateException when there is no such alignment
     */
    public static DatasimAlignment getAlignment(DatasimSimulation simulation, URL componentUrl, DatasimPersona persona) {
        return simulation.getAlignments().entrySet().stream()
                .filter((entry) -> entry.getKey().getComponent().equals(componentUrl))
                .filter((entry) -> entry.getValue().equals(persona))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
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
        simulation.setProfile(profile);
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
        // For now, it is enough to have one Group of Personae
        DatasimPersonaGroup group = simulation.getPersonaGroups().stream().findFirst().orElseThrow(RuntimeException::new);
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
        DatasimPersonaGroup group = simulation.getPersonaGroups().stream().findFirst().orElseThrow(RuntimeException::new);
        group.setMember(personae);
        // Delete for deselected personae
        simulation.getAlignments().entrySet().removeIf((entry) -> !personae.contains(entry.getValue()));
        // Create neutral alignments for all components for all selected personae if there is no alignment
        Map<URL, Set<Pair<DatasimPersona, Float>>> componentAligns = DatasimSimulationService.getComponentAlignsByUrl(simulation.getAlignments());
        Map<URL, Set<DatasimPersona>> componentPersonae = componentAligns.entrySet().stream()
                .map((entry) ->
                        Pair.of(
                                entry.getKey(),
                                entry.getValue().stream().map(Pair::getFirst).collect(Collectors.toSet()))
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
                simulation.getProfile());
        this.simulationRepository.save(copy);
        return copy;
    }
}
