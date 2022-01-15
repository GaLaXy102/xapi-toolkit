package de.tudresden.inf.rn.xapi.datatools.datasim.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import de.tudresden.inf.rn.xapi.datatools.datasim.DatasimConnector;
import de.tudresden.inf.rn.xapi.datatools.datasim.DatasimResultService;
import de.tudresden.inf.rn.xapi.datatools.datasim.DatasimSimulationService;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.ActorWithAlignmentsTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimPersona;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimPersonaGroupTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimPersonaTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimProfileTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulationParamsTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulationTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.validators.Finalized;
import de.tudresden.inf.rn.xapi.datatools.ui.IUIFlow;
import de.tudresden.inf.rn.xapi.datatools.ui.IUIStep;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
@Validated
public class DatasimSimulationMavController implements IUIFlow {

    enum Mode {
        CREATING,
        EDITING,
        DISPLAYING
    }

    private final DatasimSimulationService datasimSimulationService;
    private final DatasimResultService datasimResultService;
    private final DatasimConnector datasimConnector;
    static final String BASE_URL = "/ui/datasim";
    private final List<IUIStep> children;

    public DatasimSimulationMavController(DatasimSimulationService datasimSimulationService,
                                          DatasimResultService datasimResultService, DatasimConnector datasimConnector,
                                          RemarkSettingFlowController remarkSettingFlowController,
                                          ProfileSettingFlowController profileSettingFlowController,
                                          PersonaSettingFlowController personaSettingFlowController,
                                          AlignmentSettingFlowController alignmentSettingFlowController,
                                          ParameterSettingFlowController parameterSettingFlowController) {
        this.datasimSimulationService = datasimSimulationService;
        this.datasimResultService = datasimResultService;
        this.datasimConnector = datasimConnector;
        this.children = List.of(
                remarkSettingFlowController,
                profileSettingFlowController,
                personaSettingFlowController,
                alignmentSettingFlowController,
                parameterSettingFlowController
        );
    }

    @Override
    public String getName() {
        return "Simulations";
    }

    @Override
    public String getEntrypoint() {
        return BASE_URL + "/show";
    }

    @Override
    public List<IUIStep> getSteps() {
        return this.children;
    }

    @GetMapping(BASE_URL + "/show")
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
                            simulation.getAlignments().stream().map(ActorWithAlignmentsTO::getAlignments).map(Set::size).map(Long::valueOf).reduce(Long::sum).orElse(0L)
                    );
                }
        );
        mav.addObject("simulations", simulations);
        mav.addObject("numPersonae", numPersonae);
        mav.addObject("numAligns", numAligns);
        mav.addObject("resultsList", this.datasimResultService.getSimulationsWithResultAvailable());
        return mav;
    }

    @PostMapping(BASE_URL + "/finalize")
    public RedirectView finalizeSimulation(@RequestParam(name = "flow") UUID simulationId, HttpServletRequest request) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        this.datasimSimulationService.finalizeSimulation(simulation);
        return new RedirectView(Objects.requireNonNullElse(request.getHeader("Referer"), "./show"));
    }

    @PostMapping(BASE_URL + "/delete")
    public RedirectView deleteSimulation(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        this.datasimSimulationService.deleteSimulation(simulation);
        return new RedirectView("./show");
    }

    @PostMapping(BASE_URL + "/copy")
    public RedirectView copySimulation(@RequestParam(name = "flow") UUID simulationId, RedirectAttributes attributes) {
        DatasimSimulation existing = this.datasimSimulationService.getSimulation(simulationId);
        DatasimSimulation copy = this.datasimSimulationService.copySimulation(existing);
        attributes.addAttribute("flow", copy.getId().toString());
        return new RedirectView("./show");
    }

    @PostMapping(BASE_URL + "/perform")
    public RedirectView performSimulation(@RequestParam(name = "flow") UUID simulationId, HttpServletRequest request) {
        @Finalized DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        List<JsonNode> result = this.datasimConnector.sendSimulation(DatasimSimulationTO.of(simulation).forExport());
        this.datasimResultService.saveSimulationResult(simulation, result);
        return new RedirectView(Objects.requireNonNullElse(request.getHeader("Referer"), "./show"));
    }
}
