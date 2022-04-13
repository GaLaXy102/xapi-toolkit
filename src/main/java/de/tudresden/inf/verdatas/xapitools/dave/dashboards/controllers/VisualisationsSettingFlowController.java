package de.tudresden.inf.verdatas.xapitools.dave.dashboards.controllers;

import com.google.common.base.Supplier;
import de.tudresden.inf.verdatas.xapitools.dave.dashboards.DaveDashboardService;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVis;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ModelAndView Controller for Adding Analyses to a Dashboard
 * By implementing {@link DashboardStep}, it is automatically bound in {@link DaveDashboardMavController}.
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Controller
@Order(3)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class VisualisationsSettingFlowController implements DashboardStep {
    private final DaveDashboardService daveDashboardService;

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

    /**
     * Show the page to add Analyses to an existing Dashboard.
     * To ensure faster loading of the page the activities of the associated LRS are requested once and afterwards cached for ten minutes
     *
     * @param dashboardId UUID of the associated Dashboard
     * @param cache       if true the cached activities of the associated LRS are used
     */
    @GetMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations")
    public ModelAndView showSelectAnalysis(@RequestParam(name = "flow") UUID dashboardId, Optional<Boolean> cache) {
        if (!cache.orElse(true)) this.daveDashboardService.cleanCaches();
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        LrsConnection lrsConnection = dashboard.getLrsConnection();
        Map<String, List<String>> activitiesByType = this.daveDashboardService.getActivitiesOfLrs(lrsConnection)
                .entrySet()
                .stream()
                .map((entry) -> Map.entry(
                        Arrays.stream(entry.getKey().split("/"))
                                .reduce((acc, s) -> s)
                                .orElse(entry.getKey()),
                        entry.getValue()
                                .stream()
                                .sorted()
                                .collect(Collectors.toList())
                ))
                .collect(
                        Collectors.groupingBy(
                                Map.Entry::getKey,
                                Collectors.collectingAndThen(Collectors.toList(),
                                        (entries) -> entries.stream().map(Map.Entry::getValue).flatMap(List::stream).toList()
                                )
                        )
                );

        Map<String, String> activityToType = new HashMap<>();
        activitiesByType.forEach((key, value) -> value.forEach((activity) -> activityToType.put(activity, key)));
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/analysis");
        mav.addObject("flow", dashboardId.toString());
        mav.addObject("possibleActivities", activitiesByType);
        mav.addObject("possibleAnalysis", this.daveDashboardService.getAllAnalysis(true)
                .sorted(Comparator.comparing(DaveVis::getName))
                .toList());
        mav.addObject("dashboardVisualisations", this.daveDashboardService.getVisualisationsOfDashboard(dashboard));
        mav.addObject("activityTypes", activityToType);
        mav.addObject("mode", DaveDashboardMavController.Mode.CREATING);
        return mav;
    }

    /**
     * Show the page to edit Analyses of an existing Dashboard.
     * To ensure faster loading of the page the activities of the associated LRS are requested once and afterwards cached for ten minutes
     *
     * @param dashboardId UUID of the associated Dashboard
     * @param cache       if true the cached activities of the associated LRS are used
     */
    @GetMapping(DaveDashboardMavController.BASE_URL + "/edit/visualisations")
    public ModelAndView showEditAnalysis(@RequestParam(name = "flow") UUID dashboardId, Optional<Boolean> cache) {
        ModelAndView mav = this.showSelectAnalysis(dashboardId, cache);
        mav.addObject("mode", DaveDashboardMavController.Mode.EDITING);
        return mav;
    }

    /**
     * Add an Analysis to the existing Dashboard
     *
     * @param dashboardId  UUID of the associated Dashboard
     * @param activityId   indicates if the Analysis is executed using the whole LRS data set or only the data belonging to a specific activity
     * @param analysisName Name of the Analysis to add
     * @param mode         -- Page mode, used for redirection
     */
    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations/add")
    public RedirectView addVisualisationToDashboard(@RequestParam(name = "flow") UUID dashboardId,
                                                    @RequestParam(name = "activity") String activityId,
                                                    @RequestParam(name = "analysis") String analysisName,
                                                    DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        attributes.addAttribute("flow", dashboardId.toString());
        DaveVis analysis = this.daveDashboardService.getAnalysisByName(analysisName);
        if (!(activityId.equals("all")) && !this.daveDashboardService.checkLimitationOfAnalysis(analysis)) {
            attributes.addAttribute("analysisName", analysis.getName());
            return new RedirectView("../../error");
        }
        UUID analysisIdentifier = analysis.getId();
        this.daveDashboardService.addVisualisationToDashboard(dashboard, activityId, analysisIdentifier);

        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "../visualisations" : "../../edit/visualisations");
    }

    /**
     * Finalize the configuration of an existing Dashboard
     *
     * @param dashboardId UUID of the associated Dashboard
     */
    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations")
    public RedirectView selectVisualisations(@RequestParam(name = "flow") UUID dashboardId, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        this.daveDashboardService.checkDashboardConfiguration(dashboard);
        this.daveDashboardService.finalizeDashboard(dashboard);
        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView("../show");
    }

    /**
     * Change position of element in the Analyses' List of an existing Dashboard
     *
     * @param dashboardId UUID of the associated Dashboard
     * @param position    of Analysis, which should be moved one position forward in the Analyses' List
     * @param mode        -- Page mode, used for redirection
     */
    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations/up")
    public RedirectView moveVisualisationUp(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "position") Integer position,
                                            DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        this.daveDashboardService.shiftPositionOfVisualisationOfDashboard(dashboard, position, DaveDashboardService.Move.UP);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "../visualisations" : "../../edit/visualisations");
    }

    /**
     * Change position of element in the Analyses' List of an existing Dashboard
     *
     * @param dashboardId UUID of the associated Dashboard
     * @param position    of Analysis, which should be moved one position backward in the Analyses' List
     * @param mode        -- Page mode, used for redirection
     */
    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations/down")
    public RedirectView moveVisualisationDown(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "position") Integer position,
                                              DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        this.daveDashboardService.shiftPositionOfVisualisationOfDashboard(dashboard, position, DaveDashboardService.Move.DOWN);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "../visualisations" : "../../edit/visualisations");
    }

    /**
     * Delete an Analysis from the Analyses' List of an existing Dashboard
     *
     * @param dashboardId UUID of the associated Dashboard
     * @param position    of Analysis, which should be deleted from the Analyses' List
     * @param mode        -- Page mode, used for redirection
     */
    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/visualisations/delete")
    public RedirectView deleteVisualisation(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "position") Integer position,
                                            DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        this.daveDashboardService.deleteVisualisationFromDashboard(dashboard, position);

        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "../visualisations" : "../../edit/visualisations");
    }
}
