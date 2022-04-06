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

/**
 * This service handles the preparation of Analyses for execution.
 * Their queries and graph descriptions have to be saved as files, so they can be delivered to DAVE and executed correctly
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class FileManagementService {

    @Value("${xapi.datasim.sim-storage}")  // TODO
    private String basepath;

    /**
     * Create a new file
     *
     * @param prefix  friendly name for the file
     * @param content data to save
     * @throws UncheckedIOException when an error occurs while creation
     */
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

    /**
     * Save the Query description of the given Analysis in a file
     *
     * @param analysis   Entity to use
     * @param activityId URL, indicates if the Analysis is executed using the whole LRS data set or only the data belonging to a specific activity
     */
    public File prepareQuery(DaveVis analysis, String activityId) {
        Optional<String> activity = activityId.equals("all") ? Optional.empty() : Optional.of(activityId);
        return this.writeFile("query", DaveDashboardService.prepareQueryLimit(analysis, activity));
    }

    /**
     * Save the Graph description of the given Analysis in a file
     *
     * @param analysis Entity to use
     */
    public File prepareVisualisation(DaveVis analysis) {
        return this.writeFile("graph description", analysis.getDescription().getDescription());
    }

    /**
     * Validate the given Analysis description parts if they use the scheme required by DAVE
     *
     * @param queryDescription query to validate
     * @param graphDescription graph description to validate
     * @return the absolute paths for both files as {@link Pair}
     */
    public Pair<String, String> prepareValidityCheck(String queryDescription, String graphDescription) {
        File query = this.writeFile("query", queryDescription);
        File graph = this.writeFile("graph", graphDescription);
        return Pair.of(query.getAbsolutePath(), graph.getAbsolutePath());
    }
}