package de.tudresden.inf.verdatas.xapitools.ui;

import java.util.regex.Pattern;

/**
 * Interface representing a step of an {@link IUIFlow}
 * You probably need to create a sub-interface for dynamic binding to work correctly (see Developer's Guide).
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface IUIStep {
    /**
     * Get the Human readable name of this Step
     *
     * @return Name of Step
     */
    String getName();

    /**
     * Get the Paths which belong to this step.
     * When this pattern is matched, the step will be highlighted in the UI.
     * Be sure to match **any** subpath of your step.
     *
     * @return Regex-Pattern matching all Paths that belong to this step
     */
    Pattern getPathRegex();
}
