package de.tudresden.inf.rn.xapi.datatools.lrs;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

public interface LrsConnectionRepository extends CrudRepository<LrsConnection, UUID> {
    @Override
    @NonNull
    Streamable<LrsConnection> findAll();

    Streamable<LrsConnection> findByEnabledIsTrue();
}