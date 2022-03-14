package de.tudresden.inf.verdatas.xapitools.lrs;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * Seeder for LRS connections
 * <p>
 * Is run only in dev mode.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Component
@Profile("dev")
public class LrsConnectionSeeder {
    private final LrsConnectionRepository lrsConnectionRepository;

    /**
     * This class is instantiated by Spring Boot and not intended for manual creation.
     */
    LrsConnectionSeeder(LrsConnectionRepository lrsConnectionRepository) {
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
                this.lrsConnectionRepository.save(
                        new LrsConnectionTO(Optional.empty(), "Dave-Test-LRS", new URL("https://ba-lrs.galaxion.de/data/xAPI"), "8bbb203add18ab964236346874db4f8d930c4eb8", "069e6b99c4c5cd31178a972ec25f769ea1c8ea0d", Optional.empty()).toNewLrsConnection()
                );
                this.lrsConnectionRepository.save(
                        new LrsConnectionTO(Optional.empty(), "VerDatAs-Test-LRS", new URL("https://ba-lrs.galaxion.de/data/xAPI"), "16df080479a1d0848be040760dad31a7f2973678", "81b1ad91cfe66a0dfe1e159d723682b81a02aeb4", Optional.empty()).toNewLrsConnection()
                );
            }
        } catch (MalformedURLException ignored) {
        }
    }
}
