package de.tudresden.inf.verdatas.xapitools.dave.analysis;

import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVis;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dave/analysis")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AnalysisRestController {
    private final DaveAnalysisService daveAnalysisService;

    @GetMapping("/description")
    public ResponseEntity<DaveVis> getAnalysisDescription(@RequestParam("flow") UUID analysisId) {
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(analysis.getName().replace(" ", "_") + ".json")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        return ResponseEntity.ok().headers(headers).body(analysis);
    }

    @PostMapping("/insert")
    public RedirectView createAnalysisFromFile(@RequestParam List<MultipartFile> analysisFile) {
        return null;
    }
}
