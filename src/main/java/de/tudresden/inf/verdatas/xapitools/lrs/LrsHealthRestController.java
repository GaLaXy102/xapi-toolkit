package de.tudresden.inf.verdatas.xapitools.lrs;

import de.tudresden.inf.verdatas.xapitools.lrs.validators.Active;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Rest Controller for LRS Health Checks
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@RestController
@Validated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LrsHealthRestController {
    private final LrsService lrsService;

    /**
     * Endpoint for Health Checks
     */
    public static final String HEALTH_ENDPOINT = "/api/v1/lrs/health";

    /**
     * Respond to Health Check Queries
     *
     * @param connectionId ID of {@link LrsConnection} of which the Health shall be queried.
     * @return 200 and true if alive, 200 and false if dead, 400 and error page if not found or inactive connection.
     */
    @GetMapping(HEALTH_ENDPOINT)
    ResponseEntity<Boolean> getHealthOfLrs(@RequestParam(name = "id") UUID connectionId) {
        @Active LrsConnection connection = this.lrsService.getConnection(connectionId);
        return ResponseEntity.ok(this.lrsService.getConnector(connection).getHealth());
    }
}
