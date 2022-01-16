package de.tudresden.inf.rn.xapi.datatools.datasim;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimHealthRestController {
    private final DatasimConnector connector;

    static final String HEALTH_ENDPOINT = "/api/v1/datasim/health";

    @GetMapping(HEALTH_ENDPOINT)
    ResponseEntity<Boolean> getHealthOfDatasim() {
        return ResponseEntity.ok(this.connector.getHealth());
    }
}
