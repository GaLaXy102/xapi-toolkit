package de.tudresden.inf.rn.xapi.datatools.ui;

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
    private final List<IExternalService> externalServices;

    public BasepageMavController(List<IUIFlow> flows, List<IUIManagementFlow> managementFlows, List<IExternalService> externalServices) {
        this.flows = flows;
        this.managementFlows = managementFlows;
        this.externalServices = externalServices;
    }

    @GetMapping("")
    public ModelAndView showHome() {
        ModelAndView mav = new ModelAndView("bootstrap/home");
        mav.addObject("uiFlows", this.flows);
        mav.addObject("uiManagementFlows", this.managementFlows);
        mav.addObject("uiExternalServices", this.externalServices);
        return mav;
    }
}
