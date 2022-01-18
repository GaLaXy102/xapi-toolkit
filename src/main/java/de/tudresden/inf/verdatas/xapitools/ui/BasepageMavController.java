package de.tudresden.inf.verdatas.xapitools.ui;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/ui")
public class BasepageMavController {
    private final List<IUIFlow> flows;
    private final List<IUIManagementFlow> managementFlows;
    private final ApplicationContext context;

    public BasepageMavController(List<IUIFlow> flows, List<IUIManagementFlow> managementFlows, ApplicationContext context) {
        this.flows = flows;
        this.managementFlows = managementFlows;
        this.context = context;
    }

    @GetMapping("")
    public ModelAndView showHome() {
        // These can be runtime-specific because of the LrsConnector Lifecycle
        List<IExternalService> externalServices = this.context.getBeansOfType(IExternalService.class).values().stream().toList();
        ModelAndView mav = new ModelAndView("bootstrap/home");
        mav.addObject("uiFlows", flows);
        mav.addObject("uiManagementFlows", managementFlows);
        mav.addObject("uiExternalServices", externalServices);
        return mav;
    }
}
