package de.tudresden.inf.verdatas.xapitools.ui;

import java.util.List;

public interface IUIFlow extends IUIIconable {
    String getName();
    String getEntrypoint();
    List<IUIStep> getSteps();
}
