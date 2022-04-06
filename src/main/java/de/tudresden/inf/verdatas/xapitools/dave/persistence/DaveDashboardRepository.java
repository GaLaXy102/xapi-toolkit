package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository to persist {@link DaveDashboard}s
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
public interface DaveDashboardRepository extends MongoRepository<DaveDashboard, UUID> {
    /**
     * Find a Dashboard by its name
     *
     * @param name of the Dashboard to find
     * @return {@link Optional} which contains the Dashboard, if it could be found
     */
    Optional<DaveDashboard> findByName(String name);

    /**
     * Find all finalized Dashboard descriptions
     *
     * @return {@link List} of found Dashboards
     */
    List<DaveDashboard> findAllByFinalizedIsTrue();
}
