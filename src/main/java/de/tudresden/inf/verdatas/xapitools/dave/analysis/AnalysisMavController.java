package de.tudresden.inf.verdatas.xapitools.dave.analysis;

import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVis;
import de.tudresden.inf.verdatas.xapitools.ui.BootstrapUIIcon;
import de.tudresden.inf.verdatas.xapitools.ui.IUIManagementFlow;
import de.tudresden.inf.verdatas.xapitools.ui.UIIcon;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@Order(2)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AnalysisMavController implements IUIManagementFlow {
    private final DaveAnalysisService daveAnalysisService;

    private static final String BASE_URL = "/ui/dave/manage/analysis";

    /**
     * Get the Human readable name of this Setting.
     *
     * @return Name of Setting
     */
    @Override
    public String getName() {
        return "DAVE Analysis";
    }

    /**
     * Get the URL where the sub-application can be started.
     *
     * @return URL for Application Launch
     */
    @Override
    public String getEntrypoint() {
        return BASE_URL + "/show";
    }

    /**
     * Get the Icon for this UI Element
     *
     * @return Icon entity
     */
    @Override
    public UIIcon getIcon() {
        return BootstrapUIIcon.CLIPBOARD;
    }

    @GetMapping(BASE_URL + "/show")
    public ModelAndView showAnalysis(@RequestParam("flow") Optional<UUID> analysisId,
                                     @RequestParam(name = "finalized_only") Optional<Boolean> finalizedOnly) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/analysis/show");
        List<DaveVis> analysis = analysisId
                .map(this.daveAnalysisService::getAnalysis)
                .map(List::of)
                .orElseGet(
                        () -> this.daveAnalysisService
                                .getAllAnalysis(finalizedOnly.orElse(true))
                                .sorted(
                                        Comparator
                                                .comparing(DaveVis::getName, Comparator.naturalOrder())
                                                .thenComparing(DaveVis::getId, Comparator.naturalOrder())
                                )
                                .toList()
                );
        mav.addObject("analysis", analysis);
        return mav;
    }

    @PostMapping(BASE_URL + "/copy")
    public RedirectView copyAnalysis(@RequestParam("flow") UUID analysisId) {
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        DaveVis copy = this.daveAnalysisService.createCopyOfAnalysis(analysis);
        this.daveAnalysisService.checkAnalysisConfiguration(copy);
        return new RedirectView("./show");
    }

    @PostMapping(BASE_URL + "/delete")
    public RedirectView deleteAnalysis(@RequestParam("flow") UUID analysisId) {
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        this.daveAnalysisService.deleteAnalysis(analysis);
        return new RedirectView("./show");
    }

    @GetMapping(BASE_URL + "/add")
    public ModelAndView showAddAnalysis() {
        ModelAndView mav = new ModelAndView("bootstrap/dave/analysis/detail");
        mav.addObject("possibleQueries", this.daveAnalysisService.getAllQueries().toList());
        mav.addObject("possibleGraphs", this.daveAnalysisService.getAllGraphDescriptions().toList());
        mav.addObject("method", "add");
        return mav;
    }

    @PostMapping(BASE_URL + "/add")
    public RedirectView createAnalysis(@RequestParam("name") String name,
                                       @RequestParam("queryContent") String query,
                                       @RequestParam("queryName") String queryName,
                                       @RequestParam("graphContent") String graphDescription,
                                       @RequestParam("graphName") String graphName) {
        DaveVis analysis = this.daveAnalysisService.createAnalysis(name, query.replace("\r", ""), queryName, graphDescription.replace("\r", ""), graphName);
        return new RedirectView("./show");
    }

    @GetMapping(BASE_URL + "/edit")
    public ModelAndView showEditAnalysis(@RequestParam("flow") UUID analysisId) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/analysis/detail");
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        mav.addObject("analysis", analysis);
        mav.addObject("possibleQueries", this.daveAnalysisService.getAllQueries().toList());
        mav.addObject("possibleGraphs", this.daveAnalysisService.getAllGraphDescriptions().toList());
        mav.addObject("method", "edit");
        return mav;
    }

    @PostMapping(BASE_URL + "/edit")
    public RedirectView editAnalysis(@RequestParam("flow") UUID analysisId,
                                     @RequestParam("name") String name,
                                     @RequestParam("queryId") Optional<UUID> queryId,
                                     @RequestParam("queryContent") String query,
                                     @RequestParam("queryName") String queryName,
                                     @RequestParam("graphId") Optional<UUID> graphId,
                                     @RequestParam("graphContent") String graphDescription,
                                     @RequestParam("graphName") String graphName) {
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        this.daveAnalysisService.checkUsageOfAnalysis(analysis);
        this.daveAnalysisService.updateAnalysis(analysis, name, query.replace("\r", ""), queryId, queryName,
                graphDescription.replace("\r", ""), graphId, graphName);
        return new RedirectView("./show");
    }

    @GetMapping("/dave/analysis_description")
    public ResponseEntity<DaveVis> getAnalysisDescription(@RequestParam("flow") UUID analysisId) {
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(analysis.getName())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        return ResponseEntity.ok().headers(headers).body(analysis);
    }
}

