package de.tudresden.inf.rn.xapi.datatools.lrs;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequestMapping("/ui/manage/lrs")
public class LrsMavController {
    private final LrsService lrsService;

    public LrsMavController(LrsService lrsService) {
        this.lrsService = lrsService;
    }

    @GetMapping("/")
    public ModelAndView showLrsConnections(@RequestParam Optional<Boolean> all) {
        ModelAndView mav = new ModelAndView("bootstrap/lrs/show");
        mav.addObject("connections", this.lrsService.getConnections(all.orElse(true)).stream().map(LrsConnectionTO::of).toList());
        return mav;
    }
}
