package de.tudresden.inf.verdatas.xapitools;

import de.tudresden.inf.verdatas.xapitools.ui.BasepageMavController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This Controller only adds a Redirect for the Root-Context ("/")
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
public class RootRedirectController {

    /**
     * Simple 302 Redirect to {@link BasepageMavController#showHome()}
     */
    @GetMapping("/")
    public RedirectView redirectToUi() {
        return new RedirectView("/ui");
    }
}
