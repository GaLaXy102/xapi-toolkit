package de.tudresden.inf.verdatas.xapitools.dave.connector;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsService;
import de.tudresden.inf.verdatas.xapitools.lrs.validators.Active;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveHealthRestController {
    private final LrsService lrsService;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;

    /**
     * Endpoint for Health Checks
     */
    public static final String HEALTH_ENDPOINT = "/api/v1/dave/health";

    /**
     * Respond to Health Check Queries
     *
     * @param connectionId ID of {@link LrsConnection} which belongs to the DAVEConnector of which the Health shall be queried.
     * @return 200 and true if alive, 200 and false if dead, 400 and error page if not found or inactive connection.
     */
    @GetMapping(HEALTH_ENDPOINT)
    ResponseEntity<Boolean> getHealthOfDave(@RequestParam(name = "id") Optional<UUID> connectionId) {
        if (connectionId.isPresent()) {
            @Active LrsConnection connection = this.lrsService.getConnection(connectionId.get());
            return ResponseEntity.ok(this.daveConnectorLifecycleManager.getConnector(connection).getHealth());
        } else {
            return ResponseEntity.ok(this.daveConnectorLifecycleManager.getTestConnector().getHealth());
        }
    }
}
