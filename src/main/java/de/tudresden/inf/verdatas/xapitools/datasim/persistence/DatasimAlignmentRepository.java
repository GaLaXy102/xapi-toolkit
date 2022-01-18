package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

public interface DatasimAlignmentRepository extends CrudRepository<DatasimAlignment, UUID> {
    @Override
    @NonNull
    Streamable<DatasimAlignment> findAll();
}
