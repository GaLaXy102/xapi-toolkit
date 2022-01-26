package de.tudresden.inf.verdatas.xapitools.datasim;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulationTO;
import de.tudresden.inf.verdatas.xapitools.datasim.validators.Finalized;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Rest Controller to Read and Perform Datasim Simulations
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@RestController
@RequestMapping("/api/v1/datasim")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimSimulationRestController {

    private final DatasimSimulationService datasimSimulationService;
    private final ObjectMapper objectMapper;
    private final DatasimConnector connector;
    private final DatasimResultService resultService;

    /**
     * Get the JSON document representing the DATASIM simulation
     * This can be used as <a href="https://github.com/yetanalytics/datasim/tree/d55f24098aca78db69e2f03d4f202ce58b5fdb49#alternatively-simulation-specification">Simulation Specification</a>.
     *
     * @param simulationId UUID of the Simulation to get the description of
     * @return JSON file containing the Simulation Representation
     */
    @GetMapping("/simulation_description")
    public ResponseEntity<DatasimSimulationTO> getSimulationDescription(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulationTO simulation = DatasimSimulationTO.of(this.datasimSimulationService.getSimulation(simulationId));
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(simulation.getRemark().orElse(simulation.getId().map(UUID::toString).orElse("Untitled Simulation")) + ".json")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(cd);
        return ResponseEntity.ok().headers(headers).body(simulation.forExport());
    }

    /**
     * Get a ZIP archive containing the JSON documents representing the DATASIM simulation
     * This file contains the <a href="https://github.com/yetanalytics/datasim/tree/d55f24098aca78db69e2f03d4f202ce58b5fdb49#simulation-inputs">Simulation Inputs</a>.
     *
     * @param simulationId UUID of the Simulation to get the description of
     * @return ZIP file containing the Simulation Representation
     */
    @GetMapping("/simulation_description_zip")
    public ResponseEntity<byte[]> getSimulationDescriptionZip(@RequestParam(name = "flow") UUID simulationId) throws IOException {
        DatasimSimulationTO simulation = DatasimSimulationTO.of(this.datasimSimulationService.getSimulation(simulationId));
        DatasimSimulationTO exportable = simulation.forExport();
        // Outputs
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        // Alignments
        ByteArrayOutputStream tempBAOS = new ByteArrayOutputStream();
        ZipEntry tempEntry = new ZipEntry("alignments.json");
        this.objectMapper.writeValue(tempBAOS, exportable.getAlignments());
        tempEntry.setSize(tempBAOS.size());
        zos.putNextEntry(tempEntry);
        zos.write(tempBAOS.toByteArray());
        zos.closeEntry();
        // Personae
        tempBAOS = new ByteArrayOutputStream();
        tempEntry = new ZipEntry("personae.json");
        this.objectMapper.writeValue(tempBAOS, exportable.getPersonaGroups());
        tempEntry.setSize(tempBAOS.size());
        zos.putNextEntry(tempEntry);
        zos.write(tempBAOS.toByteArray());
        zos.closeEntry();
        // Parameters
        tempBAOS = new ByteArrayOutputStream();
        tempEntry = new ZipEntry("parameters.json");
        this.objectMapper.writeValue(tempBAOS, exportable.getParameters());
        tempEntry.setSize(tempBAOS.size());
        zos.putNextEntry(tempEntry);
        zos.write(tempBAOS.toByteArray());
        zos.closeEntry();
        // Profile
        tempBAOS = new ByteArrayOutputStream();
        tempEntry = new ZipEntry("profiles.json");
        this.objectMapper.writeValue(tempBAOS, exportable.getProfiles());
        tempEntry.setSize(tempBAOS.size());
        zos.putNextEntry(tempEntry);
        zos.write(tempBAOS.toByteArray());
        zos.closeEntry();
        // Finish Zipping
        zos.close();
        // Send
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(simulation.getRemark().orElse(simulation.getId().map(UUID::toString).orElse("Untitled Simulation")) + ".zip")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(cd);
        return ResponseEntity.ok().headers(headers).body(baos.toByteArray());
    }

    /**
     * Send a Simulation to DATASIM and retrieve the result without persisting it.
     *
     * @param simulationId UUID of the Simulation to get the description of
     * @return Resulting xAPI Statements
     */
    @GetMapping("/perform")
    public ResponseEntity<List<JsonNode>> performSimulationWithoutPersist(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        DatasimSimulationTO sendable = DatasimSimulationTO.of(simulation).forExport();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.ok().headers(headers).body(this.connector.sendSimulation(sendable));
    }

    /**
     * Retrieve Results of DATASIM Simulation saved with {@link DatasimResultService#saveSimulationResult(DatasimSimulation, List)}.
     *
     * @param simulationId UUID of the Simulation to get the description of
     * @return Resulting xAPI Statements
     */
    @GetMapping("/retrieve")
    public ResponseEntity<List<JsonNode>> getSimulationResult(@RequestParam(name = "flow") UUID simulationId) {
        @Finalized DatasimSimulation simulation = this.datasimSimulationService.getSimulation(simulationId);
        ContentDisposition cd = ContentDisposition.attachment()
                .filename(simulation.getRemark() + ".json")
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(cd);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return ResponseEntity.ok().headers(headers).body(this.resultService.getSimulationResult(simulation));
    }
}
