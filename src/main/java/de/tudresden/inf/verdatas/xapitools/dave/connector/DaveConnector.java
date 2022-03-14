package de.tudresden.inf.verdatas.xapitools.dave.connector;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.ui.IExternalService;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.Closeable;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class DaveConnector implements IExternalService, Closeable, DisposableBean {
    private static final String HEALTH_ENDPOINT = "";

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final URL daveEndpoint;
    private final Supplier<WebDriver> getDriverFunction;
    private LrsConnection lrsConnection = null;
    private WebDriver driver;
    @Getter
    private Boolean health = null;

    DaveConnector(URL daveEndpoint, LrsConnection lrsConnection, Supplier<WebDriver> getDriverFunction) {
        this.daveEndpoint = daveEndpoint;
        this.lrsConnection = lrsConnection;
        this.getDriverFunction = getDriverFunction;
    }

    DaveConnector(URL daveEndpoint, Supplier<WebDriver> getDriverFunction) {
        this.daveEndpoint = daveEndpoint;
        this.getDriverFunction = getDriverFunction;
    }

    void initialize() {
        if (this.driver != null) return;
        // Try to initialize DAVE connector, because of random errors caused by the use of selenium multiple tries could be necessary
        for (int i = 0; i < 3; i++) {
            this.driver = DaveInteractions.startNewSession(this.daveEndpoint, this.getDriverFunction);
            try {
                DaveInteractions.initializeDave(this.driver, this.lrsConnection.getFriendlyName(),
                        this.lrsConnection.getXApiEndpoint().toString(), this.lrsConnection.getXApiClientKey(),
                        this.lrsConnection.getXApiClientSecret());
                this.healthChangedCallback(true);
                return;
            } catch (Exception e) {
                this.close();
                logger.warning("Starting exception occurred, retrying: " + e.getMessage());
            }
        }
        // Nothing helped...
        this.healthChangedCallback(false);
    }

    void startTestSession() {
        if (this.driver != null) return;
        this.driver = DaveInteractions.startNewSession(this.daveEndpoint, this.getDriverFunction);
        try {
            DaveInteractions.initializeTestSession(this.driver);
            this.healthChangedCallback(true);
        } catch (Exception e) {
            this.close();
            logger.warning("Exception occurred: " + e.getMessage());
            logger.info("Restart connector " + this.getName());
            this.startTestSession();
        }

    }

    // TODO prevent null pointer exception
    public void close() {
        if (this.driver == null) return;
        this.driver.quit();
        this.healthChangedCallback(false);
        this.driver = null;
    }

    /**
     * Invoked by {@link DaveConnectorLifecycleManager} on destruction.
     */
    @Override
    public void destroy() {
        this.close();
    }

    /**
     * Get the Human readable name of this Service.
     *
     * @return Name of service
     */
    @Override
    public String getName() {
        if (this.lrsConnection != null) {
            return "DAVE-" + this.lrsConnection.getFriendlyName();
        } else {
            return "DAVE";
        }
    }

    /**
     * Get the Path to the Health Check Endpoint for this Service.
     *
     * @return Path to Health Endpoint
     */
    @Override
    public String getCheckEndpoint() {
        if (this.lrsConnection != null) {
            return DaveHealthRestController.HEALTH_ENDPOINT + "?id=" + this.lrsConnection.getConnectionId();
        } else {
            return DaveHealthRestController.HEALTH_ENDPOINT;
        }
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    protected void healthCheck() {
        if (this.health != null && this.driver != null) {
            this.healthChangedCallback(DaveInteractions.checkForInitSuccess(this.driver));
        } else if (this.driver != null) {
            DaveInteractions.checkForLogo(this.driver);
        }
    }

    /**
     * Callback for timed health check
     * <p>
     * Sets the health-Property and logs the result.
     */
    private void healthChangedCallback(boolean newHealth) {
        this.health = newHealth;
        if (this.health && this.lrsConnection != null) {
            this.logger.info("DAVE-" + this.lrsConnection.getFriendlyName() + " connection is alive.");
        } else if (this.health) {
            this.logger.info("DAVE" + " connection is alive.");
        }
        if (!this.health && this.lrsConnection != null) {
            this.logger.warning("DAVE-" + this.lrsConnection.getFriendlyName() + " is not responding correctly. Tried URL " + this.daveEndpoint.toString() + HEALTH_ENDPOINT);
        } else if (!this.health) {
            this.logger.warning("DAVE" + " is not responding correctly. Tried URL " + this.daveEndpoint.toString() + HEALTH_ENDPOINT);
        }
    }

    public synchronized String executeAnalysis(String queryPath, String graphPath) {
        if (this.getHealth()) {
            try {
                return DaveInteractions.executeAnalysis(this.driver, queryPath, graphPath);
            } catch (Exception e) {
                this.close();
                this.initialize();
                throw new IllegalStateException(e.getMessage());
            }
        }
        if (this.lrsConnection != null) {
            throw new IllegalStateException("Interaction with " + "DAVE-" + this.lrsConnection.getFriendlyName() + " not possible.");
        } else {
            throw new IllegalStateException("Interaction with " + "DAVE" + " not possible.");
        }
    }

    public Optional<String> testAnalysisExecution(String query, String graph) {
        if (this.getHealth()) {
            try {
                Optional<String> queryError = DaveInteractions.addDescriptionToAnalysis(this.driver, query, DaveInteractions.AnalysisDescription.QUERY);
                Optional<String> graphError = DaveInteractions.addDescriptionToAnalysis(this.driver, graph, DaveInteractions.AnalysisDescription.GRAPH);
                if (queryError.isPresent()) {
                    return queryError;
                } else if (graphError.isPresent()) {
                    return graphError;
                }
                return Optional.empty();
            } catch (Exception e) {
                this.close();
                this.startTestSession();
                throw new IllegalStateException(e.getMessage());
            }
        }
        throw new IllegalStateException("Interaction with " + "DAVE" + " not possible.");
    }

    public List<String> getAnalysisResult(String queryPath, String graphPath) {
        if (this.getHealth()) {
            try {
                String result = DaveInteractions.getAnalysisResult(this.driver, queryPath, graphPath);
                if (result.startsWith("#")) {
                    result = result.substring(2, result.length() - 1);
                } else {
                    result = result.substring(1, result.length() - 1);
                }
                return Pattern.compile("\\[[^\\[\\]]*\\]", Pattern.MULTILINE).matcher(result).results().map(MatchResult::group).toList();
            } catch (Exception e) {
                this.close();
                this.initialize();
                throw new IllegalStateException(e.getMessage());
            }
        }
        if (this.lrsConnection != null) {
            throw new IllegalStateException("Interaction with " + "DAVE-" + this.lrsConnection.getFriendlyName() + " not possible.");
        } else {
            throw new IllegalStateException("Interaction with " + "DAVE" + " not possible.");
        }
    }
}
