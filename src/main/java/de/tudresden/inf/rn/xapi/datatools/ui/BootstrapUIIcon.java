package de.tudresden.inf.rn.xapi.datatools.ui;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum BootstrapUIIcon implements UIIcon {
    SHUFFLE("shuffle"),
    CLOUD("cloud-arrow-up");

    @Getter
    private final String iconName;
}
