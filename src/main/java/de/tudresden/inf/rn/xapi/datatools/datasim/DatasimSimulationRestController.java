package de.tudresden.inf.rn.xapi.datatools.datasim;

import de.tudresden.inf.rn.xapi.datatools.datasim.persistence.DatasimSimulationTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/datasim")
public class DatasimSimulationRestController {

    private final DatasimSimulationService datasimSimulationService;

    public DatasimSimulationRestController(DatasimSimulationService datasimSimulationService) {
        this.datasimSimulationService = datasimSimulationService;
    }

    @GetMapping("/simulation_description")
    public ResponseEntity<DatasimSimulationTO> getSimulationDescription(@RequestParam(name = "flow") UUID simulationId) {
        DatasimSimulationTO simulation = DatasimSimulationTO.of(this.datasimSimulationService.getSimulation(simulationId)).forExport();
        return ResponseEntity.ok(simulation);
    }
}
