package de.tudresden.inf.verdatas.xapitools.datasim.controllers;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimSimulationService;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimAlignment;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimProfileTO;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * ModelAndView Controller for Management of {@link DatasimAlignment}s
 * By implementing {@link SimulationStep}, it is automatically bound in {@link DatasimSimulationMavController}.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
@Order(4)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class AlignmentSettingFlowController implements SimulationStep {

    private final DatasimSimulationService datasimSimulationService;

    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    @Override
    public String getName() {
        return "Create alignments";
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
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit|show)/alignment(/add|/delete)?$");
    }

    /**
     * Show page to create Alignments
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new/alignment")
    public ModelAndView showCreateAlignments(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/alignment");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("alignments", DatasimSimulationService.getComponentAlignsByUrl(simulation.getAlignments()));
        mav.addObject("possibleAlignments", DatasimProfileTO.of(simulation.getProfiles().get(0)).getPossibleAlignmentsByType());
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    /**
     * Show page to edit existing Alignments
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit/alignment")
    public ModelAndView showEditAlignments(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showCreateAlignments(simulationId);
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

    /**
     * Show page to show existing Alignments
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/show/alignment")
    public ModelAndView showDisplayAlignments(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/alignment");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("alignments", DatasimSimulationService.getComponentAlignsByUrl(simulation.getAlignments()));
        mav.addObject("mode", DatasimSimulationMavController.Mode.DISPLAYING);
        return mav;
    }

    /**
     * Add a new neutral Alignment to the Simulation
     *
     * @param simulationId UUID of associated Simulation
     * @param componentUrl Identifier of the Component to align to
     * @param mode         -- Page mode, used for redirection
     * @param attributes   -- Autowired by Spring, used for redirection
     */
    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/alignment/add")
    public RedirectView addComponentToSimulation(@RequestParam(name = "flow") UUID simulationId,
                                                 @RequestParam(name = "component") URL componentUrl,
                                                 DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        this.datasimSimulationService.addComponentToSimulationWithNeutralWeight(simulation, componentUrl);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "../alignment" : "../../edit/alignment");
    }

    /**
     * Remove a Component with all Alignments from the Simulation
     *
     * @param simulationId UUID of associated Simulation
     * @param componentUrl Identifier of the Component to align to
     * @param mode         -- Page mode, used for redirection
     * @param attributes   -- Autowired by Spring, used for redirection
     */
    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/alignment/delete")
    public RedirectView removeComponentFromSimulation(@RequestParam(name = "flow") UUID simulationId,
                                                      @RequestParam(name = "component") URL componentUrl,
                                                      DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        this.datasimSimulationService.removeComponentFromSimulation(simulation, componentUrl);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "../alignment" : "../../edit/alignment");
    }

    /**
     * Update Simulation from received alignments
     *
     * @param simulationId    UUID of associated Simulation
     * @param componentAligns Mapping personaID@componentUrl -> weight
     * @param mode            -- Page mode, used for redirection
     * @param attributes      -- Autowired by Spring, used for redirection
     */
    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/alignment")
    public RedirectView createAlignments(@RequestParam(name = "flow") UUID simulationId,
                                         @RequestParam Map<String, String> componentAligns,
                                         DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        // TODO: Map<String, String> maps all attributes. This could be considered technical debt.
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        componentAligns.entrySet().stream()
                .filter((entry) -> entry.getKey().contains("@"))
                .map((entry) -> Pair.of(entry.getKey().split("@", 2), Float.parseFloat(entry.getValue())))
                .map((pair) -> {
                    try {
                        return Pair.of(
                                DatasimSimulationService.getAlignment(
                                        simulation,
                                        new URL(pair.getFirst()[1]),
                                        this.datasimSimulationService.getPersona(UUID.fromString(pair.getFirst()[0]))),
                                pair.getSecond());
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException("Bad component URL");
                    }
                })
                .forEach((pair) -> this.datasimSimulationService.setAlignmentWeight(pair.getFirst(), pair.getSecond()));
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "./parameters" : "../show");
    }
}
