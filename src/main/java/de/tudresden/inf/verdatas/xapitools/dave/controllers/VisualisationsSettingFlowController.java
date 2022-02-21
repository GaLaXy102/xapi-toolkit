package de.tudresden.inf.verdatas.xapitools.dave.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.DaveAnalysisService;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVis;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Controller
@Order(3)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class VisualisationsSettingFlowController implements AnalysisStep{
    private final DaveAnalysisService daveAnalysisService;
    private final LrsService lrsService;

    static final String BASE_URL = DaveAnalysisMavController.BASE_URL + "/dashboards";

    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    @Override
    public String getName() {
        return "Select analysis";
    }

    /**
     * Get the Paths which belong to this step.
     * When this pattern is matched, the step will be highlighted in the UI.
     * Be sure to match **any** subpath of your step.
     *
     * @return Regex-Pattern matching all Paths that belong to this step
     */
    @Override
    public Pattern getPathRegex() {
        return Pattern.compile(BASE_URL + "/(new|edit)/visualisations(/add)?$");
    }

    // TODO Write method to get results and activityId
    @GetMapping(BASE_URL + "/new/visualisations")
    public ModelAndView showSelectAnalysis(@RequestParam(name = "flow") UUID dashboardId) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        LrsConnection lrsConnection = dashboard.getLrsConnection();
        List<String> activities = this.daveAnalysisService.getActivitiesOfLrs(lrsConnection);

        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/analysis");
        mav.addObject("flow", dashboardId.toString());
        mav.addObject("possibleActivities", activities);
        mav.addObject("possibleAnalysis", this.daveAnalysisService.getAllAnalysis().toList());
        mav.addObject("dashboardVisualisations", this.daveAnalysisService.getVisualisationsOfDashboard(dashboard));
        mav.addObject("mode", DaveAnalysisMavController.Mode.CREATING);
        return mav;
    }

    @GetMapping(BASE_URL + "/edit/visualisations")
    public ModelAndView showEditAnalysis(@RequestParam(name = "flow") UUID dashboardId) {
        ModelAndView mav = this.showSelectAnalysis(dashboardId);
        mav.addObject("mode", DaveAnalysisMavController.Mode.EDITING);
        return mav;
    }

    @PostMapping(BASE_URL + "/new/visualisations/add")
    public RedirectView addVisualisationToDashboard(@RequestParam(name = "flow") UUID dashboardId,
                                                    @RequestParam(name = "activity") String activityId,
                                                    @RequestParam(name = "analysis") String analysisIdentifier,
                                                    DaveAnalysisMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        DaveVis visualisation = this.daveAnalysisService.getAnalysisByName(analysisIdentifier);
        this.daveAnalysisService.addVisualisationToDashboard(dashboard, activityId, visualisation);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveAnalysisMavController.Mode.CREATING.equals(mode) ? "../visualisations" : ".../edit/visualisations");
    }

    @PostMapping(BASE_URL + "/new/visualisations")
    public RedirectView selectVisualisations(@RequestParam(name = "flow") UUID dashboardId,
                                             DaveAnalysisMavController.Mode mode, RedirectAttributes attributes) {
        this.daveAnalysisService.finalizeDashboard(this.daveAnalysisService.getDashboard(dashboardId));
        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView("../show");
    }

    @PostMapping(BASE_URL + "/new/visualisations/up")
    public RedirectView moveVisualisationUp(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "position") Integer position,
                                            DaveAnalysisMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        this.daveAnalysisService.moveVisualisationOfDashboard(dashboard, position, DaveAnalysisService.Move.UP);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveAnalysisMavController.Mode.CREATING.equals(mode) ? "../visualisations" : ".../edit/visualisations");
    }

    @PostMapping(BASE_URL + "/new/visualisations/down")
    public RedirectView moveVisualisationDown(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "position") Integer position,
                                              DaveAnalysisMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        this.daveAnalysisService.moveVisualisationOfDashboard(dashboard, position, DaveAnalysisService.Move.DOWN);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveAnalysisMavController.Mode.CREATING.equals(mode) ? "../visualisations" : ".../edit/visualisations");
    }

    @PostMapping(BASE_URL + "/new/visualisations/delete")
    public RedirectView deleteVisualisation(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "position") Integer position,
                                            DaveAnalysisMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        this.daveAnalysisService.deleteVisualisationFromDashboard(dashboard, position);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveAnalysisMavController.Mode.CREATING.equals(mode) ? "../visualisations" : ".../edit/visualisations");
    }

}
