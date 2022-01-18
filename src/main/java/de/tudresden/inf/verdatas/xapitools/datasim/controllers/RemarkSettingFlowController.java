package de.tudresden.inf.verdatas.xapitools.datasim.controllers;

import de.tudresden.inf.verdatas.xapitools.datasim.DatasimSimulationService;
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

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Controller
@Order(1)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class RemarkSettingFlowController implements SimulationStep {

    private final DatasimSimulationService datasimSimulationService;

    @Override
    public String getName() {
        return "Set Remark";
    }

    @Override
    public Pattern getPathRegex() {
        return Pattern.compile(DatasimSimulationMavController.BASE_URL + "/(new|edit)$");
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/new")
    public ModelAndView showSetRemark(@RequestParam(name = "flow") Optional<UUID> simulationId) {
        ModelAndView mav = new ModelAndView("bootstrap/datasim/remark");
        mav.addObject("simulationRemark",
                simulationId
                        .map(this.datasimSimulationService::getUnfinalizedSimulation)
                        .map(DatasimSimulation::getRemark)
                        .orElse("")
        );
        simulationId.ifPresent((id) -> mav.addObject("flow", id.toString()));
        mav.addObject("mode", DatasimSimulationMavController.Mode.CREATING);
        return mav;
    }

    @GetMapping(DatasimSimulationMavController.BASE_URL + "/edit")
    public ModelAndView showEditRemark(@RequestParam(name = "flow") UUID simulationId) {
        ModelAndView mav = this.showSetRemark(Optional.of(simulationId));
        mav.addObject("mode", DatasimSimulationMavController.Mode.EDITING);
        return mav;
    }

    @PostMapping(DatasimSimulationMavController.BASE_URL + "/new")
    public RedirectView setRemarkAndCreate(@RequestParam(name = "flow") Optional<UUID> simulationId, String remark, DatasimSimulationMavController.Mode mode, RedirectAttributes attributes) {
        DatasimSimulation simulation = simulationId
                .map(this.datasimSimulationService::getUnfinalizedSimulation)
                .orElseGet(this.datasimSimulationService::createEmptySimulation);
        this.datasimSimulationService.setSimulationRemark(simulation, remark);
        attributes.addAttribute("flow", simulation.getId());
        return new RedirectView(DatasimSimulationMavController.Mode.CREATING.equals(mode) ? "./new/profile" : "./show");
    }
}
