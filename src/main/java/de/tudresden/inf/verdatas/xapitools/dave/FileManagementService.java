package de.tudresden.inf.verdatas.xapitools.dave;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudresden.inf.verdatas.xapitools.dave.dashboards.DaveVisualisationService;
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class FileManagementService {
    private final ObjectMapper mapper;

    @Value("${xapi.datasim.sim-storage}")  // TODO
    private String basepath;

    private static void writeFile(File file, String content) throws IOException {
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes(StandardCharsets.UTF_8));
        fos.close();
    }

    public File prepareQuery(DaveVis analysis, String activityId) {
        File out = new File(this.basepath + "/" + analysis.getQuery().getId() + ".json");
        Optional<String> activity = activityId.equals("all") ? Optional.empty() : Optional.of(activityId);
        try {
            FileManagementService.writeFile(out, DaveVisualisationService.prepareQueryLimit(analysis, activity));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }

    public File prepareVisualisation(DaveVis analysis) {
        File out = new File(this.basepath + "/" + analysis.getDescription().getId() + ".json");
        try {
            FileManagementService.writeFile(out, analysis.getDescription().getDescription());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out;
    }

    public Pair<String, String> prepareValidityCheck(String queryName, String queryDescription,
                                                         String graphName, String graphDescription) {
        File query = new File(this.basepath + "/" + queryName + ".json");
        File graph = new File(this.basepath + "/" + graphName + ".json");
        try {
            FileManagementService.writeFile(query, queryDescription);
            FileManagementService.writeFile(graph, graphDescription);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Pair.of(query.getAbsolutePath(), graph.getAbsolutePath());
    }
}
