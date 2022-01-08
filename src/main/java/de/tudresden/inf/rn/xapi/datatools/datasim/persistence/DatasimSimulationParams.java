package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimSimulationParams {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Long max;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @Positive
    private Long seed;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private LocalDateTime start;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private LocalDateTime end;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String timezone;

    DatasimSimulationParams(Long max, Long seed, LocalDateTime start, LocalDateTime end, String timezone) {
        this.max = max;
        this.seed = seed;
        this.start = start;
        this.end = end;
        this.timezone = timezone;
    }
}