package de.tudresden.inf.verdatas.xapitools.dave.connector;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveHealthRestController {
    private final DaveConnector connector;

    /**
     * Endpoint for Health Checks
     */
    public static final String HEALTH_ENDPOINT = "/api/v1/dave/health";

    /**
     * Respond to Health Check Queries
     *
     * @return true if alive, false if dead
     */
    @GetMapping(HEALTH_ENDPOINT)
    ResponseEntity<Boolean> getHealthOfDave() {
        return ResponseEntity.ok(this.connector.getHealth());
    }
}
