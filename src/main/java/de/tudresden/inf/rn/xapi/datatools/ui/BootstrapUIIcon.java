package de.tudresden.inf.rn.xapi.datatools.ui;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BootstrapUIIcon implements UIIcon {
    SHUFFLE("shuffle"),
    CLOUD("cloud-arrow-up");

    private final String iconName;
    private static final String PREFIX = "bi-";

    @Override
    public String getIconName() {
        return PREFIX + this.iconName;
    }
}
