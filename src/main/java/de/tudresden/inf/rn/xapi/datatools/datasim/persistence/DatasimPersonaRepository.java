package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

public interface DatasimPersonaRepository extends CrudRepository<DatasimPersona, UUID> {
    @Override
    Streamable<DatasimPersona> findAll();
}
