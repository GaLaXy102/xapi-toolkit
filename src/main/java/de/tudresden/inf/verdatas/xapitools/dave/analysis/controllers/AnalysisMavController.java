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

/**
 * ModelAndView Controller for DAVE Analyses Management
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Controller
@Order(2)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AnalysisMavController implements IUIManagementFlow {
    private static final String BASE_URL = "/ui/dave/manage/analysis";
    private final DaveAnalysisService daveAnalysisService;

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

    /**
     * Show a List with all Analyses, or only a specific on
     *
     * @param analysisId    Filter for a specific Analyis ID
     * @param finalizedOnly if true: only Analyses, whose configuration is completed are shown
     */
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

    /**
     * Copy an Analysis
     *
     * @param analysisId UUID of the {@link DaveVis} to copy
     */
    @PostMapping(BASE_URL + "/copy")
    public RedirectView copyAnalysis(@RequestParam("flow") UUID analysisId) {
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        DaveVis copy = this.daveAnalysisService.createCopyOfAnalysis(analysis);
        this.daveAnalysisService.checkAnalysisConfiguration(copy);
        return new RedirectView("./show");
    }

    /**
     * Delete an Analysis
     *
     * @param analysisId UUID of the {@link DaveVis} to delete. Must not be used in a {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard}
     */
    @PostMapping(BASE_URL + "/delete")
    public RedirectView deleteAnalysis(@RequestParam("flow") UUID analysisId) {
        DaveVis analysis = this.daveAnalysisService.getAnalysis(analysisId);
        this.daveAnalysisService.deleteAnalysis(analysis);
        return new RedirectView("./show");
    }

    /**
     * Show the Add page for Analyses
     */
    @GetMapping(BASE_URL + "/add")
    public ModelAndView showAddAnalysis() {
        ModelAndView mav = new ModelAndView("bootstrap/dave/analysis/detail");
        mav.addObject("possibleQueries", this.daveAnalysisService.getAllQueries().toList());
        mav.addObject("possibleGraphs", this.daveAnalysisService.getAllGraphDescriptions().toList());
        mav.addObject("method", "add");
        return mav;
    }

    /**
     * Handle the creation of an Analysis
     *
     * @param name             Title of the new Analysis. Must not be already used
     * @param queryName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} should be created this title must not be already used
     * @param query            Query Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. Duplication inhibited
     * @param graphName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} should be created this title must not be used already
     * @param graphDescription Graph Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. Duplication inhibited
     */
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
        Optional<Map<String, Object>> objectMap = this.daveAnalysisService.checkForSideEffects(Mode.CREATING, name, Optional.empty(),
                queryName, query, graphName, graphDescription);
        if (objectMap.isPresent()) {
            RedirectView red = new RedirectView("./ack");
            objectMap.get().forEach(attributes::addFlashAttribute);
            return red;
        }
        this.daveAnalysisService.createAnalysis(name, query, queryName, graphDescription, graphName);
        return new RedirectView("./show");
    }

    /**
     * Show the Edit page for the given Analysis
     *
     * @param analysisId UUID of the Analysis to edit
     */
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

    /**
     * Handle the editing of an Analysis
     *
     * @param analysisId       UUID of the Analysis to edit
     * @param name             Title of the Analysis. Must not be already used
     * @param queryName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} should be created this title must not be already used
     * @param query            Query Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. Duplication inhibited
     * @param graphName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} should be created this title must not be used already
     * @param graphDescription Graph Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. Duplication inhibited
     */
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
        Optional<Map<String, Object>> objectMap = this.daveAnalysisService.checkForSideEffects(Mode.EDITING, name, Optional.of(analysis),
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

    /**
     * Get user acknowledgement in case of conflicting Analyses
     *
     * @param request contains details of Analysis, whose creation or modification has led to conflicts
     */
    @GetMapping(BASE_URL + "/ack")
    public ModelAndView getUserAcknowledgement(HttpServletRequest request) {
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);

        return this.prepareAcknowledge((Mode) inputFlashMap.get("mode"),
                Optional.ofNullable((UUID) inputFlashMap.get("flow")),
                (String) inputFlashMap.get("name"),
                (String) inputFlashMap.get("queryContent"),
                (String) inputFlashMap.get("queryName"),
                (String) inputFlashMap.get("graphContent"),
                (String) inputFlashMap.get("graphName"),
                (List<String>) inputFlashMap.get("messages"),
                (List<String>) inputFlashMap.get("hints"));
    }

    /**
     * Handle the editing of an Analysis after user acknowledgement was given
     *
     * @param analysisId       UUID of the Analysis to edit
     * @param name             Title of the Analysis. Must not be already used
     * @param queryName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} should be created this title must not be already used
     * @param query            Query Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. Duplication inhibited
     * @param graphName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} should be created this title must not be used already
     * @param graphDescription Graph Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. Duplication inhibited
     */
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

    /**
     * Prepare user acknowledgement page in case of conflicting Analyses
     *
     * @param mode             used to distinguish between creation and modification of Analyses
     * @param analysisId       UUID of Analysis to be modified. Only available if mode equals {@link Mode}.EDITING
     * @param name             Title of the Analysis. Must not be already used
     * @param queryName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} should be created this title must not be already used
     * @param query            Query Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. Duplication inhibited
     * @param graphName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} should be created this title must not be used already
     * @param graphDescription Graph Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. Duplication inhibited
     * @param errorMessages    List of errors, which have occurred
     * @param hints            List of hints to solve the occurred errors
     */
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

    /**
     * UI helper enum, controls button states
     */
    public enum Mode {
        CREATING,
        EDITING
    }
}
