package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Validated
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimSimulationParamsTO {
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public Optional<UUID> id;

    @Getter
    @Setter
    @Positive
    private Long max;

    @Getter
    @Setter
    @Positive
    private Long seed;

    @Getter
    @Setter
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime start;

    @Getter
    @Setter
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
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
        Random random = new Random();
        return new DatasimSimulationParamsTO(Optional.empty(), 1000L, random.nextLong(5000L), LocalDateTime.now(), LocalDateTime.now().plusWeeks(1), ZoneId.systemDefault());
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

    public DatasimSimulationParamsTO forExport() {
        return new DatasimSimulationParamsTO(
                Optional.empty(),
                this.max,
                this.seed,
                this.start,
                this.end,
                this.timezone
        );
    }
}
