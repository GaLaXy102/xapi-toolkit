package de.tudresden.inf.verdatas.xapitools.lrs;

import com.fasterxml.jackson.databind.JsonNode;
import de.tudresden.inf.verdatas.xapitools.lrs.connector.LrsConnector;
import de.tudresden.inf.verdatas.xapitools.lrs.connector.LrsConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.lrs.validators.Active;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@DependsOn("lrsConnectionSeeder")
public class LrsService {

    private final LrsConnectionRepository lrsConnectionRepository;
    private final LrsConnectorLifecycleManager connectorLifecycleManager;

    public LrsService(LrsConnectionRepository lrsConnectionRepository, LrsConnectorLifecycleManager connectorLifecycleManager) {
        this.lrsConnectionRepository = lrsConnectionRepository;
        this.connectorLifecycleManager = connectorLifecycleManager;
        this.lrsConnectionRepository.findAll().stream().filter(LrsConnection::isEnabled).forEach(this.connectorLifecycleManager::createConnector);
    }

    /**
     * Create an LRS connection from a Transfer Object and save it
     */
    @Transactional
    LrsConnection createConnection(LrsConnectionTO lrsData) {
        LrsConnection created = lrsData.toNewLrsConnection();
        this.lrsConnectionRepository.save(created);
        this.connectorLifecycleManager.createConnector(created);
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
        this.connectorLifecycleManager.deleteConnector(connection);
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
        this.connectorLifecycleManager.createConnector(connection);
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
    public LrsConnection getConnection(UUID connectionId) {
        return this.lrsConnectionRepository.findById(connectionId).orElseThrow(() -> new IllegalArgumentException("No such LRS connection."));
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
        this.connectorLifecycleManager.deleteConnector(found);
        this.connectorLifecycleManager.createConnector(found);
        return found;
    }

    public List<UUID> sendStatements(List<JsonNode> statements, @Active LrsConnection connection) {
        LrsConnector connector = this.connectorLifecycleManager.getConnector(connection);
        return connector.sendStatements(statements);
    }

    public List<JsonNode> getStatements(@Active LrsConnection connection) {
        LrsConnector connector = this.connectorLifecycleManager.getConnector(connection);
        return connector.getStatements();
    }

    LrsConnector getConnector(LrsConnection connection) {
        return this.connectorLifecycleManager.getConnector(connection);
    }
}
