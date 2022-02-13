package de.tudresden.inf.verdatas.xapitools.datasim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulationTO;
import de.tudresden.inf.verdatas.xapitools.ui.IExternalService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * This Service provides a connection to a DATASIM instance.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@EnableScheduling
@Service
@Order(1)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimConnector implements IExternalService {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    @Value("${xapi.datasim.backend-base-url}")
    private URL datasimEndpoint;

    @Value("${xapi.datasim.backend-username}")
    private String datasimUser;

    @Value("${xapi.datasim.backend-password}")
    private String datasimPassword;

    @Getter
    private Boolean health = null;

    private static final String HEALTH_ENDPOINT = "/health";
    private static final String SIM_ENDPOINT = "/api/v1/generate";

    private final ObjectMapper mapper;

    /**
     * Get the Human readable name of this Service.
     *
     * @return Name of service
     */
    @Override
    public String getName() {
        return "DATASIM";
    }

    /**
     * Get the Path to the Health Check Endpoint for this Service.
     *
     * @return Path to Health Endpoint
     */
    @Override
    public String getCheckEndpoint() {
        return DatasimHealthRestController.HEALTH_ENDPOINT;
    }

    /**
     * Check whether the connected instance is alive
     * <p>
     * To run manually, call this method and afterwards get the result from getHealth()
     */
    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    protected void healthCheck() {
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
     * <p>
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

    /**
     * Send the prepared {@link DatasimSimulationTO} (i.e. {@link DatasimSimulationTO#forExport()} has been called) to DATASIM
     *
     * @param simulation Simulation description
     * @return Resulting xAPI Statements
     */
    public List<JsonNode> sendSimulation(DatasimSimulationTO simulation) {
        RestTemplate restTemplate = new RestTemplateBuilder().basicAuthentication(this.datasimUser, this.datasimPassword).build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("personae-array", simulation.getPersonaGroups());
        parts.add("profiles", simulation.getProfiles());
        parts.add("alignments", simulation.getAlignments());
        parts.add("parameters", simulation.getParameters());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);
        try {
            String result = restTemplate.postForObject(this.datasimEndpoint.toString() + SIM_ENDPOINT, requestEntity, String.class);
            // Now it becomes hacky.
            // DATASIM doesn't output valid JSON, as the result appears to be a list ([ ... ]),
            // but the contained statements have no delimiting ",", but rather "\n".
            // As none of these are safely replaceable, we have to remove the first two and the last two bytes of the result.
            assert result != null;
            if (result.equals("[\n]")) return List.of();
            result = result.substring(2, result.length() - 2);
            // Now we have the statements remaining, one by line
            String[] statements = result.split("\n");
            return Arrays.stream(statements).map(content -> {
                try {
                    return this.mapper.readTree(content);
                } catch (JsonProcessingException e) {
                    return mapper.nullNode();
                }
            }).toList();
        } catch (ResourceAccessException e) {
            // This happens when connection is refused
            throw new DatasimExceptions.NoDatasimConnection("No connection to Datasim.");
        }
    }
}
