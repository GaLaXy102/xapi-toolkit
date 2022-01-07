package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

public interface DatasimProfileRepository extends CrudRepository<DatasimProfile, UUID> {
    @Override
    @NonNull
    Streamable<DatasimProfile> findAll();
}
