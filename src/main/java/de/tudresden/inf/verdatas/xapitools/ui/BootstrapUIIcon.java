package de.tudresden.inf.verdatas.xapitools.ui;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BootstrapUIIcon implements UIIcon {
    ARROW_LR("arrow-left-right"),
    CLOUD("cloud-arrow-up"),
    SHUFFLE("shuffle");

    private final String iconName;
    private static final String PREFIX = "bi-";

    @Override
    public String getIconName() {
        return PREFIX + this.iconName;
    }
}
