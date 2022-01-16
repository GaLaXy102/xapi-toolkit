package de.tudresden.inf.rn.xapi.datatools;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class RootRedirectController {

    @GetMapping("/")
    public RedirectView redirectToUi() {
        return new RedirectView("/ui");
    }
}
