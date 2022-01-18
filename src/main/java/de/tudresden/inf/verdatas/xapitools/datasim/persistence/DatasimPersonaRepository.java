package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

public interface DatasimPersonaRepository extends CrudRepository<DatasimPersona, UUID> {
    @Override
    @NonNull
    Streamable<DatasimPersona> findAll();
}
