package de.tudresden.inf.verdatas.xapitools.lrs;

import de.tudresden.inf.verdatas.xapitools.ui.BootstrapUIIcon;
import de.tudresden.inf.verdatas.xapitools.ui.IUIManagementFlow;
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

import java.util.Optional;
import java.util.UUID;

/**
 * ModelAndView Controller for LRS Connection Management.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
@Order(1)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class LrsMavController implements IUIManagementFlow {
    private final LrsService lrsService;
    private static final String BASE_URL = "/ui/manage/lrs";

    /**
     * Get the Human readable name of this Setting.
     *
     * @return Name of Setting
     */
    @Override
    public String getName() {
        return "LRS Connections";
    }

    /**
     * Get the URL where the sub-application can be started.
     *
     * @return URL for Application Launch
     */
    @Override
    public String getEntrypoint() {
        return LrsMavController.BASE_URL + "/";
    }

    /**
     * Get the Icon for this UI Element
     *
     * @return Icon entity
     */
    @Override
    public UIIcon getIcon() {
        return BootstrapUIIcon.CLOUD;
    }

    /**
     * Show the page with all LRS Connections
     *
     * @param activeOnly only show enabled connections
     */
    @GetMapping(LrsMavController.BASE_URL + "/")
    public ModelAndView showLrsConnections(@RequestParam(name = "active_only") Optional<Boolean> activeOnly) {
        ModelAndView mav = new ModelAndView("bootstrap/lrs/show");
        mav.addObject("connections", this.lrsService.getConnections(activeOnly.orElse(true)).stream().map(LrsConnectionTO::of).toList());
        return mav;
    }

    /**
     * Deactivate an LRS Connection
     *
     * @param lrsUuid UUID of the Connection to deactivate
     */
    @PostMapping(LrsMavController.BASE_URL + "/deactivate")
    public RedirectView deleteLrsConnection(@RequestParam(name = "lrs_uuid") UUID lrsUuid, RedirectAttributes attributes) {
        this.lrsService.deactivateConnection(lrsUuid);
        attributes.addAttribute("deactivate", this.lrsService.getConnection(lrsUuid).getFriendlyName());
        return new RedirectView(".");
    }

    /**
     * Reactivate an LRS Connection
     *
     * @param lrsUuid UUID of the Connection to reactivate
     */
    @PostMapping(LrsMavController.BASE_URL + "/reactivate")
    public RedirectView reactivateLrsConnection(@RequestParam(name = "lrs_uuid") UUID lrsUuid, RedirectAttributes attributes) {
        this.lrsService.activateConnection(lrsUuid);
        attributes.addAttribute("reactivate", this.lrsService.getConnection(lrsUuid).getFriendlyName());
        return new RedirectView(".");
    }

    /**
     * Show the edit page for the given LRS Connection
     *
     * @param lrsUuid UUID of the Connection to edit
     */
    @GetMapping(LrsMavController.BASE_URL + "/edit")
    public ModelAndView showEditLrsConnection(@RequestParam(name = "lrs_uuid") UUID lrsUuid) {
        LrsConnection found = this.lrsService.getConnection(lrsUuid);
        ModelAndView mav = new ModelAndView("bootstrap/lrs/detail");
        mav.addObject("connection", LrsConnectionTO.of(found));
        mav.addObject("method", "edit");
        return mav;
    }

    /**
     * Handle the editing of LRS Connections
     *
     * @param data Transfer object containing the new details of the LRS Connection
     */
    @PostMapping(LrsMavController.BASE_URL + "/edit")
    public RedirectView editLrsConnection(@Validated LrsConnectionTO data, RedirectAttributes attributes) {
        this.lrsService.updateConnection(data);
        attributes.addAttribute("edit", data.getName());
        return new RedirectView(".");
    }

    /**
     * Show the Add page for LRS Connections
     */
    @GetMapping(LrsMavController.BASE_URL + "/add")
    public ModelAndView showAddLrsConnection() {
        ModelAndView mav = new ModelAndView("bootstrap/lrs/detail");
        mav.addObject("method", "add");
        return mav;
    }

    /**
     * Handle the creation of LRS Connections
     *
     * @param data Transfer object containing the new details of the LRS Connection
     */
    @PostMapping(LrsMavController.BASE_URL + "/add")
    public RedirectView addLrsConnection(@Validated LrsConnectionTO data, RedirectAttributes attributes) {
        this.lrsService.createConnection(data);
        attributes.addAttribute("create", data.getName());
        return new RedirectView(".");
    }
}
