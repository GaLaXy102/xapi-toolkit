package de.tudresden.inf.verdatas.xapitools.ui;

import java.util.List;

public interface IUIManagementFlow extends IUIIconable {
    String getName();
    String getEntrypoint();
    List<IUIStep> getSteps();
}
