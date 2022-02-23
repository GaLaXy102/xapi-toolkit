package de.tudresden.inf.verdatas.xapitools.dave.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.DaveAnalysisService;
import de.tudresden.inf.verdatas.xapitools.dave.DaveVisualisationService;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
import de.tudresden.inf.verdatas.xapitools.ui.BootstrapUIIcon;
import de.tudresden.inf.verdatas.xapitools.ui.IUIFlow;
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
import org.springframework.web.servlet.view.RedirectView;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@Order(3)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveAnalysisMavController implements IUIFlow {

    /**
     * UI helper enum, controls button states
     */
    enum Mode {
        CREATING,
        EDITING
    }

    private final DaveAnalysisService daveAnalysisService;
    private final DaveVisualisationService daveVisualisationService;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;
    private final List<AnalysisStep> children;

    static final String BASE_URL = "/ui/dave/dashboards";

    /**
     * Get the Human readable name of this sub-application.
     *
     * @return Name of sub-application
     */
    @Override
    public String getName() {
        return "Dashboards";
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
     * Get all Steps belonging to the sub-application, so they can be displayed alongside the Launcher.
     *
     * @return List of sub-app Steps
     */
    @Override
    public List<AnalysisStep> getSteps() {
        return this.children;
    }

    /**
     * Get the Icon for this UI Element
     *
     * @return Icon entity
     */
    @Override
    public UIIcon getIcon() {
        return BootstrapUIIcon.CHART;
    }

    @GetMapping(BASE_URL + "/show")
    public ModelAndView showDetails(@RequestParam(name = "flow") Optional<UUID> dashboardId,
                                    @RequestParam(name = "finalized_only") Optional<Boolean> finalizedOnly) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/detail");
        List<DaveDashboard> dashboards = dashboardId
                .map(this.daveAnalysisService::getDashboard)
                .map(List::of)
                .orElseGet(
                        () -> this.daveAnalysisService
                                .getAllDashboards(finalizedOnly.orElse(true))
                                .sorted(
                                        Comparator
                                                .comparing(DaveDashboard::getName, Comparator.naturalOrder())
                                                .thenComparing(DaveDashboard::getId, Comparator.naturalOrder())
                                )
                                .toList()
                );
        mav.addObject("dashboards", dashboards);
        return mav;
    }

    @PostMapping(BASE_URL + "/show")
    public RedirectView executeDashboard(@RequestParam(name = "flow") UUID dashboardId, RedirectAttributes attributes) {
        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView("./execute");
    }

    // TODO
    @GetMapping(BASE_URL + "/execute")
    public ModelAndView executeVisualisationsOfDashboard(@RequestParam(name = "flow") UUID dashboardId) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/show");
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);

        mav.addObject("dashboard", dashboard);
        mav.addObject("graphs", this.daveVisualisationService.executeVisualisationsOfDashboard(dashboard));
        return mav;
    }

    @PostMapping(BASE_URL + "/copy")
    public RedirectView copyDashboard(@RequestParam(name = "flow") UUID dashboardId) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        DaveDashboard copy = this.daveAnalysisService.createCopyOfDashboard(dashboard);
        this.daveAnalysisService.checkDashboardConfiguration(copy);
        return new RedirectView("../dashboards/show");
    }

    @PostMapping(BASE_URL + "/delete")
    public RedirectView deleteDashboard(@RequestParam(name = "flow") UUID dashboardId) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        this.daveAnalysisService.deleteDashboard(dashboard);
        return new RedirectView("../dashboards/show");
    }
}
