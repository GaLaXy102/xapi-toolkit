package de.tudresden.inf.verdatas.xapitools.datasim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.verdatas.xapitools.datasim.validators.Finalized;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Handle Storage of Results of Datasim Simulations
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Component
@Validated
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimResultService {
    @Value("${xapi.datasim.sim-storage}")
    private String basepath;

    private final ObjectMapper mapper;

    private void ensureBasepathExists() {
        new File(this.basepath).mkdirs();
    }

    /**
     * Save the result of a performed simulation
     *
     * @param simulation Simulation description
     * @param result     Resulting xAPI Statements
     */
    public void saveSimulationResult(@Finalized DatasimSimulation simulation, List<JsonNode> result) {
        this.ensureBasepathExists();
        try {
            this.mapper.writeValue(new File(this.basepath + "/" + simulation.getId() + ".json"), result);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Retrieve the result of a given Simulation
     *
     * @param simulation Simulation description to get the result of
     * @return xAPI Statements that resulted from the given Simulation
     * @throws DatasimExceptions.NoSuchSimulationResult when the result is unavailable.
     */
    public List<JsonNode> getSimulationResult(@Finalized DatasimSimulation simulation) {
        this.ensureBasepathExists();
        try {
            return this.mapper.readValue(
                    new File(this.basepath + "/" + simulation.getId() + ".json"),
                    this.mapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class)
            );
        } catch (IOException e) {
            throw new DatasimExceptions.NoSuchSimulationResult("No result available for Simulation " + simulation.getId());
        }
    }

    /**
     * Get all available Results
     *
     * @return Simulation IDs where the Result exists locally
     */
    public List<UUID> getSimulationsWithResultAvailable() {
        this.ensureBasepathExists();
        return Arrays
                .stream(
                        Objects.requireNonNullElse(
                                new File(this.basepath).listFiles((dir, name) -> {
                                    // Pattern taken from https://stackoverflow.com/a/37616347
                                    return name.replace(".json", "").matches("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})");
                                }),
                                new File[]{}
                        )
                )
                .map(File::getName)
                .map((fname) -> fname.replace(".json", ""))
                .map(UUID::fromString)
                .toList();

    }
}
