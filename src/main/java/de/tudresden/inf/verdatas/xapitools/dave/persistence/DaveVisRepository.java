package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository to persist {@link DaveVis}s (Analyses)
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
public interface DaveVisRepository extends MongoRepository<DaveVis, UUID> {
    /**
     * Find an Analysis by its name
     *
     * @param name of the Analysis to find
     * @return {@link Optional} which contains the Analysis, if it could be found
     */
    Optional<DaveVis> findByName(String name);

    /**
     * Find all finalized Analyses descriptions
     *
     * @return {@link List} of found Analyses
     */
    List<DaveVis> findAllByFinalizedIsTrue();
}
