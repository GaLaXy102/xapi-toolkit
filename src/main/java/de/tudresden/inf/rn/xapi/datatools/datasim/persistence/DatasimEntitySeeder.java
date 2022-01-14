package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Logger;

@Component
@Profile("dev")
public class DatasimEntitySeeder {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DatasimProfileRepository profileRepository;
    private final DatasimPersonaRepository personaRepository;
    private final DatasimSimulationRepository simulationRepository;

    public DatasimEntitySeeder(DatasimSimulationRepository simulationRepository, DatasimProfileRepository profileRepository, DatasimPersonaRepository personaRepository) {
        this.simulationRepository = simulationRepository;
        this.profileRepository = profileRepository;
        this.personaRepository = personaRepository;
        this.seed();
    }

    private void seed() {
        DatasimProfile sampleProfile = new DatasimProfile("ya-cmi5", "ya-cmi5.json");
        this.profileRepository.save(sampleProfile);
        Set<DatasimPersona> samplePersonae = this.createSamplePersonae();
        this.personaRepository.saveAll(samplePersonae);
        DatasimPersonaGroup sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        Random random = new Random();
        DatasimSimulationParams sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        DatasimSimulation simulation = this.createSampleSimulation(sampleGroup, sampleProfile, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
        sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        simulation = this.createSampleSimulation(sampleGroup, sampleProfile, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
        sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        simulation = this.createSampleSimulation(sampleGroup, sampleProfile, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
        sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        simulation = this.createSampleSimulation(sampleGroup, sampleProfile, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
        sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        simulation = this.createSampleSimulation(sampleGroup, sampleProfile, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
    }

    private Set<DatasimPersona> createSamplePersonae() {
        return Set.of(
                new DatasimPersona("Sample Persona 1", "mail1@example.org"),
                new DatasimPersona("Sample Persona 2", "mail2@example.org"),
                new DatasimPersona("Sample Persona 3", "mail3@example.org"),
                new DatasimPersona("Sample Persona 4", "mail4@example.org")
        );
    }

    private DatasimSimulation createSampleSimulation(DatasimPersonaGroup group, DatasimProfile profile, DatasimSimulationParams params) {
        return new DatasimSimulation(
                "TestSim",
                new HashSet<>(Set.of(group)),
                new HashMap<>(),
                params,
                profile
        );
    }
}
