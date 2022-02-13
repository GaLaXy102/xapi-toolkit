package de.tudresden.inf.verdatas.xapitools.dave.connector;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.ui.IExternalService;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.Closeable;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DaveConnector implements IExternalService, Closeable {
    private static final String HEALTH_ENDPOINT = "";
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final URL daveEndpoint;
    private final LrsConnection lrsConnection;
    private WebDriver driver;
    @Getter
    private Boolean health = null;

    DaveConnector(URL daveEndpoint, LrsConnection lrsConnection) {
        this.daveEndpoint = daveEndpoint;
        this.lrsConnection = lrsConnection;
    }

    void initialize() {
        if (this.driver != null) return;
        this.driver = DaveInteractions.startNewSession(daveEndpoint);

        try {
            DaveInteractions.createWorkbook(this.driver);
            DaveInteractions.connectLRS(this.driver, lrsConnection.getFriendlyName(),
                    lrsConnection.getXApiEndpoint().toString(), lrsConnection.getXApiClientKey(),
                    lrsConnection.getXApiClientSecret());
        } catch (InterruptedException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void close() {
        if (this.driver == null) return;
        this.driver.quit();
    }

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
        if (this.driver == null) return;
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
