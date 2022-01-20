package de.tudresden.inf.verdatas.xapitools.lrs.statements;

import com.fasterxml.jackson.databind.JsonNode;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsService;
import de.tudresden.inf.verdatas.xapitools.lrs.validators.Active;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/statements")
@Validated
public class LrsStatementExchangeRestController {
    private final LrsService lrsService;

    public LrsStatementExchangeRestController(LrsService lrsService) {
        this.lrsService = lrsService;
    }

    @GetMapping("/pull")
    public ResponseEntity<List<JsonNode>> insertStatements(@RequestParam UUID targetLrs) {
        @Active LrsConnection lrsConnection = this.lrsService.getConnection(targetLrs);
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(lrsConnection.getFriendlyName() + "-" + LocalDateTime.now() + ".json")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(cd);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.ok().headers(headers).body(this.lrsService.getStatements(lrsConnection));
    }
}
