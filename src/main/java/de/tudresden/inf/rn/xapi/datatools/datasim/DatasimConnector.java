package de.tudresden.inf.rn.xapi.datatools.datasim;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This Service provides a connection to a DATASIM instance.
 */
@EnableScheduling
@Service
public class DatasimConnector {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Getter
    @Value("${xapi.datasim.backend-base-url}")
    private URL datasimEndpoint;

    @Value("${xapi.datasim.backend-username}")
    private String datasimUser;

    @Value("${xapi.datasim.backend-password}")
    private String datasimPassword;

    @Getter
    private Boolean health = null;

    private static final String HEALTH_ENDPOINT = "/health";

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
            ResponseEntity<String> health = restTemplate.getForEntity(this.datasimEndpoint.toString() + HEALTH_ENDPOINT, String.class);
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
            this.logger.info("DATASIM connection is alive.");
        } else {
            this.logger.warning("DATASIM is not responding correctly. Tried URL " + this.datasimEndpoint.toString() + HEALTH_ENDPOINT);
        }
    }
}
