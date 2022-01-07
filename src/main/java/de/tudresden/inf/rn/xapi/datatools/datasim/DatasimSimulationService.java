package de.tudresden.inf.rn.xapi.datatools.datasim;

import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimProfile;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimProfileRepository;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulationRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DatasimSimulationService {
    private final DatasimSimulationRepository simulationRepository;
    private final DatasimProfileRepository profileRepository;

    public DatasimSimulationService(DatasimSimulationRepository simulationRepository, DatasimProfileRepository profileRepository) {
        this.simulationRepository = simulationRepository;
        this.profileRepository = profileRepository;
    }

    public Streamable<DatasimProfile> getProfiles() {
        return this.profileRepository.findAll();
    }

    public DatasimProfile getProfile(UUID profileId) {
        return this.profileRepository.findById(profileId).orElseThrow(() -> new IllegalArgumentException("No such profile."));
    }

    public DatasimSimulation getSimulation(UUID simulationId) {
        return this.simulationRepository.findById(simulationId).orElseThrow(() -> new IllegalArgumentException("No such simulation."));
    }

    @Transactional
    public DatasimSimulation createEmptySimulation() {
        DatasimSimulation created = new DatasimSimulation("", Set.of(), Map.of(), null, null);
        this.simulationRepository.save(created);
        return created;
    }

    @Transactional
    public void updateSimulationProfile(DatasimSimulation simulation, DatasimProfile profile) {
        simulation.setProfile(profile);
        this.simulationRepository.save(simulation);
    }
}
