package de.tudresden.inf.rn.xapi.datatools.datasim;

import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.*;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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
    public RedirectView selectProfile(@RequestParam(name = "flow") UUID simulationId,
                                      @RequestParam(name = "profile_id") UUID profileId, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        this.datasimSimulationService.updateSimulationProfile(simulation, this.datasimSimulationService.getProfile(profileId));
        attributes.addAttribute("flow", simulation.getId());
        return new RedirectView("./persona");
    }

    @GetMapping("/new/persona")
    public ModelAndView showSelectPersona(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/persona");
        mav.addObject("flow", simulation.getId().toString());
        List<Map.Entry<DatasimPersonaTO, Boolean>> payload = new ArrayList<>(this.datasimSimulationService.getPersonasWithSelected(simulation).entrySet());
        payload.sort(Comparator.comparing(entry -> entry.getKey().getId().toString()));
        mav.addObject("personas", payload);
        return mav;
    }

    @PostMapping("/new/persona/add")
    public RedirectView addPersonaToSimulation(@RequestParam(name = "flow") UUID simulationId, DatasimPersonaTO persona, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        DatasimPersona created = this.datasimSimulationService.createPersona(persona);
        this.datasimSimulationService.addPersonaToSimulation(simulation, created);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView("../persona");
    }

    @PostMapping("/new/persona")
    public RedirectView selectPersona(@RequestParam(name = "flow") UUID simulationId,
                                      @RequestParam("persona_id") Set<UUID> personaIds, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        Set<DatasimPersona> personae = personaIds.stream().map(this.datasimSimulationService::getPersona).collect(Collectors.toSet());
        this.datasimSimulationService.setPersonaeOfSimulation(simulation, personae);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView("./alignment");
    }

    @GetMapping("/new/alignment")
    public ModelAndView showCreateAlignments(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/alignment");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("alignments", DatasimSimulationService.getComponentAlignsByUrl(simulation.getAlignments()));
        return mav;
    }

    @PostMapping("/new/alignment/add")
    public RedirectView addComponentToSimulation(@RequestParam(name = "flow") UUID simulationId,
                                                 @RequestParam(name = "component") URL componentUrl, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        this.datasimSimulationService.addComponentToSimulationWithNeutralWeight(simulation, componentUrl);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView("../alignment");
    }

    @PostMapping("/new/alignment/delete")
    public RedirectView removeComponentFromSimulation(@RequestParam(name = "flow") UUID simulationId,
                                                      @RequestParam(name = "component") URL componentUrl, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        this.datasimSimulationService.removeComponentFromSimulation(simulation, componentUrl);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView("../alignment");
    }

    @PostMapping("/new/alignment")
    public RedirectView createAlignments(@RequestParam(name = "flow") UUID simulationId,
                                         @RequestParam Map<String, String> componentAligns, RedirectAttributes attributes) {
        // TODO: Map<String, String> maps all attributes. This could be considered technical debt.
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
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
        return new RedirectView("./parameters");
    }

    @GetMapping("/new/parameters")
    public ModelAndView showSetSimulationParameters(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/parameters");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("parameters", DatasimSimulationParamsTO.of(simulation.getParameters()));
        return mav;
    }

    @PostMapping("/new/parameters")
    public RedirectView setSimulationParameters(@RequestParam(name = "flow") UUID simulationId,
                                                DatasimSimulationParamsTO simulationParams,
                                                TimeZone userTimezone, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        // This can't be mapped automagically
        simulationParams.setTimezone(userTimezone.toZoneId());
        this.datasimSimulationService.setSimulationParams(simulation, simulationParams.toExistingSimulationParams());
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView("./parameters");
    }
}
