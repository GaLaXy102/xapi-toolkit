package de.tudresden.inf.rn.xapi.datatools.datasim;

import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.*;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DatasimSimulationService {
    private final DatasimSimulationRepository simulationRepository;
    private final DatasimProfileRepository profileRepository;
    private final DatasimPersonaRepository personaRepository;

    public DatasimSimulationService(DatasimSimulationRepository simulationRepository, DatasimProfileRepository profileRepository,
                                    DatasimPersonaRepository personaRepository) {
        this.simulationRepository = simulationRepository;
        this.profileRepository = profileRepository;
        this.personaRepository = personaRepository;
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
        this.simulationRepository.save(simulation);
    }
}
