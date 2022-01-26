package de.tudresden.inf.verdatas.xapitools.ui;

import org.springframework.core.annotation.Order;

/**
 * Interface to represent Settings (for automatic binding in {@link BasepageMavController}).
 * Implement this for your sub-application's Settings to appear on the UI.
 * You can specify the position of your sub-application by adding the {@link Order}-Annotation.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface IUIManagementFlow extends IUIIconable {
    /**
     * Get the Human readable name of this Setting.
     *
     * @return Name of Setting
     */
    String getName();

    /**
     * Get the URL where the sub-application can be started.
     *
     * @return URL for Application Launch
     */
    String getEntrypoint();
}
