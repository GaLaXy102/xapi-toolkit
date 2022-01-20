package de.tudresden.inf.verdatas.xapitools.lrs.connector;

import com.fasterxml.jackson.databind.JsonNode;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsHealthRestController;
import de.tudresden.inf.verdatas.xapitools.ui.IExternalService;
import lombok.Getter;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class LrsConnector implements IExternalService {
    // See https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI-Communication.md#28-about-resource
    private static final String HEALTH_ENDPOINT = "/about";
    private static final String STATEMENTS_ENDPOINT = "/statements";
    private static final Pair<String, String> XAPI_VERSION_HEADER = Pair.of("X-Experience-API-Version", "1.0.3");
    private static final String STATEMENTS_NODE_ID = "statements";
    private static final String MORE_NODE_ID = "more";
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final LrsConnection lrsConnection;
    @Getter
    private Boolean health;

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
        return LrsHealthRestController.HEALTH_ENDPOINT + "?lrs=" + this.lrsConnection.getConnectionId();
    }

    /**
     * Check whether the connected instance is alive
     * <p>
     * To run manually, call this method and afterwards get the result from {@link #getHealth()}
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void healthCheck() {
        RestTemplate restTemplate = new RestTemplate();
        boolean calculatedHealth;
        try {
            ResponseEntity<String> health = restTemplate.getForEntity(this.lrsConnection.getXApiEndpoint() + HEALTH_ENDPOINT, String.class);
            calculatedHealth = health.getStatusCode().is2xxSuccessful();
        } catch (ResourceAccessException | HttpClientErrorException e) {
            // This happens when connection is refused or on 404 error for example
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
            this.logger.info(this.lrsConnection.getFriendlyName() + " connection is alive.");
        } else {
            this.logger.warning(this.lrsConnection.getFriendlyName() + " is not responding correctly. Tried URL " + this.lrsConnection.getXApiEndpoint() + HEALTH_ENDPOINT);
        }
    }

    public List<UUID> sendStatements(List<JsonNode> statements) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .basicAuthentication(this.lrsConnection.getXApiClientKey(), this.lrsConnection.getXApiClientSecret())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(XAPI_VERSION_HEADER.getFirst(), XAPI_VERSION_HEADER.getSecond());
        HttpEntity<List<JsonNode>> requestEntity = new HttpEntity<>(statements, headers);
        try {
            ResponseEntity<UUID[]> result = restTemplate.postForEntity(this.lrsConnection.getXApiEndpoint() + STATEMENTS_ENDPOINT, requestEntity, UUID[].class);
            assert result.getBody() != null;
            return Arrays.asList(result.getBody());
        } catch (ResourceAccessException e) {
            // This happens when connection is refused
            throw new IllegalStateException("No connection to " + this.lrsConnection.getFriendlyName() + ".");
        } catch (HttpServerErrorException e) {
            throw new IllegalStateException(this.lrsConnection.getFriendlyName() + " has some issues.");
        }
    }

    public List<JsonNode> getStatements() {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .basicAuthentication(this.lrsConnection.getXApiClientKey(), this.lrsConnection.getXApiClientSecret())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(XAPI_VERSION_HEADER.getFirst(), XAPI_VERSION_HEADER.getSecond());
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        try {
            String irl = STATEMENTS_ENDPOINT;
            List<JsonNode> result = new LinkedList<>();
            // See https://github.com/adlnet/xAPI-Spec/blob/1.0.3/xAPI-Data.md#details-21
            while (irl != null) {
                ResponseEntity<JsonNode> response = restTemplate.exchange(
                        this.lrsConnection.getXApiEndpoint() + irl,
                        HttpMethod.GET,
                        requestEntity,
                        JsonNode.class);
                assert response.getBody() != null;
                Iterator<JsonNode> statements = response.getBody().get(STATEMENTS_NODE_ID).elements();
                result.addAll(
                        Stream.generate(() -> null)
                                .takeWhile(x -> statements.hasNext())
                                .map(n -> statements.next())
                                .toList()
                );
                JsonNode nextUrlNode = response.getBody().get(MORE_NODE_ID);
                if ("".equals(nextUrlNode.asText())) {
                    irl = null;
                } else {
                    irl = nextUrlNode.asText().replaceFirst(this.lrsConnection.getXApiEndpoint().getPath(), "");
                }
            }
            return result;
        } catch (ResourceAccessException e) {
            // This happens when connection is refused
            throw new IllegalStateException("No connection to " + this.lrsConnection.getFriendlyName() + ".");
        } catch (HttpServerErrorException e) {
            throw new IllegalStateException(this.lrsConnection.getFriendlyName() + " has some issues.");
        }
    }
}
