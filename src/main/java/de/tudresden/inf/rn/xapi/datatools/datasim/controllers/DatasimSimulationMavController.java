package de.tudresden.inf.rn.xapi.datatools.datasim.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import de.tudresden.inf.rn.xapi.datatools.datasim.DatasimConnector;
import de.tudresden.inf.rn.xapi.datatools.datasim.DatasimResultService;
import de.tudresden.inf.rn.xapi.datatools.datasim.DatasimSimulationService;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.ActorWithAlignmentsTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimPersonaGroupTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulationTO;
import de.tudresden.inf.rn.xapi.datatools.datasim.validators.Finalized;
import de.tudresden.inf.rn.xapi.datatools.lrs.LrsConnection;
import de.tudresden.inf.rn.xapi.datatools.lrs.LrsConnectionTO;
import de.tudresden.inf.rn.xapi.datatools.lrs.LrsService;
import de.tudresden.inf.rn.xapi.datatools.lrs.connector.LrsConnector;
import de.tudresden.inf.rn.xapi.datatools.ui.BootstrapUIIcon;
import de.tudresden.inf.rn.xapi.datatools.ui.IUIFlow;
import de.tudresden.inf.rn.xapi.datatools.ui.IUIStep;
import de.tudresden.inf.rn.xapi.datatools.ui.UIIcon;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@Order(1)
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
    private final LrsService lrsService;
    static final String BASE_URL = "/ui/datasim";
    private final List<IUIStep> children;

    public DatasimSimulationMavController(DatasimSimulationService datasimSimulationService,
                                          DatasimResultService datasimResultService, DatasimConnector datasimConnector,
                                          LrsService lrsService, List<SimulationStep> childControllers) {
        this.datasimSimulationService = datasimSimulationService;
        this.datasimResultService = datasimResultService;
        this.datasimConnector = datasimConnector;
        this.lrsService = lrsService;
        // This Type Conversion is safe as SimulationStep extends IUIStep
        this.children = childControllers.stream().map((step) -> (IUIStep) step).toList();
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

    @Override
    public UIIcon getIcon() {
        return BootstrapUIIcon.SHUFFLE;
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
        List<LrsConnectionTO> availableLrs = this.lrsService.getConnections(true)
                .stream()
                .sorted(Comparator.comparing(LrsConnection::getFriendlyName))
                .map(LrsConnectionTO::of)
                .toList();
        mav.addObject("simulations", simulations);
        mav.addObject("numPersonae", numPersonae);
        mav.addObject("numAligns", numAligns);
        mav.addObject("resultsList", this.datasimResultService.getSimulationsWithResultAvailable());
        mav.addObject("availableLrs", availableLrs);
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

    @PostMapping(BASE_URL + "/push")
    public RedirectView sendSimulation(@RequestParam(name = "flow") UUID simulationId, @RequestParam(name = "lrs_id") UUID lrsId,
                                       HttpServletRequest request, RedirectAttributes attributes) {
        @Finalized DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        LrsConnection lrsConnection = this.lrsService.getConnection(lrsId);
        List<JsonNode> simulationResult = this.datasimResultService.getSimulationResult(simulation);
        this.lrsService.sendStatements(simulationResult, lrsConnection);
        attributes.addAttribute("pushSuccess", true);
        return new RedirectView(Objects.requireNonNullElse(request.getHeader("Referer"), "./show"));
    }
}
