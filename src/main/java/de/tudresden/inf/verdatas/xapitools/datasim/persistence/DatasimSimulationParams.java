package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Positive;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Validated
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimSimulationParams {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @Positive
    private Long max;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @Positive
    private Long seed;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private ZonedDateTime start;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private ZonedDateTime end;

    DatasimSimulationParams(Long max, Long seed, ZonedDateTime start, ZonedDateTime end) {
        this.max = max;
        this.seed = seed;
        this.start = start.withNano(0);
        this.end = end.withNano(0);
    }
}
