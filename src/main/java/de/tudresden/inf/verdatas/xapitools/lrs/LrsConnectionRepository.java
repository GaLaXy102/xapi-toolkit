package de.tudresden.inf.verdatas.xapitools.lrs;

import de.tudresden.inf.verdatas.xapitools.lrs.validators.Active;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

/**
 * Repository for {@link LrsConnection} entity
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface LrsConnectionRepository extends CrudRepository<LrsConnection, UUID> {
    /**
     * Find all {@link LrsConnection}s.
     */
    @Override
    @NonNull
    Streamable<LrsConnection> findAll();

    /**
     * Find all {@link Active} {@link LrsConnection}s.
     */
    Streamable<LrsConnection> findByEnabledIsTrue();
}