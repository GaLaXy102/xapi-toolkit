package de.tudresden.inf.verdatas.xapitools.datasim;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller for DATASIM Health Checks
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimHealthRestController {
    private final DatasimConnector connector;

    /**
     * Endpoint for Health Checks
     */
    public static final String HEALTH_ENDPOINT = "/api/v1/datasim/health";

    /**
     * Respond to Health Check Queries
     *
     * @return true if alive, false if dead
     */
    @GetMapping(HEALTH_ENDPOINT)
    ResponseEntity<Boolean> getHealthOfDatasim() {
        return ResponseEntity.ok(this.connector.getHealth());
    }
}
