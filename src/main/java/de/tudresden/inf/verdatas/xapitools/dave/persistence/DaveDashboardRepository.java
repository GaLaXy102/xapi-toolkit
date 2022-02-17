package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.util.Streamable;

import java.util.Optional;
import java.util.UUID;

public interface DaveDashboardRepository extends MongoRepository<DaveDashboard, UUID> {
    Optional<DaveDashboard> findByIdentifier(String identifier);

    Streamable<DaveDashboard> findAllBy(Query mongoQuery);
}
