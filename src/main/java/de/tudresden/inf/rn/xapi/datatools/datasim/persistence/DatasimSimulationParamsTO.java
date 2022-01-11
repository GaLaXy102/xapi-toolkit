package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
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
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime start;

    @Getter
    @Setter
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime end;

    @Getter
    @Setter
    private ZoneId timezone;

    public static DatasimSimulationParamsTO of(DatasimSimulationParams simulationParams) {
        return new DatasimSimulationParamsTO(
                Optional.of(simulationParams.getId()),
                simulationParams.getMax(),
                simulationParams.getSeed(),
                simulationParams.getStart().toLocalDateTime(),
                simulationParams.getEnd().toLocalDateTime(),
                simulationParams.getStart().getZone()
        );
    }

    public DatasimSimulationParams toNewSimulationParams() {
        this.id.ifPresent((id) -> {throw new IllegalStateException("UUID must be empty when creating new.");});
        return new DatasimSimulationParams(
                this.max,
                this.seed,
                this.start.atZone(this.timezone),
                this.end.atZone(this.timezone)
        );
    }

    public static DatasimSimulationParamsTO empty() {
        return new DatasimSimulationParamsTO(Optional.empty(), 1000L, 1337L, LocalDateTime.now(), LocalDateTime.now().plusWeeks(1), ZoneId.systemDefault());
    }

    public DatasimSimulationParams toExistingSimulationParams() {
        return new DatasimSimulationParams(
                this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")),
                this.max,
                this.seed,
                this.start.atZone(this.timezone),
                this.end.atZone(this.timezone)
        );
    }
}
