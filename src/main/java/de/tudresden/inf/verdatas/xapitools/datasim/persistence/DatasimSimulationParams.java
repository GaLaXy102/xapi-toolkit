package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Positive;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Hibernate Entity representing DATASIM Simulation Parameters
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Entity
@Validated
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimSimulationParams {
    @GeneratedValue
    @Id
    @Getter
    @Column(columnDefinition = "BINARY(16)")  // Setting this manually is necessary as Hibernate does not handle it properly
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

    /**
     * Create a new Parameter Set
     *
     * @param max   Number of Statements to generate at most
     * @param seed  Seed of Simulation
     * @param start Start Time of Statements to generate
     * @param end   End Time of Statements to generate
     */
    DatasimSimulationParams(Long max, Long seed, ZonedDateTime start, ZonedDateTime end) {
        this.max = max;
        this.seed = seed;
        this.start = start.withNano(0);
        this.end = end.withNano(0);
    }
}
