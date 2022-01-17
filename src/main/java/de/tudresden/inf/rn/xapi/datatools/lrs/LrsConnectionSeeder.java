package de.tudresden.inf.rn.xapi.datatools.lrs;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * Seeder for LRS connections
 *
 * Is run only in dev mode.
 */
@Component
@Profile("dev")
public class LrsConnectionSeeder {
    private final LrsConnectionRepository lrsConnectionRepository;

    /**
     * This class is instantiated by Spring Boot and not intended for manual creation.
     */
    private LrsConnectionSeeder(LrsConnectionRepository lrsConnectionRepository) {
        this.lrsConnectionRepository = lrsConnectionRepository;
        this.seed();
    }

    /**
     * Create some connection samples
     */
    private void seed() {
        try {
            if (this.lrsConnectionRepository.count() == 0) {
                this.lrsConnectionRepository.save(
                        new LrsConnectionTO(Optional.empty(), "Sample Connection 1", new URL("https://my.xapi/push"), "foo", "bar", Optional.empty()).toNewLrsConnection()
                );
                this.lrsConnectionRepository.save(
                        new LrsConnectionTO(Optional.empty(), "Sample Connection 2", new URL("https://my.lrs/push"), "key", "secret", Optional.empty()).toNewLrsConnection()
                );
                this.lrsConnectionRepository.save(
                        new LrsConnectionTO(Optional.empty(), "Galaxion LRS", new URL("https://ba-lrs.galaxion.de/data/xAPI"), "28ec3f7855e5bd320f2b45483c5c3c10c5b45a4b", "af210bb4f29f3d9fb8b37c41f5e5ac2d4df90909", Optional.empty()).toNewLrsConnection()
                );
            }
        } catch (MalformedURLException ignored) {}
    }
}
