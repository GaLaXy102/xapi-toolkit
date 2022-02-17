package de.tudresden.inf.verdatas.xapitools.dave.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.DaveAnalysisService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Controller
@Order(3)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveAnalysisMavController implements IUIFlow {

    /**
     * UI helper enum, controls button states
     */
    enum Mode {
        CREATING,
        EDITING,
        DISPLAYING
    }

    private final DaveAnalysisService daveAnalysisService;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;
    private final List<AnalysisStep> children;

    static final String BASE_URL = "/ui/dave";

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
        return BASE_URL + "/";
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

    @GetMapping(BASE_URL + "/")
    public ModelAndView showOverview() {
        ModelAndView mav = new ModelAndView("bootstrap/dave/overview");
        return mav;
    }

    @GetMapping(BASE_URL + "/dashboards/show")
    public ModelAndView showDetail(@RequestParam(name = "flow") Optional<UUID> dashboardId) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/detail");
        // Prepare and collect visualisation helpers
        Map<DaveDashboard, Long> numVis = new HashMap<>();
        List<DaveDashboard> dashboards = dashboardId
                .map(this.daveAnalysisService::getDashboard)
                .map(List::of)
                .orElseGet(
                        () -> this.daveAnalysisService.getAllDashboards()
                                .sorted(
                                        Comparator
                                                .comparing(DaveDashboard::getIdentifier, Comparator.naturalOrder())
                                                .thenComparing(DaveDashboard::getId, Comparator.naturalOrder())
                                )
                                .toList()
                );
        dashboards.forEach(
                (dashboard) -> {
                    numVis.put(
                            dashboard,
                            dashboard.getVisualisations().stream().distinct().count()
                    );
                }
        );
        mav.addObject("dashboards", dashboards);
        mav.addObject("numVis", numVis);
        return mav;
    }
}
