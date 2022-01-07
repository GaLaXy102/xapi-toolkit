package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.util.Streamable;

import java.util.UUID;

public interface DatasimSimulationRepository extends CrudRepository<DatasimSimulation, UUID> {
    @Override
    Streamable<DatasimSimulation> findAll();
}
