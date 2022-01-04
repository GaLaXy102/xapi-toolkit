package de.tudresden.inf.rn.xapi.datatools.lrs;

import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Component
public class LrsConnectionSeeder {
    private final LrsConnectionRepository lrsConnectionRepository;

    public LrsConnectionSeeder(LrsConnectionRepository lrsConnectionRepository) {
        this.lrsConnectionRepository = lrsConnectionRepository;
        try {
            this.seed();
        } catch (MalformedURLException ignored) {}
    }

    private void seed() throws MalformedURLException {
        if (this.lrsConnectionRepository.count() == 0) {
            this.lrsConnectionRepository.save(
                    new LrsConnectionTO(Optional.empty(), "Sample Connection 1", new URL("https://my.xapi/push"), "foo", "bar", Optional.empty()).toNewLrsConnection()
            );
            this.lrsConnectionRepository.save(
                    new LrsConnectionTO(Optional.empty(), "Sample Connection 2", new URL("https://my.lrs/push"), "key", "secret", Optional.empty()).toNewLrsConnection()
            );
        }
    }
}
