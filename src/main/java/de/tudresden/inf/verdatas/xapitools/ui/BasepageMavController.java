package de.tudresden.inf.verdatas.xapitools.ui;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.List;

/**
 * Controller for the Main Page
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Controller
@RequestMapping("/ui")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class BasepageMavController {
    /**
     * Automatically (and statically) bound {@link IUIFlow}s
     */
    private final List<IUIFlow> flows;
    /**
     * Automatically (and statically) bound {@link IUIManagementFlow}s
     */
    private final List<IUIManagementFlow> managementFlows;
    private final ApplicationContext context;

    /**
     * Render the Main Page including any bound Flows and Services
     */
    @GetMapping("")
    public ModelAndView showHome() {
        // These can be runtime-specific because of the LrsConnector Lifecycle
        List<IExternalService> externalServices = this.context.getBeansOfType(IExternalService.class).values()
                .stream()
                .sorted(Comparator.comparing(IExternalService::getName))
                .toList();
        ModelAndView mav = new ModelAndView("bootstrap/home");
        mav.addObject("uiFlows", flows);
        mav.addObject("uiManagementFlows", managementFlows);
        mav.addObject("uiExternalServices", externalServices);
        return mav;
    }
}
