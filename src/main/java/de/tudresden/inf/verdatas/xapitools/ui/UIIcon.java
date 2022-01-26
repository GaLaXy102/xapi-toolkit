package de.tudresden.inf.verdatas.xapitools.ui;

/**
 * An interface that represents any programmatically bound UI Icon.
 * Remember that you possibly need to add your Icon Framework to your HTML template.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface UIIcon {
    /**
     * Get the Icon's full name
     *
     * @return HTML image class name including any framework-specific prefixes
     */
    String getIconName();
}
