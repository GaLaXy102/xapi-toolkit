package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.util.Streamable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DaveVisRepository extends MongoRepository<DaveVis, UUID> {

    Optional<DaveVis> findByName(String name);

    List<DaveVis> findAllByFinalizedIsTrue();

    Streamable<DaveVis> findAllBy(DaveQuery query);

    Streamable<DaveVis> findAllBy(DaveGraphDescription description);

    Streamable<DaveVis> findAllBy(Query mongoQuery);
}
