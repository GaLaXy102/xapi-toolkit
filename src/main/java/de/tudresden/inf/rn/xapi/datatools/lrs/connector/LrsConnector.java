package de.tudresden.inf.rn.xapi.datatools.lrs.connector;

import de.tudresden.inf.rn.xapi.datatools.lrs.LrsConnection;
import de.tudresden.inf.rn.xapi.datatools.ui.IExternalService;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LrsConnector implements IExternalService {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final LrsConnection lrsConnection;

    @Getter
    private Boolean health;

    // See https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI-Communication.md#28-about-resource
    private static final String HEALTH_ENDPOINT = "/about";

    LrsConnector(LrsConnection lrsConnection) {
        this.lrsConnection = lrsConnection;
        this.logger.info("Created Connector for " + lrsConnection.getFriendlyName());
    }

    @Override
    public String getName() {
        return this.lrsConnection.getFriendlyName();
    }

    @Override
    public String getCheckEndpoint() {
        return this.lrsConnection.getXApiEndpoint() + HEALTH_ENDPOINT;
    }

    /**
     * Check whether the connected instance is alive
     *
     * To run manually, call this method and afterwards get the result from {@link #getHealth()}
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void healthCheck() {
        RestTemplate restTemplate = new RestTemplate();
        boolean calculatedHealth;
        try {
            ResponseEntity<String> health = restTemplate.getForEntity(this.lrsConnection.getXApiEndpoint() + HEALTH_ENDPOINT, String.class);
            calculatedHealth = health.getStatusCode().is2xxSuccessful();
        } catch (ResourceAccessException e) {
            // This happens when connection is refused
            calculatedHealth = false;
        }
        // Only log changes
        if (this.health == null || calculatedHealth != this.health) {
            this.healthChangedCallback(calculatedHealth);
        }
    }

    /**
     * Callback for timed health check
     *
     * Sets the health-Property and logs the result.
     */
    private void healthChangedCallback(boolean newHealth) {
        this.health = newHealth;
        if (this.health) {
            this.logger.info(this.lrsConnection.getFriendlyName() + " connection is alive.");
        } else {
            this.logger.warning(this.lrsConnection.getFriendlyName() + " is not responding correctly. Tried URL " + this.lrsConnection.getXApiEndpoint() + HEALTH_ENDPOINT);
        }
    }
}
