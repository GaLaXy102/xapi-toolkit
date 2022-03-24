package de.tudresden.inf.verdatas.xapitools.dave.dashboards.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.dashboards.DaveDashboardService;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dave")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveDashboardRestController {
    private final DaveDashboardService daveDashboardService;

    @GetMapping("/visualisation")
    public ResponseEntity<String> downloadVisualisation(@RequestParam(name = "flow") UUID dashboardId,
                                                        @RequestParam(name = "activityURL") String activityURL,
                                                        @RequestParam(name = "visId") UUID analysisId) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        String svgContent = this.daveDashboardService.executeVisualisationOfDashboard(dashboard, activityURL, analysisId);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename("vis-" + dashboard.getName().replace(" ", "_")
                        + "-" + this.daveDashboardService.getNameOfAnalysis(analysisId).replace(" ", "_")
                        + "-" + DateTimeFormatter.ISO_DATE_TIME.format(LocalDateTime.now().withNano(0)) + ".svg")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.parseMediaType("image/svg+xml"));
        return ResponseEntity.ok().headers(headers).body(svgContent);
    }
}
