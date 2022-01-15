package de.tudresden.inf.rn.xapi.datatools.ui;

import java.util.regex.Pattern;

public interface IUIStep {
    String getName();
    Pattern getPathRegex();
}
