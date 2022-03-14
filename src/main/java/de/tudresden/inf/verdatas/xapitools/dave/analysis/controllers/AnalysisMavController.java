package de.tudresden.inf.verdatas.xapitools.dave.analysis.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.analysis.DaveAnalysisService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
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
    public RedirectView createAnalysis(@RequestParam("name") String name,
                                       @RequestParam("queryContent") String query,
                                       @RequestParam("queryName") String queryName,
                                       @RequestParam("graphContent") String graphDescription,
                                       @RequestParam("graphName") String graphName,
                                       RedirectAttributes attributes) {
        query = query.replace("\r", "");
        graphDescription = graphDescription.replace("\r", "");

        this.daveAnalysisService.checkValidityOfAnalysisDescription(query, graphDescription);
        Optional<Map<String, Object>> objectMap = this.checkForSideEffects(Mode.CREATING, name, Optional.empty(),
                queryName, query, graphName, graphDescription);
        if (objectMap.isPresent()) {
            RedirectView red = new RedirectView("./ack");
            objectMap.get().forEach(attributes::addFlashAttribute);
            return red;
        }
        this.daveAnalysisService.createAnalysis(name, query, queryName, graphDescription, graphName);
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
                                     @RequestParam("queryContent") String query,
                                     @RequestParam("queryName") String queryName,
                                     @RequestParam("graphContent") String graphDescription,
                                     @RequestParam("graphName") String graphName, RedirectAttributes attributes) {
        query = query.replace("\r", "");
        graphDescription = graphDescription.replace("\r", "");

        this.daveAnalysisService.checkValidityOfAnalysisDescription(query, graphDescription);
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        Optional<Map<String, Object>> objectMap = this.checkForSideEffects(Mode.EDITING, name, Optional.of(analysis),
                queryName, query, graphName, graphDescription);
        if (objectMap.isPresent()) {
            RedirectView red = new RedirectView("./ack");
            objectMap.get().forEach(attributes::addFlashAttribute);
            return red;
        }
        this.daveAnalysisService.updateAnalysis(analysis, name, query.replace("\r", ""), queryName,
                graphDescription.replace("\r", ""), graphName);
        return new RedirectView("./show");
    }

    // TODO Error page if parameters not there
    @GetMapping(BASE_URL + "/ack")
    public ModelAndView getUserAcknowledgement(HttpServletRequest request) {
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        assert inputFlashMap != null;

        return this.prepareAcknowledge((Mode) inputFlashMap.get("mode"), Optional.ofNullable((UUID) inputFlashMap.get("flow")),
                (String) inputFlashMap.get("name"), (String) inputFlashMap.get("queryContent"), (String) inputFlashMap.get("queryName"),
                (String) inputFlashMap.get("graphContent"), (String) inputFlashMap.get("graphName"), (List<String>) inputFlashMap.get("messages"),
                (List<String>) inputFlashMap.get("hints"));
    }

    @PostMapping(BASE_URL + "/ack")
    public RedirectView finalizeModifyingOfAnalysis(@RequestParam("flow") Optional<UUID> analysisId,
                                                    @RequestParam("name") String name,
                                                    @RequestParam("queryContent") String query,
                                                    @RequestParam("queryName") String queryName,
                                                    @RequestParam("graphContent") String graphDescription,
                                                    @RequestParam("graphName") String graphName) {
        if (analysisId.isPresent()) {
            DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId.get());
            this.daveAnalysisService.updateAnalysis(analysis, name, query.replace("\r", ""), queryName,
                    graphDescription.replace("\r", ""), graphName);
        } else {
            this.daveAnalysisService.createAnalysis(name, query.replace("\r", ""), queryName,
                    graphDescription.replace("\r", ""), graphName);
        }
        return new RedirectView("./show");
    }

    public Optional<Map<String, Object>> checkForSideEffects(Mode mode, String name, Optional<DaveVis> analysis, String queryName, String query,
                                            String graphName, String graphDescription) {
        Set<String> analysisWithQueryConflict = Set.of();
        Set<String> analysisWithGraphConflict = Set.of();
        Set<String> dashboardNames = Set.of();
        Map<String, Object> objectMap = new HashMap<>();
        List<String> messages = new LinkedList<>();
        List<String> hints = new LinkedList<>();

        if (mode.equals(Mode.CREATING)) {
            analysisWithQueryConflict = this.daveAnalysisService.checkForQueryConflicts(queryName, query)
                    .stream()
                    .map(DaveVis::getName)
                    .collect(Collectors.toSet());
            analysisWithGraphConflict = this.daveAnalysisService.checkForGraphDescriptionConflicts(graphName, graphDescription)
                    .stream()
                    .map(DaveVis::getName)
                    .collect(Collectors.toSet());
        } else if (analysis.isPresent()){
            dashboardNames = this.daveAnalysisService.checkUsageOfAnalysis(analysis.get());
            analysisWithQueryConflict = this.daveAnalysisService.checkUsageOfQuery(analysis.get(), queryName, query);
            analysisWithGraphConflict = this.daveAnalysisService.checkUsageOfGraphDescription(analysis.get(), graphName, graphDescription);
        }

        // Needed if checking for side effects provides error and user has to acknowledge changes
        objectMap.put("mode", mode);
        objectMap.put("name", name);
        objectMap.put("queryContent", query);
        objectMap.put("queryName", queryName);
        objectMap.put("graphContent", graphDescription);
        objectMap.put("graphName", graphName);

        if (mode.equals(Mode.EDITING) && analysis.isPresent()) {
            objectMap.put("flow", analysis.get().getId());
            if (!(dashboardNames.isEmpty())) {
                messages.add("Modification of " + analysis.get().getName()
                        + " not possible.\n Still in use for dashboard(s) " + dashboardNames);
                hints.add("Please make a copy the analysis first and modify it afterwarts " +
                        "if you do not want the dashboard(s) to be changed.\n Otherwise, continue the modification.");
            }
        }

        if (!(analysisWithQueryConflict.isEmpty())) {
            messages.add("Modification of query not possible.\n Still in use for analysis "
                    + analysisWithQueryConflict);
            hints.add("Please use a different name for your query " +
                    "if you do not want the other analysis to be changed.\n Otherwise, continue the modification.");
        }
        if (!(analysisWithGraphConflict.isEmpty())) {
            messages.add("Modification of graph description not possible.\n Still in use for analysis "
                    + analysisWithGraphConflict);
            hints.add("Please use a different name for your graphDescription " +
                    "if you do not want the other analysis to be changed.\n Otherwise, continue the modification.");
        }

        if (!(messages.isEmpty())) {
            objectMap.put("messages", messages);
            objectMap.put("hints", hints);
            return Optional.of(objectMap);
        }
        return Optional.empty();
    }

    public ModelAndView prepareAcknowledge(Mode mode, Optional<UUID> analysisId, String name, String query, String queryName,
                                         String graphDescription, String graphName, List<String> errorMessages, List<String> hints) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/analysis/conflict");
        mav.addObject("mode", mode);
        mav.addObject("name", name);
        mav.addObject("queryContent", query);
        mav.addObject("queryName", queryName);
        mav.addObject("graphContent", graphDescription);
        mav.addObject("graphName", graphName);
        mav.addObject("messages", errorMessages);
        mav.addObject("hints", hints);
        analysisId.ifPresent(uuid -> mav.addObject("flow", uuid));
        return mav;
    }
}

