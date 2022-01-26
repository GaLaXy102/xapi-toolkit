package de.tudresden.inf.verdatas.xapitools.ui;

/**
 * Interface representing an UI Element (like {@link IUIFlow} or {@link IUIManagementFlow}) that can have an Icon.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface IUIIconable {
    /**
     * Get the Icon for this UI Element
     *
     * @return Icon entity
     */
    UIIcon getIcon();
}
