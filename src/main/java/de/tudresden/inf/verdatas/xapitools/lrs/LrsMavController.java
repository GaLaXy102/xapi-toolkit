package de.tudresden.inf.verdatas.xapitools.lrs;

import de.tudresden.inf.verdatas.xapitools.ui.BootstrapUIIcon;
import de.tudresden.inf.verdatas.xapitools.ui.IUIManagementFlow;
import de.tudresden.inf.verdatas.xapitools.ui.IUIStep;
import de.tudresden.inf.verdatas.xapitools.ui.UIIcon;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@Order(1)
@RequiredArgsConstructor
public class LrsMavController implements IUIManagementFlow {
    private final LrsService lrsService;

    static final String BASE_URL = "/ui/manage/lrs";

    @Override
    public String getName() {
        return "LRS Connections";
    }

    @Override
    public String getEntrypoint() {
        return LrsMavController.BASE_URL + "/";
    }

    @Override
    public List<IUIStep> getSteps() {
        return List.of();
    }

    @Override
    public UIIcon getIcon() {
        return BootstrapUIIcon.CLOUD;
    }

    @GetMapping(LrsMavController.BASE_URL + "/")
    public ModelAndView showLrsConnections(@RequestParam(name = "active_only") Optional<Boolean> activeOnly) {
        ModelAndView mav = new ModelAndView("bootstrap/lrs/show");
        mav.addObject("connections", this.lrsService.getConnections(activeOnly.orElse(true)).stream().map(LrsConnectionTO::of).toList());
        return mav;
    }

    @PostMapping(LrsMavController.BASE_URL + "/deactivate")
    public ModelAndView deleteLrsConnection(@RequestParam(name = "lrs_uuid") UUID lrsUuid) {
        this.lrsService.deactivateConnection(lrsUuid);
        return new ModelAndView("redirect:/ui/manage/lrs/");
    }

    @PostMapping(LrsMavController.BASE_URL + "/reactivate")
    public ModelAndView reactivateLrsConnection(@RequestParam(name = "lrs_uuid") UUID lrsUuid) {
        this.lrsService.activateConnection(lrsUuid);
        return new ModelAndView("redirect:/ui/manage/lrs/");
    }

    @GetMapping(LrsMavController.BASE_URL + "/edit")
    public ModelAndView showEditLrsConnection(@RequestParam(name = "lrs_uuid") UUID lrsUuid) {
        LrsConnection found = this.lrsService.getConnection(lrsUuid);
        ModelAndView mav = new ModelAndView("bootstrap/lrs/detail");
        mav.addObject("connection", LrsConnectionTO.of(found));
        mav.addObject("method", "edit");
        return mav;
    }

    @PostMapping(LrsMavController.BASE_URL + "/edit")
    public ModelAndView editLrsConnection(@Validated LrsConnectionTO data) {
        this.lrsService.updateConnection(data);
        return new ModelAndView("redirect:/ui/manage/lrs/");
    }

    @GetMapping(LrsMavController.BASE_URL + "/add")
    public ModelAndView showAddLrsConnection() {
        ModelAndView mav = new ModelAndView("bootstrap/lrs/detail");
        mav.addObject("method", "add");
        return mav;
    }

    @PostMapping(LrsMavController.BASE_URL + "/add")
    public ModelAndView addLrsConnection(@Validated LrsConnectionTO data) {
        this.lrsService.createConnection(data);
        return new ModelAndView("redirect:/ui/manage/lrs/");
    }
}
