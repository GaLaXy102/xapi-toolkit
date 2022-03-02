package de.tudresden.inf.verdatas.xapitools.dave;

import de.tudresden.inf.verdatas.xapitools.dave.dashboards.DaveDashboardService;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class FileManagementService {

    @Value("${xapi.datasim.sim-storage}")  // TODO
    private String basepath;

    private File writeFile(String prefix, String content) throws UncheckedIOException {
        try {
            File file = File.createTempFile(prefix, ".json", new File(this.basepath));
            file.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
            }
            return file;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public File prepareQuery(DaveVis analysis, String activityId) {
        Optional<String> activity = activityId.equals("all") ? Optional.empty() : Optional.of(activityId);
        return this.writeFile("query", DaveDashboardService.prepareQueryLimit(analysis, activity));
    }

    public File prepareVisualisation(DaveVis analysis) {
        return this.writeFile("analysis", analysis.getDescription().getDescription());
    }

    public Pair<String, String> prepareValidityCheck(String queryDescription, String graphDescription) {
        File query = this.writeFile("query", queryDescription);
        File graph = this.writeFile("graph", graphDescription);
        return Pair.of(query.getAbsolutePath(), graph.getAbsolutePath());
    }
}