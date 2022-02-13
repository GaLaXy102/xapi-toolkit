package de.tudresden.inf.verdatas.xapitools.datasim.controllers;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimSimulationService;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;
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

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * ModelAndView Controller for Setting of a Simulation's Remark
 * By implementing {@link SimulationStep}, it is automatically bound in {@link DatasimSimulationMavController}.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
@Order(1)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class RemarkSettingFlowController implements SimulationStep {

    private final DatasimSimulationService datasimSimulationService;

    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    @Override
    public String getName() {
        return "Set title";
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
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit)$");
    }

    /**
     * Show page to set the Remark of a new Simulation
     *
     * @param simulationId Optionally the UUID of the current Simulation in creation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new")
    public ModelAndView showSetRemark(@RequestParam(name = "flow") Optional<UUID> simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/remark");
        mav.addObject("simulationRemark",
                simulationId
                        .map(this.datasimSimulationService::getUnfinalizedSimulation)
                        .map(DatasimSimulation::getRemark)
                        .orElse("")
        );
        simulationId.ifPresent((id) -> mav.addObject("flow", id.toString()));
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    /**
     * Show page to set the Remark of an existing Simulation
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit")
    public ModelAndView showEditRemark(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSetRemark(Optional.of(simulationId));
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

    /**
     * Set the Remark of a Simulation. Creates a new Simulation if simulationId is empty.
     *
     * @param simulationId Optionally the UUID of the current Simulation to set the Remark of. When missing, a new Simulation is drafted.
     * @param remark       Remark to set
     * @param mode         -- Page mode, used for redirection
     * @param attributes   -- Autowired by Spring, used for redirection
     */
    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new")
    public RedirectView setRemarkAndCreate(@RequestParam(name = "flow") Optional<UUID> simulationId, String remark,
                                           DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = simulationId
                .map(this.datasimSimulationService::getUnfinalizedSimulation)
                .orElseGet(this.datasimSimulationService::createEmptySimulation);
        this.datasimSimulationService.setSimulationRemark(simulation, remark);
        attributes.addAttribute("flow", simulation.getId());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "./new/profile" : "./show");
    }
}
