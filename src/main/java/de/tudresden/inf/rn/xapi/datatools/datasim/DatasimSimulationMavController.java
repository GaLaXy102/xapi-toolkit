package de.tudresden.inf.rn.xapi.datatools.datasim;

import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimPersona;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimPersonaGroupTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimPersonaTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimProfileTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulationParamsTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulationTO;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ui/datasim")
public class DatasimSimulationMavController {

    enum Mode {
        CREATING,
        EDITING
    }

    private final DatasimSimulationService datasimSimulationService;

    public DatasimSimulationMavController(DatasimSimulationService datasimSimulationService) {
        this.datasimSimulationService = datasimSimulationService;
    }

    @GetMapping("/new")
    public ModelAndView showSetRemark(@RequestParam(name = "flow") Optional<UUID> simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/remark");
        mav.addObject("simulationRemark",
                simulationId
                        .map(this.datasimSimulationService::getUnfinalizedSimulation)
                        .map(DatasimSimulation::getRemark)
                        .orElse("")
        );
        simulationId.ifPresent((id) -> mav.addObject("flow", id.toString()));
        mav.addObject("mode", Mode.CREATING);
        return mav;
    }

    @GetMapping("/edit")
    public ModelAndView showEditRemark(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSetRemark(Optional.of(simulationId));
        mav.addObject("mode", Mode.EDITING);
        return mav;
    }

    @PostMapping("/new")
    public RedirectView setRemarkAndCreate(@RequestParam(name = "flow") Optional<UUID> simulationId, String remark, Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = simulationId
                .map(this.datasimSimulationService::getUnfinalizedSimulation)
                .orElseGet(this.datasimSimulationService::createEmptySimulation);
        this.datasimSimulationService.setSimulationRemark(simulation, remark);
        attributes.addAttribute("flow", simulation.getId());
        return new RedirectView(Mode.CREATING.equals(mode) ? "./new/profile" : "./show");
    }

    @GetMapping("/new/profile")
    public ModelAndView showSelectProfile(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/profiles");
        mav.addObject("profiles", this.datasimSimulationService.getProfiles().map(DatasimProfileTO::of));
        mav.addObject("flow", simulationId.toString());
        mav.addObject("mode", Mode.CREATING);
        return mav;
    }

    @GetMapping("/edit/profile")
    public ModelAndView showEditProfile(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSelectProfile(simulationId);
        mav.addObject("mode", Mode.EDITING);
        return mav;
    }

    @PostMapping("/new/profile")
    public RedirectView selectProfile(@RequestParam(name = "flow") UUID simulationId,
                                      @RequestParam(name = "profile_id") UUID profileId,
                                      Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        this.datasimSimulationService.updateSimulationProfile(simulation, this.datasimSimulationService.getProfile(profileId));
        attributes.addAttribute("flow", simulation.getId());
        return new RedirectView(Mode.CREATING.equals(mode) ? "./persona" : "../show");
    }

    @GetMapping("/new/persona")
    public ModelAndView showSelectPersona(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/persona");
        mav.addObject("flow", simulation.getId().toString());
        List<Map.Entry<DatasimPersonaTO, Boolean>> payload = new ArrayList<>(this.datasimSimulationService.getPersonasWithSelected(simulation).entrySet());
        payload.sort(Comparator.comparing(entry -> entry.getKey().getId().toString()));
        mav.addObject("personas", payload);
        mav.addObject("mode", Mode.CREATING);
        return mav;
    }

    @GetMapping("/edit/persona")
    public ModelAndView showEditSelectPersona(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSelectPersona(simulationId);
        mav.addObject("mode", Mode.EDITING);
        return mav;
    }

    @PostMapping("/new/persona/add")
    public RedirectView addPersonaToSimulation(@RequestParam(name = "flow") UUID simulationId, DatasimPersonaTO persona,
                                               Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        DatasimPersona created = this.datasimSimulationService.createPersona(persona.toNewDatasimPersona());
        this.datasimSimulationService.addPersonaToSimulation(simulation, created);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(Mode.CREATING.equals(mode) ? "../persona" : "../../edit/persona");
    }

    @PostMapping("/new/persona")
    public RedirectView selectPersona(@RequestParam(name = "flow") UUID simulationId,
                                      @RequestParam("persona_id") Set<UUID> personaIds,
                                      Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        Set<DatasimPersona> personae = personaIds.stream().map(this.datasimSimulationService::getPersona).collect(Collectors.toSet());
        this.datasimSimulationService.setPersonaeOfSimulation(simulation, personae);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(Mode.CREATING.equals(mode) ? "./alignment" : "../show");
    }

    @GetMapping("/new/alignment")
    public ModelAndView showCreateAlignments(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/alignment");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("alignments", DatasimSimulationService.getComponentAlignsByUrl(simulation.getAlignments()));
        mav.addObject("mode", Mode.CREATING);
        return mav;
    }

