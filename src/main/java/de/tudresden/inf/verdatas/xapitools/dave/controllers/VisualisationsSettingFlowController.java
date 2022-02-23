package de.tudresden.inf.verdatas.xapitools.dave.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.DaveDashboardService;
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
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Controller
@Order(3)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class VisualisationsSettingFlowController implements DashboardStep {
    private final DaveDashboardService daveAnalysisService;
    private final LrsService lrsService;

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
        return Pattern.compile(DaveDashboardMavController.BASE_URL + "/(new|edit)/visualisations(/add)?$");
    }

    // TODO Write method to get results and activityId
    @GetMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations")
    public ModelAndView showSelectAnalysis(@RequestParam(name = "flow") UUID dashboardId, Optional<Boolean> cache) {
        if (!cache.orElse(true)) this.daveAnalysisService.cleanCaches();
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        LrsConnection lrsConnection = dashboard.getLrsConnection();
        List<String> activities = this.daveAnalysisService.getActivitiesOfLrs(lrsConnection);

        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/analysis");
        mav.addObject("flow", dashboardId.toString());
        mav.addObject("possibleActivities", activities);
        mav.addObject("possibleAnalysis", this.daveAnalysisService.getAllAnalysis().toList());
        mav.addObject("dashboardVisualisations", this.daveAnalysisService.getVisualisationsOfDashboard(dashboard));
        mav.addObject("mode", DaveDashboardMavController.Mode.CREATING);
        return mav;
    }

    @GetMapping(DaveDashboardMavController.BASE_URL + "/edit/visualisations")
    public ModelAndView showEditAnalysis(@RequestParam(name = "flow") UUID dashboardId) {
        ModelAndView mav = this.showSelectAnalysis(dashboardId, Optional.empty());
        mav.addObject("mode", DaveDashboardMavController.Mode.EDITING);
        return mav;
    }

    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations/add")
    public RedirectView addVisualisationToDashboard(@RequestParam(name = "flow") UUID dashboardId,
                                                    @RequestParam(name = "activity") String activityId,
                                                    @RequestParam(name = "analysis") String analysisName,
                                                    DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        UUID analysisIdentifier = this.daveAnalysisService.getAnalysisByName(analysisName).getId();
        this.daveAnalysisService.addVisualisationToDashboard(dashboard, activityId, analysisIdentifier);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "../visualisations" : "../../edit/visualisations");
    }

    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations")
    public RedirectView selectVisualisations(@RequestParam(name = "flow") UUID dashboardId, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        this.daveAnalysisService.checkDashboardConfiguration(dashboard);
        this.daveAnalysisService.finalizeDashboard(dashboard);
        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView("../show");
    }

    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations/up")
    public RedirectView moveVisualisationUp(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "position") Integer position,
                                            DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        this.daveAnalysisService.shiftPositionOfVisualisationOfDashboard(dashboard, position, DaveDashboardService.Move.UP);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "../visualisations" : "../../edit/visualisations");
    }

    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations/down")
    public RedirectView moveVisualisationDown(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "position") Integer position,
                                              DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        this.daveAnalysisService.shiftPositionOfVisualisationOfDashboard(dashboard, position, DaveDashboardService.Move.DOWN);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "../visualisations" : "../../edit/visualisations");
    }

    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations/delete")
    public RedirectView deleteVisualisation(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "position") Integer position,
                                            DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        this.daveAnalysisService.deleteVisualisationFromDashboard(dashboard, position);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "../visualisations" : "../../edit/visualisations");
    }

}
