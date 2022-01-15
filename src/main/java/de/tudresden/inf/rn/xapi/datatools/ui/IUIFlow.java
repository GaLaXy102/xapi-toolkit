package de.tudresden.inf.rn.xapi.datatools.ui;

import java.util.List;

public interface IUIFlow {
    String getName();
    String getEntrypoint();
    List<IUIStep> getSteps();
}
