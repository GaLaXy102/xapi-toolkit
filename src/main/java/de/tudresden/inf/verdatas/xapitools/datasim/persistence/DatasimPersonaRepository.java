package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

/**
 * Repository to persist {@link DatasimPersona}e
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface DatasimPersonaRepository extends CrudRepository<DatasimPersona, UUID> {
    /**
     * Find all Personae
     *
     * @return Streamable of all Personae in the system
     */
    @Override
    @NonNull
    Streamable<DatasimPersona> findAll();
}
