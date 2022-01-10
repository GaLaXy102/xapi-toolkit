package de.tudresden.inf.rn.xapi.datatools.datasim;

import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.*;
import org.springframework.data.util.Pair;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatasimSimulationService {
    private final DatasimSimulationRepository simulationRepository;
    private final DatasimProfileRepository profileRepository;
    private final DatasimPersonaRepository personaRepository;
    private final DatasimAlignmentRepository alignmentRepository;

    public DatasimSimulationService(DatasimSimulationRepository simulationRepository, DatasimProfileRepository profileRepository,
                                    DatasimPersonaRepository personaRepository, DatasimAlignmentRepository alignmentRepository) {
        this.simulationRepository = simulationRepository;
        this.profileRepository = profileRepository;
        this.personaRepository = personaRepository;
        this.alignmentRepository = alignmentRepository;
    }

    public Streamable<DatasimProfile> getProfiles() {
        return this.profileRepository.findAll();
    }

    public DatasimProfile getProfile(UUID profileId) {
        return this.profileRepository.findById(profileId).orElseThrow(() -> new IllegalArgumentException("No such profile."));
    }

    public DatasimSimulation getSimulation(UUID simulationId) {
        return this.simulationRepository.findById(simulationId).orElseThrow(() -> new IllegalArgumentException("No such simulation."));
    }

    public DatasimPersona getPersona(UUID personaId) {
        return this.personaRepository.findById(personaId).orElseThrow(() -> new IllegalArgumentException("No such persona."));
    }

    @Transactional
    public DatasimSimulation createEmptySimulation() {
        DatasimPersonaGroup personaGroup = DatasimPersonaGroupTO.empty("Default Group").toNewPersonaGroup();
        DatasimSimulation created = new DatasimSimulation("", new HashSet<>(Set.of(personaGroup)), new HashMap<>(), null, null);
        this.simulationRepository.save(created);
        return created;
    }

    @Transactional
    public void updateSimulationProfile(DatasimSimulation simulation, DatasimProfile profile) {
        simulation.setProfile(profile);
        this.simulationRepository.save(simulation);
    }

    @Transactional
    public void setSimulationRemark(DatasimSimulation simulation, String remark) {
        simulation.setRemark(remark);
        this.simulationRepository.save(simulation);
    }

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

    @Transactional
    public DatasimPersona createPersona(DatasimPersonaTO persona) {
        return this.personaRepository.save(persona.toNewDatasimPersona());
    }

    @Transactional
    public void addPersonaToSimulation(DatasimSimulation simulation, DatasimPersona persona) {
        DatasimPersonaGroup group = simulation.getPersonaGroups().stream().findFirst().orElseThrow(RuntimeException::new);
        group.getMember().add(persona);
        this.simulationRepository.save(simulation);
    }

    @Transactional
    public void setPersonaeOfSimulation(DatasimSimulation simulation, Set<DatasimPersona> personae) {
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

    @Transactional
    public void addComponentToSimulationWithNeutralWeight(DatasimSimulation simulation, URL componentUrl) {
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
}
