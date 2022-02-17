package de.tudresden.inf.verdatas.xapitools.dave.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.DaveAnalysisService;
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

@Controller
@Order(2)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LRSSettingFlowController implements AnalysisStep{
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
        return Pattern.compile(BASE_URL + "/(new|edit)/source$");
    }

    @GetMapping(BASE_URL + "/new/source")
    public ModelAndView showSelectLRS(@RequestParam(name = "flow") UUID dashboardId) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/source");
        mav.addObject("activeLrs", this.lrsService.getConnections(true));
        mav.addObject("flow", dashboardId.toString());
        mav.addObject("mode", DaveAnalysisMavController.Mode.CREATING);
        return mav;
    }

    @GetMapping(BASE_URL + "/edit/source")
    public ModelAndView showEditLRS(@RequestParam(name = "flow") UUID dashboardId) {
        ModelAndView mav = this.showSelectLRS(dashboardId);
        mav.addObject("mode", DaveAnalysisMavController.Mode.EDITING);
        return mav;
    }

    @PostMapping(BASE_URL + "/new/source")
    public RedirectView selectLRS(@RequestParam(name = "flow") UUID dashboardId, @RequestParam(name = "lrs_id") UUID lrsId,
                                  DaveAnalysisMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = this.daveAnalysisService.getDashboard(dashboardId);
        LrsConnection lrsConnection = this.lrsService.getConnection(lrsId);
        this.daveAnalysisService.setDashboardSource(dashboard, lrsConnection);
        attributes.addAttribute("flow", dashboard.getId());
        return new RedirectView(DaveAnalysisMavController.Mode.CREATING.equals(mode) ? "./visualisations" : "../show");
    }
}
