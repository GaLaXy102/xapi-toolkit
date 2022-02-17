package de.tudresden.inf.verdatas.xapitools.dave.controllers;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimSimulationService;
import de.tudresden.inf.verdatas.xapitools.datasim.controllers.DatasimSimulationMavController;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimProfileTO;
import de.tudresden.inf.verdatas.xapitools.dave.DaveAnalysisService;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnector;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedList;
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
        return Pattern.compile(BASE_URL + "/(new|edit)/visualisations$");
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
        mav.addObject("mode", DaveAnalysisMavController.Mode.CREATING);
        return mav;
    }
}
