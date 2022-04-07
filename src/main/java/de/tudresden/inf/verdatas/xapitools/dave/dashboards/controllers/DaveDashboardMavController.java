package de.tudresden.inf.verdatas.xapitools.dave.dashboards.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.dave.dashboards.DaveDashboardService;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
import de.tudresden.inf.verdatas.xapitools.ui.BasepageMavController;
import de.tudresden.inf.verdatas.xapitools.ui.BootstrapUIIcon;
import de.tudresden.inf.verdatas.xapitools.ui.IUIFlow;
import de.tudresden.inf.verdatas.xapitools.ui.UIIcon;
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
import java.util.stream.Collectors;

/**
 * ModelAndView Controller for the Dashboard Application
 * By implementing {@link IUIFlow}, it is bound automatically to the main UI in {@link BasepageMavController}.
 * It contains all Views that are not part of {@link DashboardStep}s.
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Controller
@Order(3)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveDashboardMavController implements IUIFlow {
    static final String BASE_URL = "/ui/dave/dashboards";
    private final DaveDashboardService daveDashboardService;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;
    private final List<DashboardStep> children;

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
    public List<DashboardStep> getSteps() {
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

    /**
     * Show a List with all Dashboards, or only a specific on
     *
     * @param dashboardId   Filter for a specific Dashboard ID
     * @param finalizedOnly if true: only Dashboards, whose configuration is completed are shown
     */
    @GetMapping(BASE_URL + "/show")
    public ModelAndView showDetails(@RequestParam(name = "flow") Optional<UUID> dashboardId,
                                    @RequestParam(name = "finalized_only") Optional<Boolean> finalizedOnly) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/detail");
        List<DaveDashboard> dashboards = dashboardId
                .map(this.daveDashboardService::getDashboard)
                .map(List::of)
                .orElseGet(
                        () -> this.daveDashboardService
                                .getAllDashboards(finalizedOnly.orElse(false))
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

    /**
     * Start execution of all Analyses for the given Dashboard
     *
     * @param dashboardId UUID of the Dashboard to execute
     */
    @PostMapping(BASE_URL + "/show")
    public RedirectView executeDashboard(@RequestParam(name = "flow") UUID dashboardId, RedirectAttributes attributes) {
        attributes.addAttribute("flow", dashboardId.toString());
        if (!this.daveDashboardService.checkConnectorInitialisation(this.daveDashboardService.getDashboard(dashboardId))) {
            return new RedirectView("../error");
        }
        return new RedirectView("./execute");
    }

    /**
     * Execute all Analyses of the given Dashboard and show the resulting diagrams
     *
     * @param dashboardId UUID of the Dashboard to execute
     */
    @GetMapping(BASE_URL + "/execute")
    public ModelAndView executeVisualisationsOfDashboard(@RequestParam(name = "flow") UUID dashboardId) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/show");
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        HashMap<UUID, String> analysisIdToName = new HashMap<>(dashboard.getVisualisations()
                .stream()
                .map(Pair::getSecond)
                .distinct()
                .map((analysisId) -> Pair.of(analysisId, this.daveDashboardService.getNameOfAnalysis(analysisId)))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
        );

        mav.addObject("dashboard", dashboard);
        mav.addObject("visNames", analysisIdToName);
        return mav;
    }

    /**
     * Create a copy of the given Dashboard
     *
     * @param dashboardId UUID of the Dashboard to copy
     */
    @PostMapping(BASE_URL + "/copy")
    public RedirectView copyDashboard(@RequestParam(name = "flow") UUID dashboardId) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        DaveDashboard copy = this.daveDashboardService.createCopyOfDashboard(dashboard);
        this.daveDashboardService.checkDashboardConfiguration(copy);
        return new RedirectView("../dashboards/show");
    }

    /**
     * Delete the given Dashboard
     *
     * @param dashboardId UUID of the Dashboard to delete
     */
    @PostMapping(BASE_URL + "/delete")
    public RedirectView deleteDashboard(@RequestParam(name = "flow") UUID dashboardId) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        this.daveDashboardService.deleteDashboard(dashboard);
        return new RedirectView("../dashboards/show");
    }

    /**
     * Show an error page when the user either tried to interact with a DAVE-Connector, which was not initialised or tried to limit the execution of an Analysis, for which it is not supported
     *
     * @param dashboardId UUID of the Dashboard which was used
     * @param analysisName Title of the Analysis which was used
     */
    @GetMapping(BASE_URL + "/error")
    public ModelAndView showErrorMessage(@RequestParam(name = "flow") UUID dashboardId,
                                         @RequestParam(name = "analysisName") Optional<String> analysisName) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/error");
        if (analysisName.isPresent()) {
            mav.addObject("title", "Invalid configuration of analysis' execution.");
            mav.addObject("message", "The analysis '"
                    + analysisName.get()
                    + "' is to complex to be executed on a single activity. \n"
                    + "Please select the whole LRS for its execution.");
        } else {
            mav.addObject("title", "Unsuccessful interaction with DAVE-Connector.");
            mav.addObject("message", "The DAVE-Connector '"
                    + this.daveConnectorLifecycleManager.getConnector(dashboard.getLrsConnection()).getName()
                    + "' is not ready for interaction. \n"
                    + "Please wait, until its initialisation is completed.");
        }
        return mav;
    }

    /**
     * UI helper enum, controls button states
     */
    enum Mode {
        CREATING,
        EDITING
    }
}
