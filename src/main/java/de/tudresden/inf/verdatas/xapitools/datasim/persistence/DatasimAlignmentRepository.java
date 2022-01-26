package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

/**
 * Repository to persist {@link DatasimAlignment}s
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface DatasimAlignmentRepository extends CrudRepository<DatasimAlignment, UUID> {
    /**
     * Find all Alignments
     *
     * @return Streamable of all Alignments in the system
     */
    @Override
    @NonNull
    Streamable<DatasimAlignment> findAll();
}
