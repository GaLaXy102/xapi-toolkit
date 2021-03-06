package de.tudresden.inf.verdatas.xapitools.dave.dashboards.controllers;

import de.tudresden.inf.verdatas.xapitools.dave.dashboards.DaveDashboardService;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
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

/**
 * ModelAndView Controller for Setting of a Dashboards' Title
 * By implementing {@link DashboardStep}, it is automatically bound in {@link DaveDashboardMavController}.
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Controller
@Order(1)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class NameSettingFlowController implements DashboardStep {
    private final DaveDashboardService daveDashboardService;

    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    @Override
    public String getName() {
        return "Set title";
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
        return Pattern.compile(DaveDashboardMavController.BASE_URL + "/(new|edit)$");
    }

    /**
     * Show the page to set the Title of a Dashboard
     *
     * @param dashboardId Optionally the UUID of the current Dashboard in creation
     */
    @GetMapping(DaveDashboardMavController.BASE_URL + "/new")
    public ModelAndView showSetTitle(@RequestParam(name = "flow") Optional<UUID> dashboardId) {
        ModelAndView mav = new ModelAndView("bootstrap/dave/dashboard/identifier");
        mav.addObject("dashboardIdentifier",
                dashboardId
                        .map(this.daveDashboardService::getDashboard)
                        .map(DaveDashboard::getName)
                        .orElse("")
        );
        dashboardId.ifPresent((id) -> mav.addObject("flow", id.toString()));
        mav.addObject("mode", DaveDashboardMavController.Mode.CREATING);
        return mav;
    }

    /**
     * Show the page to edit the Title of an existing Dashboard
     *
     * @param dashboardId UUID of the Dashboard to modify
     */
    @GetMapping(DaveDashboardMavController.BASE_URL + "/edit")
    public ModelAndView showEditTitle(@RequestParam(name = "flow") UUID dashboardId) {
        ModelAndView mav = this.showSetTitle(Optional.of(dashboardId));
        mav.addObject("mode", DaveDashboardMavController.Mode.EDITING);
        return mav;
    }

    /**
     * Set the Title of a Dashboard. Creates a new Dashboard if {@param dashboardId} is empty
     *
     * @param dashboardId UUID of the Dashboard to modify. If missing a new Dashboard will be created
     * @param identifier  Title to set. Must not be already used
     * @param mode        -- Page mode, used for redirection
     */
    @PostMapping(DaveDashboardMavController.BASE_URL + "/new")
    public RedirectView setTitleAndCreate(@RequestParam(name = "flow") Optional<UUID> dashboardId, String identifier,
                                          DaveDashboardMavController.Mode mode, RedirectAttributes attributes) {
        DaveDashboard dashboard = dashboardId
                .map(this.daveDashboardService::getDashboard)
                .orElseGet(() -> this.daveDashboardService.createEmptyDashboard(identifier));
        this.daveDashboardService.setDashboardName(dashboard, identifier);
        this.daveDashboardService.checkDashboardConfiguration(dashboard);
        attributes.addAttribute("flow", dashboard.getId().toString());
        return new RedirectView(DaveDashboardMavController.Mode.CREATING.equals(mode) ? "./new/source" : "./show");
    }
}
