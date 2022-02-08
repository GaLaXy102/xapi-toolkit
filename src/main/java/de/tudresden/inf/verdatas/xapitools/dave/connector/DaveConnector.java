package de.tudresden.inf.verdatas.xapitools.dave.connector;

import de.tudresden.inf.verdatas.xapitools.ui.IExternalService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@EnableScheduling
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveConnector implements IExternalService {
    private static final String HEALTH_ENDPOINT = "";
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    @Value("${xapi.dave.backend-base-url}")
    private URL daveEndpoint;
    @Getter
    private Boolean health = null;

    /**
     * Get the Human readable name of this Service.
     *
     * @return Name of service
     */
    @Override
    public String getName() {
        return "DAVE";
    }

    /**
     * Get the Path to the Health Check Endpoint for this Service.
     *
     * @return Path to Health Endpoint
     */
    @Override
    public String getCheckEndpoint() {
        return DaveHealthRestController.HEALTH_ENDPOINT;
    }

    /**
     * Check whether the connected instance is alive
     * <p>
     * To run manually, call this method and afterwards get the result from getHealth()
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    protected void healthCheck() {
        // Build connection template
        RestTemplate restTemplate = this.daveEndpoint.getUserInfo() != null
                ? new RestTemplateBuilder()
                    .basicAuthentication(this.daveEndpoint.getUserInfo().split(":")[0], this.daveEndpoint.getUserInfo().split(":")[1])
                    .build()
                : new RestTemplate();
        boolean calculatedHealth;
        try {
            ResponseEntity<String> health = restTemplate.getForEntity(this.daveEndpoint.toString() + HEALTH_ENDPOINT, String.class);
            calculatedHealth = health.getStatusCode().is2xxSuccessful();
        } catch (ResourceAccessException | HttpClientErrorException e) {
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
     * <p>
     * Sets the health-Property and logs the result.
     */
    private void healthChangedCallback(boolean newHealth) {
        this.health = newHealth;
        if (this.health) {
            this.logger.info("DAVE connection is alive.");
        } else {
            this.logger.warning("DAVE is not responding correctly. Tried URL " + this.daveEndpoint.toString() + HEALTH_ENDPOINT);
        }
    }
}
