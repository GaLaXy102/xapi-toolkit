package de.tudresden.inf.verdatas.xapitools.ui;

import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * Interface to represent an UI application (for automatic binding in {@link BasepageMavController}).
 * Implement this for your sub-application to appear on the UI.
 * You can specify the position of your sub-application by adding the {@link Order}-Annotation.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface IUIFlow extends IUIIconable {
    /**
     * Get the Human readable name of this sub-application.
     *
     * @return Name of sub-application
     */
    String getName();

    /**
     * Get the URL where the sub-application can be started.
     *
     * @return URL for Application Launch
     */
    String getEntrypoint();

    /**
     * Get all Steps belonging to the sub-application, so they can be displayed alongside the Launcher.
     *
     * @return List of sub-app Steps
     */
    List<? extends IUIStep> getSteps();
}
