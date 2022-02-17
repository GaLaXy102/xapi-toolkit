package de.tudresden.inf.verdatas.xapitools.dave.connector;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.ui.IExternalService;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.Closeable;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

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
        this.driver = DaveInteractions.startNewSession(this.daveEndpoint);

        try {
            DaveInteractions.initializeDave(this.driver, this.lrsConnection.getFriendlyName(),
                    this.lrsConnection.getXApiEndpoint().toString(), this.lrsConnection.getXApiClientKey(),
                    this.lrsConnection.getXApiClientSecret());
            this.healthChangedCallback(true);
        } catch (Exception e) {
            this.driver.quit();
            this.healthChangedCallback(false);
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
        return "DAVE-" + this.lrsConnection.getFriendlyName();
    }

    /**
     * Get the Path to the Health Check Endpoint for this Service.
     *
     * @return Path to Health Endpoint
     */
    @Override
    public String getCheckEndpoint() {
        return DaveHealthRestController.HEALTH_ENDPOINT + "?id=" + this.lrsConnection.getConnectionId();
    }

    /**
     * Callback for timed health check
     * <p>
     * Sets the health-Property and logs the result.
     */
    private void healthChangedCallback(boolean newHealth) {
        this.health = newHealth;
        if (this.health) {
            this.logger.info("DAVE-" + this.lrsConnection.getFriendlyName() + " connection is alive.");
        } else {
            this.logger.warning("DAVE-" + this.lrsConnection.getFriendlyName() + " is not responding correctly. Tried URL " + this.daveEndpoint.toString() + HEALTH_ENDPOINT);
        }
    }

    public synchronized String executeAnalysis(List<String> paths) {
        if (this.getHealth()) {
            try {
                return DaveInteractions.executeAnalysis(this.driver, paths);
            } catch (Exception e) {
                this.driver.quit();
                this.healthChangedCallback(false);
                throw new IllegalStateException(e.getMessage());
            }
        }
        throw new IllegalStateException("Interaction with " + "DAVE-" + this.lrsConnection.getFriendlyName() + " not possible.");
    }
}
