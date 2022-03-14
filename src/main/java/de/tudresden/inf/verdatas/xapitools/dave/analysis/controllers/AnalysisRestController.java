package de.tudresden.inf.verdatas.xapitools.dave.analysis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.tudresden.inf.verdatas.xapitools.dave.analysis.DaveAnalysisService;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVis;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.InputMismatchException;
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
    @Transactional
    public RedirectView createAnalysisFromFile(@RequestParam List<MultipartFile> analysisFiles) {
        JsonMapper mapper = new JsonMapper();
        List<JsonNode> analysisData = analysisFiles.stream()
                .map((multipartFile -> {
                    try {
                        return multipartFile.getInputStream();
                    } catch (IOException e) {
                        throw new InputMismatchException("Could not read input file.");
                    }
                }))
                .map((input) -> {
                    try {
                        return mapper.<JsonNode>readValue(input, JsonNode.class);
                    } catch (IOException e) {
                        throw new InputMismatchException("Could not read input file. Input was expected to be a List of analysis.");
                    }
                })
                .toList();
        this.daveAnalysisService.retrieveAnalysisDescriptions(analysisData);
        return new RedirectView("/ui/dave/manage/analysis/show");
    }
}
