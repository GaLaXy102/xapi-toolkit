package de.tudresden.inf.verdatas.xapitools.dave.connector;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.ui.IExternalService;
import lombok.Getter;
import lombok.NonNull;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.task.TaskExecutor;
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

/**
 * This Service provides a connection to a DAVE instance with the help of Selenium
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
public class DaveConnector implements IExternalService, Closeable, DisposableBean {
    private static final String HEALTH_ENDPOINT = "";

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final TaskExecutor taskExecutor;
    private final URL daveEndpoint;
    private final Supplier<WebDriver> getDriverFunction;
    private LrsConnection lrsConnection = null;
    private WebDriver driver;
    @Getter
    private Boolean health = null;

    /**
     * Constructor of a DAVE Connector for an existing LRS Connection. Used by {@link DaveConnectorLifecycleManager}
     *
     * @param daveEndpoint      URL to reach the DAVE framework
     * @param lrsConnection     Valid {@link LrsConnection} Entity
     * @param getDriverFunction {@link WebDriver} for Selenium
     * @param taskExecutor      used for scheduling of tasks
     */
    DaveConnector(URL daveEndpoint, LrsConnection lrsConnection, Supplier<WebDriver> getDriverFunction,
                  TaskExecutor taskExecutor) {
        this.daveEndpoint = daveEndpoint;
        this.lrsConnection = lrsConnection;
        this.getDriverFunction = getDriverFunction;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Constructor of the additional DAVE Connector for validation of Analyses descriptions. Used by {@link DaveConnectorLifecycleManager}
     *
     * @param daveEndpoint      URL to reach the DAVE framework
     * @param getDriverFunction {@link WebDriver} for Selenium
     * @param taskExecutor      used for scheduling of tasks
     */
    DaveConnector(URL daveEndpoint, Supplier<WebDriver> getDriverFunction, TaskExecutor taskExecutor) {
        this.daveEndpoint = daveEndpoint;
        this.getDriverFunction = getDriverFunction;
        this.taskExecutor = taskExecutor;
    }

    /**
     * Initialize the DAVE Connector
     */
    void initialize() {
        if (this.driver != null) return;
        // Try to initialize DAVE connector, because of random errors caused by the use of selenium multiple tries could be necessary
        for (int i = 0; i < 5; i++) {
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

    /**
     * Initialize the additional DAVE Connector for validation of Analyses
     */
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

    /**
     * Close the connection to DAVE. Used if an exception occurred while executing
     */
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

    /**
     * Check whether the connected instance is alive
     * <p>
     * To run manually, call this method and afterwards get the result from getHealth()
     */
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
    private void healthChangedCallback(@NonNull Boolean newHealth) {
        if (newHealth.equals(this.health)) return;
        this.health = newHealth;
        if (this.health && this.lrsConnection != null) {
            this.logger.info("DAVE-" + this.lrsConnection.getFriendlyName() + " connection is alive.");
        } else if (this.health) {
            this.logger.info("DAVE connection is alive.");
        }
        if (!this.health && this.lrsConnection != null) {
            this.logger.warning("DAVE-" + this.lrsConnection.getFriendlyName()
                    + " is not responding correctly. Tried URL " + this.daveEndpoint.toString() + HEALTH_ENDPOINT);
        } else if (!this.health) {
            this.logger.warning("DAVE is not responding correctly. Tried URL "
                    + this.daveEndpoint.toString() + HEALTH_ENDPOINT);
        }
    }

    /**
     * Get the visualisation for an Analysis.
     * Uses files to store the descriptions and uploads them to DAVE with the help of Selenium to ensure their correctly delivered.
     *
     * @param queryPath Path, where the Query description can be found
     * @param graphPath Path, where the Graph Description can be found
     * @return result of Analysis as diagram
     * @throws de.tudresden.inf.verdatas.xapitools.dave.connector.DaveExceptions.NoDaveConnection when there's an error with the DAVE Connector
     */
    public synchronized String executeAnalysis(String queryPath, String graphPath) {
        if (this.getHealth()) {
            try {
                return DaveInteractions.executeAnalysis(this.driver, queryPath, graphPath);
            } catch (Exception e) {
                this.close();
                this.taskExecutor.execute(this::initialize);
                throw new DaveExceptions.AnalysisExecutionException(e.getMessage());
            }
        }
        if (this.lrsConnection != null) {
            throw new DaveExceptions.NoDaveConnection("Interaction with "
                    + "DAVE-" + this.lrsConnection.getFriendlyName() + " not possible.");
        } else {
            throw new DaveExceptions.NoDaveConnection("Interaction with DAVE not possible.");
        }
    }

    /**
     * Validate if the given Analysis matches the scheme for DAVE Analyses
     *
     * @param query Query description to validate
     * @param graph Graph Description to validate
     * @return errors, if validation failed
     * @throws de.tudresden.inf.verdatas.xapitools.dave.connector.DaveExceptions.AnalysisConfigurationException when an Exception occurred while executing the Analysis
     * @throws de.tudresden.inf.verdatas.xapitools.dave.connector.DaveExceptions.NoDaveConnection               when there's an error with the DAVE Connector
     */
    public Optional<String> testAnalysisExecution(String query, String graph) {
        if (this.getHealth()) {
            try {
                Optional<String> queryError = DaveInteractions.
                        addDescriptionToAnalysis(this.driver, query, DaveInteractions.AnalysisDescription.QUERY);
                Optional<String> graphError = DaveInteractions.
                        addDescriptionToAnalysis(this.driver, graph, DaveInteractions.AnalysisDescription.GRAPH);
                if (queryError.isPresent()) {
                    return queryError;
                } else if (graphError.isPresent()) {
                    return graphError;
                }
                return Optional.empty();
            } catch (Exception e) {
                this.close();
                this.taskExecutor.execute(this::startTestSession);
                throw new DaveExceptions.AnalysisConfigurationException(e.getMessage());
            }
        }
        throw new DaveExceptions.NoDaveConnection("Interaction with DAVE not possible.");
    }

    /**
     * Get the execution results of the given Analysis.
     * This is the information, which was filtered by the corresponding Query and is used to create the visualisation.
     *
     * @param queryPath Path, where the Query description can be found
     * @param graphPath Path, where the Graph Description can be found
     * @return results of the Analysis as nested {@link List}
     * @throws de.tudresden.inf.verdatas.xapitools.dave.connector.DaveExceptions.AnalysisResultError when an Exception occurred while executing the Analysis
     * @throws de.tudresden.inf.verdatas.xapitools.dave.connector.DaveExceptions.NoDaveConnection    when there's an error with the DAVE Connector
     */
    public List<String> getAnalysisResult(String queryPath, String graphPath) {
        if (this.getHealth()) {
            try {
                String result = DaveInteractions.getAnalysisResult(this.driver, queryPath, graphPath);
                if (result.startsWith("#")) {
                    result = result.substring(2, result.length() - 1);
                } else {
                    result = result.substring(1, result.length() - 1);
                }
                return Pattern.compile("\\[[^\\[\\]]*\\]", Pattern.MULTILINE)
                        .matcher(result)
                        .results()
                        .map(MatchResult::group)
                        .toList();
            } catch (Exception e) {
                this.close();
                this.taskExecutor.execute(this::initialize);
                throw new DaveExceptions.AnalysisResultError(e.getMessage());
            }
        }
        if (this.lrsConnection != null) {
            throw new DaveExceptions.NoDaveConnection("Interaction with "
                    + "DAVE-" + this.lrsConnection.getFriendlyName() + " not possible.");
        } else {
            throw new DaveExceptions.NoDaveConnection("Interaction with DAVE not possible.");
        }
    }
}
