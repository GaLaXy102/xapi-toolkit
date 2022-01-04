package de.tudresden.inf.rn.xapi.datatools.lrs;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/ui/manage/lrs")
public class LrsMavController {
    private final LrsService lrsService;

    public LrsMavController(LrsService lrsService) {
        this.lrsService = lrsService;
    }

    @GetMapping("/")
    public ModelAndView showLrsConnections(@RequestParam(name = "active_only") Optional<Boolean> activeOnly) {
        ModelAndView mav = new ModelAndView("bootstrap/lrs/show");
        mav.addObject("connections", this.lrsService.getConnections(activeOnly.orElse(true)).stream().map(LrsConnectionTO::of).toList());
        return mav;
    }

    @PostMapping("/delete")
    public ModelAndView deleteLrsConnection(@RequestParam(name = "lrs_uuid") UUID lrsUuid) {
        // TODO: This can throw an IAE. See this in #16
        this.lrsService.deactivateConnection(lrsUuid);
        return new ModelAndView("redirect:/ui/manage/lrs/");
    }

    @PostMapping("/reactivate")
    public ModelAndView reactivateLrsConnection(@RequestParam(name = "lrs_uuid") UUID lrsUuid) {
        // TODO: This can throw an IAE. See this in #16
        this.lrsService.activateConnection(lrsUuid);
        return new ModelAndView("redirect:/ui/manage/lrs/");
    }

    @GetMapping("/{lrs_uuid}")
    public ModelAndView showEditLrsConnection(@PathVariable(name = "lrs_uuid") UUID lrsUuid) {
        // TODO: This can throw an IAE. See this in #16
        LrsConnection found = this.lrsService.getConnection(lrsUuid);
        ModelAndView mav = new ModelAndView("bootstrap/lrs/detail");
        mav.addObject("connection", LrsConnectionTO.of(found));
        return mav;
    }

    @PostMapping("/edit")
    public ModelAndView editLrsConnection(@Validated LrsConnectionTO data) {
        // TODO: This can throw an IAE. See this in #16
        this.lrsService.updateConnection(data);
        return new ModelAndView("redirect:/ui/manage/lrs/");
    }
}
