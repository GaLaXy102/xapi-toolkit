package de.tudresden.inf.verdatas.xapitools;

import de.tudresden.inf.verdatas.xapitools.ui.BasepageMavController;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * This Controller only adds a Redirect for the Root-Context ("/")
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
public class RootRedirectController implements ErrorController {

    /**
     * Simple 302 Redirect to {@link BasepageMavController#showHome()}
     */
    @GetMapping("/")
    public RedirectView redirectToUi() {
        return new RedirectView("/ui");
    }

    @GetMapping("/error")
    public ModelAndView showNotFound() {
        throw new NoSuchElementException("No such page");
    }
}
