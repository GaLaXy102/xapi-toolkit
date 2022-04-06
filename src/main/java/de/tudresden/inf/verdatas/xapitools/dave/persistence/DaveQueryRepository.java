package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository to persist {@link DaveQuery}s
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
public interface DaveQueryRepository extends MongoRepository<DaveQuery, UUID> {
    /**
     * Find a Query by its name
     *
     * @param name of the Query to find
     * @return {@link Optional} which contains the Query, if it could be found
     */
    Optional<DaveQuery> findByName(String name);
}
