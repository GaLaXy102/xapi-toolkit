package de.tudresden.inf.verdatas.xapitools.datasim.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import de.tudresden.inf.verdatas.xapitools.datasim.DatasimConnector;
import de.tudresden.inf.verdatas.xapitools.datasim.DatasimResultService;
import de.tudresden.inf.verdatas.xapitools.datasim.DatasimSimulationService;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.ActorWithAlignmentsTO;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimPersonaGroupTO;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulationTO;
import de.tudresden.inf.verdatas.xapitools.datasim.validators.Finalized;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnectionTO;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsService;
import de.tudresden.inf.verdatas.xapitools.ui.BasepageMavController;
import de.tudresden.inf.verdatas.xapitools.ui.BootstrapUIIcon;
import de.tudresden.inf.verdatas.xapitools.ui.IUIFlow;
import de.tudresden.inf.verdatas.xapitools.ui.UIIcon;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
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

/**
 * ModelAndView Controller for the Datasim Simulation Application
 * By implementing {@link IUIFlow}, it is bound automatically to the main UI in {@link BasepageMavController}.
 * It contains all Views that are not part of {@link SimulationStep}s.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
@Order(1)
@Validated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimSimulationMavController implements IUIFlow {

    /**
     * UI helper enum, controls button states
     */
    enum Mode {
        CREATING,
        EDITING,
        DISPLAYING
    }

    private final DatasimSimulationService datasimSimulationService;
    private final DatasimResultService datasimResultService;
    private final DatasimConnector datasimConnector;
    private final LrsService lrsService;
    private final List<SimulationStep> children;

    static final String BASE_URL = "/ui/datasim";

    /**
     * Get the Human readable name of this sub-application.
     *
     * @return Name of sub-application
     */
    @Override
    public String getName() {
        return "Simulations";
    }

    /**
     * Get the URL where the sub-application can be started.
     *
     * @return URL for Application Launch
     */
    @Override
    public String getEntrypoint() {
        return BASE_URL + "/show";
    }

    /**
     * Get all Steps belonging to the sub-application, so they can be displayed alongside the Launcher.
     *
     * @return List of sub-app Steps
     */
    @Override
    public List<SimulationStep> getSteps() {
        return this.children;
    }

    /**
     * Get the Icon for this UI Element
     *
     * @return Icon entity
     */
    @Override
    public UIIcon getIcon() {
        return BootstrapUIIcon.SHUFFLE;
    }

    /**
     * Show a List with all Simulations, or only a specific one.
     *
     * @param simulationId Optional: Filter for a specific Simulation ID
     */
    @GetMapping(BASE_URL + "/show")
    public ModelAndView showDetail(@RequestParam(name = "flow") Optional<UUID> simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/detail");
        // Prepare and collect visualisation helpers
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

    /**
     * Finalize a Simulation
     *
     * @param simulationId UUID of the Simulation to finalize
     * @param request      -- Autowired by Spring, used for redirection to source page
     */
    @PostMapping(BASE_URL + "/finalize")
    public RedirectView finalizeSimulation(@RequestParam(name = "flow") UUID simulationId, HttpServletRequest request) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        this.datasimSimulationService.finalizeSimulation(simulation);
        return new RedirectView(Objects.requireNonNullElse(request.getHeader("Referer"), "./show"));
    }

    /**
     * Delete a Simulation
     *
     * @param simulationId UUID of the Simulation to delete
     */
    @PostMapping(BASE_URL + "/delete")
    public RedirectView deleteSimulation(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        this.datasimSimulationService.deleteSimulation(simulation);
        return new RedirectView("./show");
    }

    /**
     * Copy a Simulation
     *
     * @param simulationId UUID of the Simulation to copy
     * @param attributes   -- Autowired by Spring, used for redirection to source page
     */
    @PostMapping(BASE_URL + "/copy")
    public RedirectView copySimulation(@RequestParam(name = "flow") UUID simulationId, RedirectAttributes attributes) {
        DatasimSimulation existing = this.datasimSimulationService.getSimulation(simulationId);
        DatasimSimulation copy = this.datasimSimulationService.copySimulation(existing);
        attributes.addAttribute("flow", copy.getId().toString());
        return new RedirectView("./show");
    }

    /**
     * Perform a Simulation by sending it to DATASIM and persisting the result
     *
     * @param simulationId UUID of the Simulation to perform
     * @param request      -- Autowired by Spring, used for redirection to source page
     */
    @PostMapping(BASE_URL + "/perform")
    public RedirectView performSimulation(@RequestParam(name = "flow") UUID simulationId, HttpServletRequest request) {
        @Finalized DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        List<JsonNode> result = this.datasimConnector.sendSimulation(DatasimSimulationTO.of(simulation).forExport());
        this.datasimResultService.saveSimulationResult(simulation, result);
        return new RedirectView(Objects.requireNonNullElse(request.getHeader("Referer"), "./show"));
    }

    /**
     * Push a Simulation result to an LRS
     *
     * @param simulationId UUID of the Simulation to finalize
     * @param lrsId        UUID of the LRS to push the result to
     * @param request      -- Autowired by Spring, used for redirection to source page
     * @param attributes   -- Autowired by Spring, used for adding the pushed statement count to the page
     */
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
