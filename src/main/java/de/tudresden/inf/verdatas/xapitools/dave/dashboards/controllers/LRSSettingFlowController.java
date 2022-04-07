package de.tudresden.inf.verdatas.xapitools.dave.dashboards.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.dashboards.DaveDashboardService;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
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

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * ModelAndView Controller for Setting of a Dashboards' source LRS
 * By implementing {@link DashboardStep}, it is automatically bound in {@link DaveDashboardMavController}.
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Controller
@Order(2)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LRSSettingFlowController implements DashboardStep {
    private final DaveDashboardService daveDashboardService;
    private final LrsService lrsService;

    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    @Override
    public String getName() {
        return "Select source LRS";
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
        return Pattern.compile(DaveDashboardMavController.BASE_URL + "/(new|edit)/source$");
    }

    /**
     * Show page to set the source LRS of an existing Dashboard
     *
     * @param dashboardId UUID of the associated Dashboard
     */
    @GetMapping(DaveDashboardMavController.BASE_URL + "/new/source")
    public ModelAndView showSelectLRS(@RequestParam(name = "flow") UUID dashboardId) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/source");
        mav.addObject("activeLrs", this.lrsService.getConnections(true));
        mav.addObject("flow", dashboardId.toString());
        mav.addObject("mode", DaveDashboardMavController.Mode.CREATING);
        return mav;
    }

    /**
     * Show page to edit the source LRS of an existing Dashboard
     *
     * @param dashboardId UUID of the associated Dashboard to modify
     */
    @GetMapping(DaveDashboardMavController.BASE_URL + "/edit/source")
    public ModelAndView showEditLRS(@RequestParam(name = "flow") UUID dashboardId) {
        ModelAndView mav = this.showSelectLRS(dashboardId);
        mav.addObject("mode", DaveDashboardMavController.Mode.EDITING);
        return mav;
    }

    /**
     * Set the source LRS for an existing Dashboard
     *
     * @param dashboardId UUID of the associated Dashboard
     * @param lrsId       UUID of the {@link LrsConnection} to add to the Dashboard
     * @param mode        -- Page mode, used for redirection
     */
    @PostMapping(DaveDashboardMavController.BASE_URL + "/new/source")
    public RedirectView selectLRS(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "lrs_id") UUID lrsId,
                                  DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveDashboardService.getDashboard(dashboardId);
        LrsConnection lrsConnection = this.lrsService.getConnection(lrsId);
        this.daveDashboardService.setDashboardSource(dashboard, lrsConnection);
        this.daveDashboardService.checkDashboardConfiguration(dashboard);
        attributes.addAttribute("flow", dashboardId.toString());
        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "./visualisations" : "../show");
    }
}
