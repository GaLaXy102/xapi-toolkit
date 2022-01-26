package de.tudresden.inf.verdatas.xapitools.datasim.controllers;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimSimulationService;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimProfile;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimProfileTO;
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

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * ModelAndView Controller for Selection of {@link DatasimProfile}s
 * By implementing {@link SimulationStep}, it is automatically bound in {@link DatasimSimulationMavController}.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
@Order(2)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class ProfileSettingFlowController implements SimulationStep {

    private final DatasimSimulationService datasimSimulationService;

    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    @Override
    public String getName() {
        return "Select Profile";
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
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit)/profile$");
    }

    /**
     * Show page to select the profile of a new Simulation
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new/profile")
    public ModelAndView showSelectProfile(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/profiles");
        mav.addObject("profiles", this.datasimSimulationService.getProfiles().map(DatasimProfileTO::of).toList());
        mav.addObject("flow", simulationId.toString());
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    /**
     * Show page to edit the profile of an existing Simulation
     *
     * @param simulationId UUID of associated Simulation
     */
    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit/profile")
    public ModelAndView showEditProfile(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSelectProfile(simulationId);
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

    /**
     * Set a Simulation's Profile
     *
     * @param simulationId UUID of associated Simulation
     * @param profileId    UUID of Profile to set
     * @param mode         -- Page mode, used for redirection
     * @param attributes   -- Autowired by Spring, used for redirection
     */
    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new/profile")
    public RedirectView selectProfile(@RequestParam(name = "flow") UUID simulationId,
                                      @RequestParam(name = "profile_id") UUID profileId,
                                      DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        this.datasimSimulationService.updateSimulationProfile(simulation, this.datasimSimulationService.getProfile(profileId));
        attributes.addAttribute("flow", simulation.getId());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "./persona" : "../show");
    }
}
