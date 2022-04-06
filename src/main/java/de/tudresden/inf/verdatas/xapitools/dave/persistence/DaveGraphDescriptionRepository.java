package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository to persist {@link DaveGraphDescription}s
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
public interface DaveGraphDescriptionRepository extends MongoRepository<DaveGraphDescription, UUID> {
    /**
     * Find a Graph description by its name
     *
     * @param name of the Graph description to find
     * @return {@link Optional} which contains the Graph description, if it could be found
     */
    Optional<DaveGraphDescription> findByName(String name);
}