    @GetMapping("/edit/alignment")
    public ModelAndView showEditAlignments(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showCreateAlignments(simulationId);
        mav.addObject("mode", Mode.EDITING);
        return mav;
    }

    @PostMapping("/new/alignment/add")
    public RedirectView addComponentToSimulation(@RequestParam(name = "flow") UUID simulationId,
                                                 @RequestParam(name = "component") URL componentUrl,
                                                 Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        this.datasimSimulationService.addComponentToSimulationWithNeutralWeight(simulation, componentUrl);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(Mode.CREATING.equals(mode) ? "../alignment" : "../../edit/alignment");
    }

    @PostMapping("/new/alignment/delete")
    public RedirectView removeComponentFromSimulation(@RequestParam(name = "flow") UUID simulationId,
                                                      @RequestParam(name = "component") URL componentUrl,
                                                      Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        this.datasimSimulationService.removeComponentFromSimulation(simulation, componentUrl);
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(Mode.CREATING.equals(mode) ? "../alignment" : "../../edit/alignment");
    }

    @PostMapping("/new/alignment")
    public RedirectView createAlignments(@RequestParam(name = "flow") UUID simulationId,
                                         @RequestParam Map<String, String> componentAligns,
                                         Mode mode, RedirectAttributes attributes) {
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
        return new RedirectView(Mode.CREATING.equals(mode) ? "./parameters" : "../show");
    }

    @GetMapping("/new/parameters")
    public ModelAndView showSetSimulationParameters(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        ModelAndView mav = new ModelAndView("bootstrap/datasim/parameters");
        mav.addObject("flow", simulationId.toString());
        mav.addObject("parameters", DatasimSimulationParamsTO.of(simulation.getParameters()));
        mav.addObject("mode", Mode.CREATING);
        return mav;
    }

    @GetMapping("/edit/parameters")
    public ModelAndView showEditSimulationParameters(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSetSimulationParameters(simulationId);
        mav.addObject("mode", Mode.EDITING);
        return mav;
    }

    @PostMapping("/new/parameters")
    public RedirectView setSimulationParameters(@RequestParam(name = "flow") UUID simulationId,
                                                DatasimSimulationParamsTO simulationParams,
                                                TimeZone userTimezone,
                                                Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = this.datasimSimulationService.getUnfinalizedSimulation(simulationId);
        // This can't be mapped automagically
        simulationParams.setTimezone(userTimezone.toZoneId());
        this.datasimSimulationService.setSimulationParams(simulation, simulationParams.toExistingSimulationParams());
        attributes.addAttribute("flow", simulationId.toString());
        return new RedirectView(Mode.CREATING.equals(mode) ? "../show" : "../show");  // This is prepared to support different modes.
    }

    @GetMapping("/show")
    public ModelAndView showDetail(@RequestParam(name = "flow") Optional<UUID> simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/detail");
        Map<DatasimSimulationTO, Long> numPersonae = new HashMap<>();
        Map<DatasimSimulationTO, Long> numAligns = new HashMap<>();
        List<DatasimSimulationTO> simulations = simulationId
                .map(this.datasimSimulationService::getSimulation)
                .map(DatasimSimulationTO::of)
                .map(List::of)
                .orElseGet(
                        () -> this.datasimSimulationService.getAllSimulations()
                                .sorted(
                                        Comparator
                                                .comparing(DatasimSimulation::getRemark, Comparator.naturalOrder())
                                                .thenComparing(DatasimSimulation::getId, Comparator.naturalOrder())
                                )
                                .map(DatasimSimulationTO::of)
                                .toList()
                );
        simulations.forEach(
                (simulation) -> {
                    numPersonae.put(
                            simulation,
                            simulation.getPersonaGroups().stream().map(DatasimPersonaGroupTO::getMember).flatMap(Collection::stream).distinct().count()
                    );
                    numAligns.put(
                            simulation,
                            simulation.getAlignments().values().stream().map(Set::size).map(Long::valueOf).reduce(Long::sum).orElse(0L)
                    );
                }
        );
        mav.addObject("simulations", simulations);
        mav.addObject("numPersonae", numPersonae);
        mav.addObject("numAligns", numAligns);
        return mav;
    }

    @PostMapping("/finalize")
    public RedirectView finalizeSimulation(@RequestParam(name = "flow") UUID simulationId, HttpServletRequest request) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        this.datasimSimulationService.finalizeSimulation(simulation);
        return new RedirectView(request.getHeader("Referer"));
    }

    @PostMapping("/delete")
    public RedirectView deleteSimulation(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        this.datasimSimulationService.deleteSimulation(simulation);
        return new RedirectView("./show");
    }

    @PostMapping("/copy")
    public RedirectView copySimulation(@RequestParam(name = "flow") UUID simulationId, RedirectAttributes attributes) {
        DatasimSimulation existing = this.datasimSimulationService.getSimulation(simulationId);
        DatasimSimulation copy = this.datasimSimulationService.copySimulation(existing);
        attributes.addAttribute("flow", copy.getId().toString());
        return new RedirectView("./show");
    }
}
