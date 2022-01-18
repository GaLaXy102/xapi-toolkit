package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

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
@Profile("!dev")
public class DatasimProfileSeeder {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final DatasimProfileRepository profileRepository;

    public DatasimProfileSeeder(DatasimProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        this.seed();
    }

    private void seed() {
        this.profileRepository.saveAll(this.findProfiles());
    }

    private Set<DatasimProfile> findProfiles() {
        File profileFolder;
        try {
            profileFolder = new ClassPathResource("xapi/profiles").getFile();
        } catch (IOException e) {
            this.logger.warning("Could not seed profiles. The Classpath folder is not there.");
            return Set.of();
        }
        return Arrays.stream(Objects.requireNonNull(
                        profileFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".json"))
                ))
                .map(File::getName)
                .map(name -> new DatasimProfile(name.replace(".json", ""), name))
                .peek(profile -> this.logger.info("Seeding profile "+ profile.getName()))
                .collect(Collectors.toSet());
    }
}
