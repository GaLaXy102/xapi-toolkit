package de.tudresden.inf.verdatas.xapitools.ui;

import java.util.regex.Pattern;

public interface IUIStep {
    String getName();
    Pattern getPathRegex();
}
