package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
@Profile("dev")
public class DatasimEntitySeeder {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DatasimPersonaRepository personaRepository;

    public DatasimEntitySeeder(DatasimPersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
        this.seed();
    }

    private void seed() {
        this.logger.info("Seeding personae");
        this.personaRepository.saveAll(this.createSamplePersonae());
    }

    private Set<DatasimPersona> createSamplePersonae() {
        return Set.of(
                new DatasimPersona("Sample Persona 1", "mail1@example.org"),
                new DatasimPersona("Sample Persona 2", "mail2@example.org"),
                new DatasimPersona("Sample Persona 3", "mail3@example.org"),
                new DatasimPersona("Sample Persona 4", "mail4@example.org")
        );
    }
}
