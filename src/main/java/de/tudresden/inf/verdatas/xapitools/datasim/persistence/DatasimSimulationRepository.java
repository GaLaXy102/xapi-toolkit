package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

/**
 * Repository to persist {@link DatasimSimulationParams}
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface DatasimSimulationRepository extends CrudRepository<DatasimSimulation, UUID> {
    /**
     * Find all Parameter Sets.
     *
     * @return Streamable of all Parameter sets in the system
     */
    @Override
    @NonNull
    Streamable<DatasimSimulation> findAll();
}
