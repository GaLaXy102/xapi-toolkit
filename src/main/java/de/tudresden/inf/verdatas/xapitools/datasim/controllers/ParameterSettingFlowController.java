package de.tudresden.inf.verdatas.xapitools.datasim.controllers;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimSimulationService;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulationParams;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulationParamsTO;
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

import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * ModelAndView Controller for Management of {@link DatasimSimulationParams}
 * By implementing {@link SimulationStep}, it is automatically bound in {@link DatasimSimulationMavController}.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
@Order(5)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ParameterSettingFlowController implements SimulationStep {

    private final DatasimSimulationService datasimSimulationService;

    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    @Override
    public String getName() {
        return "Set parameters";
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
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit)/parameters$");
    }

    /**
     * Show page to set Parameters in CREATE mode.
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new/parameters")
    public ModelAndView showSetSimulationParameters(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/parameters");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("parameters", DatasimSimulationParamsTO.of(simulation.getParameters()));
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    /**
     * Show page to set Parameters in EDITING mode.
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit/parameters")
    public ModelAndView showEditSimulationParameters(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSetSimulationParameters(simulationId);
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

    /**
     * Update Simulation Parameters
     *
     * @param simulationId     UUID of associated Simulation
     * @param simulationParams Parameters to set
     * @param userTimezone     Timezone of the user, will be used as reference
     * @param mode             -- Page mode, used for redirection
     * @param attributes       -- Autowired by Spring, used for redirection
     */
    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/parameters")
    public RedirectView setSimulationParameters(@RequestParam(name = "flow") UUID simulationId,
                                                DatasimSimulationParamsTO simulationParams,
                                                TimeZone userTimezone,
                                                DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        // This can't be mapped automagically
        simulationParams.setTimezone(userTimezone.toZoneId());
        this.datasimSimulationService.setSimulationParams(simulation, simulationParams.toExistingSimulationParams());
        attributes.addAttribute("flow", simulationId.toString());
        // This is prepared to support different modes. Please ignore the IDE warning.
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "../show" : "../show");
    }
}
