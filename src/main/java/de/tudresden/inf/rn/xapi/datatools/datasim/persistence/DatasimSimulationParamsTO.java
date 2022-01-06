package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimSimulationParamsTO {
    @Getter
    public Optional<UUID> id;

    @Getter
    @Setter
    private Long max;

    @Getter
    @Setter
    @Positive
    private Long seed;

    @Getter
    @Setter
    private LocalDateTime start;

    @Getter
    @Setter
    private LocalDateTime end;

    @Getter
    @Setter
    private TimeZone timezone;

    public static DatasimSimulationParamsTO of(DatasimSimulationParams simulationParams) {
        return new DatasimSimulationParamsTO(
                Optional.of(simulationParams.getId()),
                simulationParams.getMax(),
                simulationParams.getSeed(),
                simulationParams.getStart(),
                simulationParams.getEnd(),
                TimeZone.getTimeZone(simulationParams.getTimezone())
        );
    }

    public DatasimSimulationParams toNewSimulationParams() {
        this.id.ifPresent((id) -> {throw new IllegalStateException("UUID must be empty when creating new.");});
        return new DatasimSimulationParams(
                this.max,
                this.seed,
                this.start,
                this.end,
                this.timezone.getID()
        );
    }

    DatasimSimulationParams toExistingSimulationParams() {
        return new DatasimSimulationParams(
                this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")),
                this.max,
                this.seed,
                this.start,
                this.end,
                this.getTimezone().getID()
        );
    }
}
