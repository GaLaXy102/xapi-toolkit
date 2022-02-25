package de.tudresden.inf.verdatas.xapitools.dave.dashboards;

import de.tudresden.inf.verdatas.xapitools.dave.FileManagementService;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnector;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVis;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveVisualisationService {
    private final DaveDashboardService daveAnalysisService;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;
    private final FileManagementService fileManagementService;

    public static String prepareQueryLimit(DaveVis analysis, Optional<String> activityId) {
        String query = analysis.getQuery().getQuery();
        if (activityId.isPresent()) {
            query = query.substring(0, query.length() - 1) +
                    "[?s :statement/object ?ac][?ac :activity/id \"" + activityId.get() +"\"]"
                    + "]";
        }
        return query;
    }

    /**
     * Pair<Pair<VisName,ActivityURL>,VisSVG>
     */
    // TODO refactor types
    public List<Pair<Pair<String, String>, String>> executeVisualisationsOfDashboard(DaveDashboard dashboard) {
        DaveConnector connector = this.daveConnectorLifecycleManager.getConnector(dashboard.getLrsConnection());
        return dashboard.getVisualisations()
                .stream()
                .map((v) -> Pair.of(
                        Pair.of(this.daveAnalysisService.getAnalysisById(v.getSecond()).getName(), v.getFirst()),
                        Pair.of(
                                this.fileManagementService
                                        .prepareQuery(this.daveAnalysisService.getAnalysisById(v.getSecond()), v.getFirst()).getAbsolutePath(),
                                this.fileManagementService
                                        .prepareVisualisation(this.daveAnalysisService.getAnalysisById(v.getSecond())).getAbsolutePath()
                        )
                ))
                .map((prep) -> Pair.of(
                        prep.getFirst(),
                        connector.executeAnalysis(prep.getSecond().getFirst(), prep.getSecond().getSecond())
                ))
                .toList();
    }
}
