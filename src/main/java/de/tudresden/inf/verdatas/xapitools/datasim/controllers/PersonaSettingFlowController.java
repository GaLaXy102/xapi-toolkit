package de.tudresden.inf.verdatas.xapitools.datasim.controllers;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimSimulationService;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimPersona;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimPersonaTO;
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

import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ModelAndView Controller for Management of {@link DatasimPersona}e
 * By implementing {@link SimulationStep}, it is automatically bound in {@link DatasimSimulationMavController}.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
@Order(3)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PersonaSettingFlowController implements SimulationStep {

    private final DatasimSimulationService datasimSimulationService;

    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    @Override
    public String getName() {
        return "Select Persona";
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
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit)/persona(/add)?$");
    }

    /**
     * Show page to create and select Personae of a new Simulation
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new/persona")
    public ModelAndView showSelectPersona(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/persona");
        mav.addObject("flow", simulation.getId().toString());
        mav.addObject("personas", this.datasimSimulationService.getPersonasWithSelected(simulation));
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    /**
     * Show page to create and select Personae of an existing Simulation
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit/persona")
    public ModelAndView showEditSelectPersona(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSelectPersona(simulationId);
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

    /**
     * Add Persona to Simulation
     *
     * @param simulationId UUID of associated Simulation
     * @param persona      persona data
     * @param mode         -- Page mode, used for redirection
     * @param attributes   -- Autowired by Spring, used for redirection
     */
    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/persona/add")
    public RedirectView addPersonaToSimulation(@RequestParam(name = "flow") UUID simulationId, DatasimPersonaTO persona,
                                               DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        DatasimPersona created = this.datasimSimulationService.createPersona(persona.toNewDatasimPersona());
        this.datasimSimulationService.addPersonaToSimulation(simulation, created);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "../persona" : "../../edit/persona");
    }

    /**
     * Select Personae of Simulation
     *
     * @param simulationId UUID of associated Simulation
     * @param personaIds   UUIDs of Personae to use in Simulation
     * @param mode         -- Page mode, used for redirection
     * @param attributes   -- Autowired by Spring, used for redirection
     */
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
