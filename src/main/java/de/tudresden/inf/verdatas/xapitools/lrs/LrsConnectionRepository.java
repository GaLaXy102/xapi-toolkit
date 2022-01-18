package de.tudresden.inf.verdatas.xapitools.lrs;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

/**
 * Repository for {@link LrsConnection} entity
 */
public interface LrsConnectionRepository extends CrudRepository<LrsConnection, UUID> {
    @Override
    @NonNull
    Streamable<LrsConnection> findAll();

    Streamable<LrsConnection> findByEnabledIsTrue();
}