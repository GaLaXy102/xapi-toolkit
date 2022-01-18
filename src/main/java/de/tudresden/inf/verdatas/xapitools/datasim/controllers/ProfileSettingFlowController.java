package de.tudresden.inf.verdatas.xapitools.datasim.controllers;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimSimulationService;
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

@Controller
@Order(2)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class ProfileSettingFlowController implements SimulationStep {

    private final DatasimSimulationService datasimSimulationService;

    @Override
    public String getName() {
        return "Select Profile";
    }

    @Override
    public Pattern getPathRegex() {
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit)/profile$");
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new/profile")
    public ModelAndView showSelectProfile(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/profiles");
        mav.addObject("profiles", this.datasimSimulationService.getProfiles().map(DatasimProfileTO::of));
        mav.addObject("flow", simulationId.toString());
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit/profile")
    public ModelAndView showEditProfile(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSelectProfile(simulationId);
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

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
