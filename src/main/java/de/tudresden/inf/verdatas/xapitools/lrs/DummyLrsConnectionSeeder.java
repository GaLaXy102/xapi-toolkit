package de.tudresden.inf.verdatas.xapitools.lrs;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Dummy Seeder for LRS connections.
 * This is needed because {@link LrsService} depends on a Bean with this name, such that seeding happens before instantiation.
 * <p>
 * Is run only when not in dev mode.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Component("lrsConnectionSeeder")
@Profile("!dev")
public class DummyLrsConnectionSeeder {

}
