package de.tudresden.inf.verdatas.xapitools.dave.analysis;

import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVis;
import de.tudresden.inf.verdatas.xapitools.ui.BootstrapUIIcon;
import de.tudresden.inf.verdatas.xapitools.ui.IUIManagementFlow;
import de.tudresden.inf.verdatas.xapitools.ui.UIIcon;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@Order(2)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AnalysisMavController implements IUIManagementFlow {
    private final DaveAnalysisService daveAnalysisService;

    private static final String BASE_URL = "/ui/dave/manage/analysis";

    /**
     * UI helper enum, controls button states
     */
    enum Mode {
        CREATING,
        EDITING
    }

    /**
     * Get the Human readable name of this Setting.
     *
     * @return Name of Setting
     */
    @Override
    public String getName() {
        return "DAVE Analyses";
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
    public ModelAndView createAnalysis(@RequestParam("name") String name,
                                       @RequestParam("queryContent") String query,
                                       @RequestParam("queryName") String queryName,
                                       @RequestParam("graphContent") String graphDescription,
                                       @RequestParam("graphName") String graphName) {
        this.daveAnalysisService.checkValidityOfAnalysisDescription(query, graphDescription);
        Optional<ModelAndView> mav = this.checkForSideEffects(Mode.CREATING, name, Optional.empty(),
                queryName, query, graphName, graphDescription);
        if (mav.isPresent()) {
            return mav.get();
        }
        this.daveAnalysisService.createAnalysis(name, query.replace("\r", ""), queryName,
                        graphDescription.replace("\r", ""), graphName);
        return new ModelAndView("redirect:./show");
    }

    @PostMapping(BASE_URL + "/add/ack")
    public RedirectView finalizeCreatingOfAnalysis(@RequestParam("name") String name,
                                                  @RequestParam("queryContent") String query,
                                                  @RequestParam("queryName") String queryName,
                                                  @RequestParam("graphContent") String graphDescription,
                                                  @RequestParam("graphName") String graphName) {
        this.daveAnalysisService.createAnalysis(name, query.replace("\r", ""), queryName,
                graphDescription.replace("\r", ""), graphName);
        return new RedirectView("../show");
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
    public ModelAndView editAnalysis(@RequestParam("flow") UUID analysisId,
                                     @RequestParam("name") String name,
                                     @RequestParam("queryContent") String query,
                                     @RequestParam("queryName") String queryName,
                                     @RequestParam("graphContent") String graphDescription,
                                     @RequestParam("graphName") String graphName) {
        this.daveAnalysisService.checkValidityOfAnalysisDescription(query, graphDescription);
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        Optional<ModelAndView> mav = this.checkForSideEffects(Mode.EDITING, name, Optional.of(analysis),
                queryName, query, graphName, graphDescription);
        if (mav.isPresent()) {
            return mav.get();
        }
        this.daveAnalysisService.updateAnalysis(analysis, name, query.replace("\r", ""), queryName,
                graphDescription.replace("\r", ""), graphName);
        return new ModelAndView("redirect:./show");
    }

    @PostMapping(BASE_URL + "/edit/ack")
    public RedirectView finalizeEditingOfAnalysis(@RequestParam("flow") UUID analysisId,
                                                  @RequestParam("name") String name,
                                                  @RequestParam("queryContent") String query,
                                                  @RequestParam("queryName") String queryName,
                                                  @RequestParam("graphContent") String graphDescription,
                                                  @RequestParam("graphName") String graphName) {
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        this.daveAnalysisService.updateAnalysis(analysis, name, query.replace("\r", ""), queryName,
                graphDescription.replace("\r", ""), graphName);
        return new RedirectView("../show");
    }
    
    public Optional<ModelAndView> checkForSideEffects(Mode mode, String name, Optional<DaveVis> analysis, String queryName, String query,
                                            String graphName, String graphDescription) {
        Set<String> analysisWithSameQuery;
        Set<String> analysisWithSameGraph;
        Set<String> dashboardNames = null;
        if (mode.equals(Mode.CREATING)) {
            analysisWithSameQuery = this.daveAnalysisService.checkForQueryConflicts(queryName, query)
                    .stream()
                    .map(DaveVis::getName)
                    .collect(Collectors.toSet());
            analysisWithSameGraph = this.daveAnalysisService.checkForGraphDescriptionConflicts(graphName, graphDescription)
                    .stream()
                    .map(DaveVis::getName)
                    .collect(Collectors.toSet());
        } else {
            dashboardNames = this.daveAnalysisService.checkUsageOfAnalysis(analysis.get());
            analysisWithSameQuery = this.daveAnalysisService.checkUsageOfQuery(analysis.get(), queryName, query);
            analysisWithSameGraph = this.daveAnalysisService.checkUsageOfGraphDescription(analysis.get(), graphName, graphDescription);
        }
        // Needed if checking for side effects provides error and user has to acknowledge changes
        ModelAndView mav = new ModelAndView("bootstrap/dave/analysis/conflict");
        mav.addObject("mode", mode);
        mav.addObject("name", name);
        mav.addObject("queryContent", query);
        mav.addObject("queryName", queryName);
        mav.addObject("graphContent", graphDescription);
        mav.addObject("graphName", graphName);
        
        if (mode.equals(Mode.EDITING)) {
            mav.addObject("flow", analysis.get().getId());
            if (!(dashboardNames.isEmpty())) {
                mav.addObject("message", "Modification of " + analysis.get().getName()
                        + " not possible.\n Still in use for dashboard(s) " + dashboardNames);
                mav.addObject("hint", "Please make a copy the analysis first and modify it afterwarts " +
                        "if you do not want the dashboard(s) to be changed.\n Otherwise, continue the modification.");
                return Optional.of(mav);
            }
        }
        if (!(analysisWithSameQuery.isEmpty())) {
            mav.addObject("message", "Modification of query not possible.\n Still in use for analysis "
                        + analysisWithSameQuery);
            mav.addObject("hint", "Please use a different name for your query " +
                        "if you do not want the other analysis to be changed.\n Otherwise, continue the modification.");
            return Optional.of(mav);
        } else if (!(analysisWithSameGraph.isEmpty())) {
            mav.addObject("message", "Modification of graph description not possible.\n Still in use for analysis "
                        + analysisWithSameGraph);
            mav.addObject("hint", "Please use a different name for your graphDescription " +
                        "if you do not want the other analysis to be changed.\n Otherwise, continue the modification.");
            return Optional.of(mav);
        }
        return Optional.empty();
    }
}

