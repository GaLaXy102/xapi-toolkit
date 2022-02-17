package de.tudresden.inf.verdatas.xapitools.ui;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * {@link UIIcon}s from <a href="https://icons.getbootstrap.com/">Bootstrap Icons</a>
 * Feel free to add any required icons.
 * The prefix ('bi-') is automagically added by {@link #getIconName()}.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BootstrapUIIcon implements UIIcon {
    ARROW_LR("arrow-left-right"),
    CLOUD("cloud-arrow-up"),
    SHUFFLE("shuffle"),
    CHART("bar-chart-line");

    private final String iconName;
    private static final String PREFIX = "bi-";

    /**
     * Get the Icon's full name
     *
     * @return HTML image class name including any framework-specific prefixes
     */
    @Override
    public String getIconName() {
        return PREFIX + this.iconName;
    }
}
