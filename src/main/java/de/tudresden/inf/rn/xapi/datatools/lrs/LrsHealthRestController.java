package de.tudresden.inf.rn.xapi.datatools.lrs;

import de.tudresden.inf.rn.xapi.datatools.lrs.validators.Active;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Validated
@RequiredArgsConstructor
public class LrsHealthRestController {
    private final LrsService lrsService;

    public static final String HEALTH_ENDPOINT = "/api/v1/lrs/health";

    @GetMapping(HEALTH_ENDPOINT)
    ResponseEntity<Boolean> getHealthOfLrs(@RequestParam(name = "lrs") UUID connectionId) {
        @Active LrsConnection connection = this.lrsService.getConnection(connectionId);
        return ResponseEntity.ok(this.lrsService.getConnector(connection).getHealth());
    }
}
