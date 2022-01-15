package de.tudresden.inf.rn.xapi.datatools.datasim.controllers;

import de.tudresden.inf.rn.xapi.datatools.datasim.DatasimSimulationService;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimPersona;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimPersonaTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.rn.xapi.datatools.ui.IUIStep;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PersonaSettingFlowController implements IUIStep {

    private final DatasimSimulationService datasimSimulationService;

    @Override
    public String getName() {
        return "Select Persona";
    }

    @Override
    public Pattern getPathRegex() {
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit)/persona(/add)?$");
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new/persona")
    public ModelAndView showSelectPersona(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/persona");
        mav.addObject("flow", simulation.getId().toString());
        mav.addObject("personas", this.datasimSimulationService.getPersonasWithSelected(simulation));
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit/persona")
    public ModelAndView showEditSelectPersona(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSelectPersona(simulationId);
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/persona/add")
    public RedirectView addPersonaToSimulation(@RequestParam(name = "flow") UUID simulationId, DatasimPersonaTO persona,
                                               DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        DatasimPersona created = this.datasimSimulationService.createPersona(persona.toNewDatasimPersona());
        this.datasimSimulationService.addPersonaToSimulation(simulation, created);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "../persona" : "../../edit/persona");
    }

    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/persona")
    public RedirectView selectPersona(@RequestParam(name = "flow") UUID simulationId,
                                      @RequestParam("persona_id") Set<UUID> personaIds,
                                      DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        Set<DatasimPersona> personae = personaIds.stream().map(this.datasimSimulationService::getPersona).collect(Collectors.toSet());
        this.datasimSimulationService.setPersonaeOfSimulation(simulation, personae);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "./alignment" : "../show");
    }
}
