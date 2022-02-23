package de.tudresden.inf.verdatas.xapitools.dave;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnector;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveDashboard;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveVis;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveVisualisationService {
    private final DaveDashboardService daveAnalysisService;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;
    private final ObjectMapper mapper;

    @Value("${xapi.datasim.sim-storage}")  // TODO
    private String basepath;

    private static void writeFile(File file, String content) throws IOException {
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    private static String prepareQueryLimit(DaveVis analysis, Optional<String> activityId) {
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
        return this.daveAnalysisService.getVisualisationsOfDashboard(dashboard)
                .stream()
                .map((v) -> Pair.of(
                        Pair.of(v.getSecond().getName(), v.getFirst()),
                        Pair.of(
                                this.prepareQuery(v.getSecond(), v.getFirst()).getAbsolutePath(),
                                this.prepareVisualisation(v.getSecond()).getAbsolutePath()
                        )
                ))
                .map((prep) -> Pair.of(
                        prep.getFirst(),
                        connector.executeAnalysis(prep.getSecond().getFirst(), prep.getSecond().getSecond())
                ))
                .toList();
    }

    public File prepareQuery(DaveVis analysis, String activityId) {
        File out = new File(this.basepath + "/" + analysis.getQuery().getId() + ".json");
        Optional<String> activity = activityId.equals("all") ? Optional.empty() : Optional.of(activityId);
        try {
            DaveVisualisationService.writeFile(out, DaveVisualisationService.prepareQueryLimit(analysis, activity));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }

    public File prepareVisualisation(DaveVis analysis) {
        File out = new File(this.basepath + "/" + analysis.getDescription().getId() + ".json");
        try {
            DaveVisualisationService.writeFile(out, analysis.getDescription().getDescription());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }


}
