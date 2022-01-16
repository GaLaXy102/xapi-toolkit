package de.tudresden.inf.rn.xapi.datatools.lrs;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LrsService {

    private final LrsConnectionRepository lrsConnectionRepository;

    /**
     * Create an LRS connection from a Transfer Object and save it
     */
    @Transactional
    LrsConnection createConnection(LrsConnectionTO lrsData) {
        LrsConnection created = lrsData.toNewLrsConnection();
        this.lrsConnectionRepository.save(created);
        return created;
    }

    /**
     * Deactivate an LRS connection by its UUID
     *
     * @throws IllegalArgumentException When there is no connection with such ID
     */
    @Transactional
    LrsConnection deactivateConnection(UUID connectionId) {
        LrsConnection connection = this.getConnection(connectionId);
        connection.setEnabled(false);
        this.lrsConnectionRepository.save(connection);
        return connection;
    }

    /**
     * Activate an LRS connection by its UUID
     *
     * @throws IllegalArgumentException When there is no connection with such ID
     */
    @Transactional
    LrsConnection activateConnection(UUID connectionId) {
        LrsConnection connection = this.getConnection(connectionId);
        connection.setEnabled(true);
        this.lrsConnectionRepository.save(connection);
        return connection;
    }

    /**
     * Get a list of saved LRS connections
     *
     * @param activeOnly When true, only enabled connections will be returned
     */
    public List<LrsConnection> getConnections(boolean activeOnly) {
        if (activeOnly) {
            return this.lrsConnectionRepository.findByEnabledIsTrue().toList();
        } else {
            return this.lrsConnectionRepository.findAll().toList();
        }
    }

    /**
     * Get a specific LRS connection entity by its UUID
     *
     * @throws IllegalArgumentException When there is no connection with such ID
     */
    LrsConnection getConnection(UUID connectionId) {
        return this.lrsConnectionRepository.findById(connectionId).orElseThrow(IllegalArgumentException::new);
    }

    /**
     * Update an LRS connection from a Transfer Object
     *
     * @throws IllegalArgumentException When there is no connection with the ID specified in the Transfer Object or the ID is not given.
     */
    @Transactional
    LrsConnection updateConnection(LrsConnectionTO lrsData) {
        LrsConnection found = this.getConnection(lrsData.getUuid().orElseThrow(IllegalArgumentException::new));
        found.setFriendlyName(lrsData.getName());
        found.setXApiEndpoint(lrsData.getEndpoint());
        found.setXApiClientKey(lrsData.getClientKey());
        found.setXApiClientSecret(lrsData.getClientSecret());
        found.setEnabled(lrsData.getEnabled().orElse(found.isEnabled()));
        this.lrsConnectionRepository.save(found);
        return found;
    }
}
