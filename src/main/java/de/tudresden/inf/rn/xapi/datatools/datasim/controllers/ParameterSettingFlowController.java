package de.tudresden.inf.rn.xapi.datatools.datasim.controllers;

import de.tudresden.inf.rn.xapi.datatools.datasim.DatasimSimulationService;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulationParamsTO;
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

@Controller
@Order(5)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ParameterSettingFlowController implements SimulationStep {

    private final DatasimSimulationService datasimSimulationService;

    @Override
    public String getName() {
        return "Set Parameters";
    }

    @Override
    public Pattern getPathRegex() {
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit)/parameters$");
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new/parameters")
    public ModelAndView showSetSimulationParameters(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/parameters");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("parameters", DatasimSimulationParamsTO.of(simulation.getParameters()));
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit/parameters")
    public ModelAndView showEditSimulationParameters(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSetSimulationParameters(simulationId);
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

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
