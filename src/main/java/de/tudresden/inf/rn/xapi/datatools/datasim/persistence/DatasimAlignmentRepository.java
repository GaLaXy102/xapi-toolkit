package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

public interface DatasimAlignmentRepository extends CrudRepository<DatasimAlignment, UUID> {
    @Override
    Streamable<DatasimAlignment> findAll();
}
