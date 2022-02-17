package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.util.Streamable;

import java.util.Optional;
import java.util.UUID;

public interface DaveQueryRepository extends MongoRepository<DaveQuery, UUID> {

    Optional<DaveQuery> findByIdentifier(String identifier);

    Streamable<DaveQuery> findAllBy(Query mongoQuery);
}
