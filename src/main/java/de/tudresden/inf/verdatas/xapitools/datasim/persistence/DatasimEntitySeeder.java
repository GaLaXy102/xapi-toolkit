package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
@DependsOn("datasimProfileSeeder")
@Profile("dev")
public class DatasimEntitySeeder {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DatasimProfileRepository profileRepository;
    private final DatasimPersonaRepository personaRepository;
    private final DatasimAlignmentRepository alignmentRepository;
    private final DatasimSimulationRepository simulationRepository;

    public DatasimEntitySeeder(DatasimSimulationRepository simulationRepository, DatasimProfileRepository profileRepository,
                               DatasimPersonaRepository personaRepository, DatasimAlignmentRepository alignmentRepository) {
        this.simulationRepository = simulationRepository;
        this.profileRepository = profileRepository;
        this.personaRepository = personaRepository;
        this.alignmentRepository = alignmentRepository;
        this.seed();
    }

    private void seed() {
        List<DatasimProfile> sampleProfiles = new LinkedList<>(
                this.profileRepository.findAll().stream().min(Comparator.comparing(DatasimProfile::getName)).stream().toList()
        );
        this.profileRepository.saveAll(sampleProfiles);
        Set<DatasimPersona> samplePersonae = this.createSamplePersonae();
        this.personaRepository.saveAll(samplePersonae);
        DatasimPersonaGroup sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        Random random = new Random();
        DatasimSimulationParams sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        DatasimSimulation simulation = this.createSampleSimulation(sampleGroup, sampleProfiles, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
        sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        simulation = this.createSampleSimulation(sampleGroup, sampleProfiles, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
        sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        simulation = this.createSampleSimulation(sampleGroup, sampleProfiles, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
        sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        simulation = this.createSampleSimulation(sampleGroup, sampleProfiles, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
        sampleGroup = new DatasimPersonaGroup("Default Group", samplePersonae);
        sampleParams = new DatasimSimulationParams(1000L, random.nextLong(5000L), LocalDateTime.now().atZone(ZoneId.systemDefault()), LocalDateTime.now().plusWeeks(1).atZone(ZoneId.systemDefault()));
        simulation = this.createSampleSimulation(sampleGroup, sampleProfiles, sampleParams);
        this.simulationRepository.save(simulation);
        this.logger.info("Sample simulation: http://localhost:8080/ui/datasim/show?flow=" + simulation.getId());
    }

    private Set<DatasimPersona> createSamplePersonae() {
        return Set.of(
                new DatasimPersona("Sample Persona 1", "mailto:mail1@example.org"),
                new DatasimPersona("Sample Persona 2", "mailto:mail2@example.org"),
                new DatasimPersona("Sample Persona 3", "mailto:mail3@example.org"),
                new DatasimPersona("Sample Persona 4", "mailto:mail4@example.org")
        );
    }

    private DatasimSimulation createSampleSimulation(DatasimPersonaGroup group, List<DatasimProfile> profile, DatasimSimulationParams params) {
        URL componentFail;
        URL componentPass;
        try {
            // This is taken directly from the Profile
            componentFail = new URL("https://w3id.org/xapi/cmi5#failed");
            componentPass = new URL("https://w3id.org/xapi/cmi5#passed");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not seed.");
        }
        return new DatasimSimulation(
                "TestSim",
                new HashSet<>(Set.of(group)),
                new HashMap<>(
                        group
                                .getMember()
                                .stream()
                                .map((persona) ->
                                        Set.of(
                                                // Insert evil smile here.
                                                Pair.of(new DatasimAlignment(componentFail, 1.0f), persona),
                                                Pair.of(new DatasimAlignment(componentPass, -1.0f), persona)
                                                )
                                )
                                .flatMap(Set::stream)
                                .peek((pair) -> this.alignmentRepository.save(pair.getFirst()))
                                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                ),
                params,
                profile
        );
    }
}
