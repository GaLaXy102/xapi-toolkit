package de.tudresden.inf.verdatas.xapitools.lrs;

import com.fasterxml.jackson.databind.JsonNode;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.lrs.connector.LrsConnector;
import de.tudresden.inf.verdatas.xapitools.lrs.connector.LrsConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.lrs.validators.Active;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service managing LRS Connections.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Service
@DependsOn("lrsConnectionSeeder")
@Validated
public class LrsService {

    private final LrsConnectionRepository lrsConnectionRepository;
    private final LrsConnectorLifecycleManager connectorLifecycleManager;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;

    /**
     * This constructor is for Spring-internal use.
     * It also bootstraps the connectors for the enabled connections.
     */
    LrsService(LrsConnectionRepository lrsConnectionRepository, LrsConnectorLifecycleManager connectorLifecycleManager,
               DaveConnectorLifecycleManager daveConnectorLifecycleManager) {
        this.lrsConnectionRepository = lrsConnectionRepository;
        this.connectorLifecycleManager = connectorLifecycleManager;
        this.daveConnectorLifecycleManager = daveConnectorLifecycleManager;
        // Create Connectors for all active Connections at Boot time
        this.lrsConnectionRepository.findAll().stream().filter(LrsConnection::isEnabled).forEach(this.connectorLifecycleManager::createConnector);
        this.lrsConnectionRepository.findAll().stream().filter(LrsConnection::isEnabled).forEach(this.daveConnectorLifecycleManager::createConnector);
    }

    /**
     * Create an LRS connection from a Transfer Object, save it and create an {@link LrsConnector} for it.
     */
    @Transactional
    LrsConnection createConnection(LrsConnectionTO lrsData) {
        LrsConnection created = lrsData.toNewLrsConnection();
        this.lrsConnectionRepository.save(created);
        this.connectorLifecycleManager.createConnector(created);
        this.daveConnectorLifecycleManager.createConnector(created);
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
        this.daveConnectorLifecycleManager.deleteConnector(connection);
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
        this.daveConnectorLifecycleManager.createConnector(connection);
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
        return this.lrsConnectionRepository.findById(connectionId).orElseThrow(() -> new NoSuchElementException("No such LRS connection."));
    }

    /**
     * Update an LRS connection from a Transfer Object
     *
     * @throws IllegalArgumentException When there is no connection with the ID specified in the Transfer Object or the ID is not given.
     */
    @Transactional
    LrsConnection updateConnection(LrsConnectionTO lrsData) {
        LrsConnection found = this.getConnection(lrsData.getUuid().orElseThrow(NoSuchElementException::new));
        found.setFriendlyName(lrsData.getName());
        found.setXApiEndpoint(lrsData.getEndpoint());
        found.setXApiClientKey(lrsData.getClientKey());
        found.setXApiClientSecret(lrsData.getClientSecret());
        found.setEnabled(lrsData.getEnabled().orElse(found.isEnabled()));
        this.lrsConnectionRepository.save(found);
        this.connectorLifecycleManager.deleteConnector(found);
        this.daveConnectorLifecycleManager.deleteConnector(found);
        this.connectorLifecycleManager.createConnector(found);
        this.daveConnectorLifecycleManager.createConnector(found);
        return found;
    }

    /**
     * Send a list of xAPI Statements to an LRS via the specified {@link LrsConnection}
     *
     * @param statements List of xAPI Statements to send. No Validation is applied.
     * @param connection Connection details to use. Must be Active.
     * @return The responded list of Statement IDs in the LRS.
     */
    public List<UUID> sendStatements(List<JsonNode> statements, @Active LrsConnection connection) {
        LrsConnector connector = this.connectorLifecycleManager.getConnector(connection);
        return connector.pushStatements(statements);
    }

    /**
     * Get all Statements from the specified LRS.
     *
     * @param connection Connection details to use. Must be Active.
     * @return List of all xAPI Statements contained in the given LRS.
     */
    public List<JsonNode> getStatements(@Active LrsConnection connection) {
        LrsConnector connector = this.connectorLifecycleManager.getConnector(connection);
        return connector.pullStatements();
    }

    /**
     * Get the {@link LrsConnector} for a given {@link LrsConnection}.
     *
     * @param connection Connection details to use. Must be Active.
     * @return {@link LrsConnector} for the connection.
     */
    LrsConnector getConnector(@Active LrsConnection connection) {
        return this.connectorLifecycleManager.getConnector(connection);
    }
}
