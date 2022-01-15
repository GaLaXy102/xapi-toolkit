package de.tudresden.inf.rn.xapi.datatools.datasim.controllers;

import de.tudresden.inf.rn.xapi.datatools.datasim.DatasimSimulationService;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.rn.xapi.datatools.ui.IUIStep;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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

@Controller
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AlignmentSettingFlowController implements IUIStep {
    
    private final DatasimSimulationService datasimSimulationService;

    @Override
    public String getName() {
        return "Create Alignments";
    }

    @Override
    public Pattern getPathRegex() {
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit|show)/alignment(/add|/delete)?$");
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new/alignment")
    public ModelAndView showCreateAlignments(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/alignment");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("alignments", DatasimSimulationService.getComponentAlignsByUrl(simulation.getAlignments()));
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit/alignment")
    public ModelAndView showEditAlignments(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showCreateAlignments(simulationId);
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/show/alignment")
    public ModelAndView showDisplayAlignments(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/alignment");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("alignments", DatasimSimulationService.getComponentAlignsByUrl(simulation.getAlignments()));
        mav.addObject("mode", DatasimSimulationMavController.Mode.DISPLAYING);
        return mav;
    }

    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/alignment/add")
    public RedirectView addComponentToSimulation(@RequestParam(name = "flow") UUID simulationId,
                                                 @RequestParam(name = "component") URL componentUrl,
                                                 DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        this.datasimSimulationService.addComponentToSimulationWithNeutralWeight(simulation, componentUrl);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "../alignment" : "../../edit/alignment");
    }

    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/alignment/delete")
    public RedirectView removeComponentFromSimulation(@RequestParam(name = "flow") UUID simulationId,
                                                      @RequestParam(name = "component") URL componentUrl,
                                                      DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        this.datasimSimulationService.removeComponentFromSimulation(simulation, componentUrl);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "../alignment" : "../../edit/alignment");
    }

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
