package de.tudresden.inf.verdatas.xapitools.datasim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.verdatas.xapitools.datasim.validators.Finalized;
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

@Component
@Validated
public class DatasimResultService {
    @Value("${xapi.datasim.sim-storage}")
    private String basepath;

    private final ObjectMapper mapper;

    public DatasimResultService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    private void ensureBasepathExists() {
        new File(this.basepath).mkdirs();
    }

    public void saveSimulationResult(@Finalized DatasimSimulation simulation, List<JsonNode> result) {
        this.ensureBasepathExists();
        try {
            this.mapper.writeValue(new File(this.basepath + "/" + simulation.getId() + ".json"), result);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public List<JsonNode> getSimulationResult(@Finalized DatasimSimulation simulation) {
        this.ensureBasepathExists();
        try {
            return this.mapper.readValue(
                    new File(this.basepath + "/" + simulation.getId() + ".json"),
                    this.mapper.getTypeFactory().constructCollectionType(List.class, JsonNode.class)
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

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
