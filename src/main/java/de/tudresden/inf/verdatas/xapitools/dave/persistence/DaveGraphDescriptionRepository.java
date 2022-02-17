package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.util.Streamable;

import java.util.Optional;
import java.util.UUID;

public interface DaveGraphDescriptionRepository extends MongoRepository<DaveGraphDescription, UUID> {

    Optional<DaveGraphDescription> findByIdentifier(String identifier);

    Streamable<DaveGraphDescription> findAllBy(Query mongoQuery);
}
