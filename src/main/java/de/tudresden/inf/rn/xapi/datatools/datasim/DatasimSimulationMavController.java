package de.tudresden.inf.rn.xapi.datatools.datasim;

import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimProfileTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/ui/datasim")
public class DatasimSimulationMavController {

    private final DatasimSimulationService datasimSimulationService;

    public DatasimSimulationMavController(DatasimSimulationService datasimSimulationService) {
        this.datasimSimulationService = datasimSimulationService;
    }

    @GetMapping("/new")
    public ModelAndView showSetRemark(@RequestParam(name = "flow") Optional<UUID> simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/remark");
        mav.addObject("simulationRemark",
                simulationId
                        .map(this.datasimSimulationService::getSimulation)
                        .map(DatasimSimulation::getRemark)
                        .orElse("")
        );
        simulationId.ifPresent((id) -> mav.addObject("flow", id.toString()));
        return mav;
    }

    @PostMapping("/new")
    public RedirectView setRemarkAndCreate(@RequestParam(name = "flow") Optional<UUID> simulationId, String remark, RedirectAttributes attributes) {
        DatasimSimulation simulation = simulationId
                .map(this.datasimSimulationService::getSimulation)
                .orElseGet(this.datasimSimulationService::createEmptySimulation);
        this.datasimSimulationService.setSimulationRemark(simulation, remark);
        attributes.addAttribute("flow", simulation.getId());
        return new RedirectView("./new/profile");
    }

    @GetMapping("/new/profile")
    public ModelAndView showSelectProfile(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/profiles");
        mav.addObject("profiles", this.datasimSimulationService.getProfiles().map(DatasimProfileTO::of));
        mav.addObject("flow", simulationId.toString());
        return mav;
    }

    @PostMapping("/new/profile")
    public RedirectView selectProfile(@RequestParam(name = "profile_id") UUID profileId, RedirectAttributes attributes) {
        DatasimSimulation createdFlow = this.datasimSimulationService.createEmptySimulation();
        this.datasimSimulationService.updateSimulationProfile(createdFlow, this.datasimSimulationService.getProfile(profileId));
        attributes.addAttribute("flow", createdFlow.getId());
        return new RedirectView("./new/persona");
    }

    @GetMapping("/new/persona")
    public ModelAndView showSelectPersona(@RequestParam(name = "flow") UUID simulationId) {
        // TODO: Implement this.
        DatasimSimulation existingFlow = this.datasimSimulationService.getSimulation(simulationId);
        return new ModelAndView();
    }
}
