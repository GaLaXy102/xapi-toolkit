package de.tudresden.inf.verdatas.xapitools.dave.controllers;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;

import java.util.regex.Pattern;

@Controller
@Order(3)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class VisualisationsSettingFlowController implements AnalysisStep{

    static final String BASE_URL = DaveAnalysisMavController.BASE_URL + "/dashboards";

    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    @Override
    public String getName() {
        return "Select analysis";
    }

    /**
     * Get the Paths which belong to this step.
     * When this pattern is matched, the step will be highlighted in the UI.
     * Be sure to match **any** subpath of your step.
     *
     * @return Regex-Pattern matching all Paths that belong to this step
     */
    @Override
    public Pattern getPathRegex() {
        return Pattern.compile(BASE_URL + "/(new|edit)/visualisations$");
    }
}
