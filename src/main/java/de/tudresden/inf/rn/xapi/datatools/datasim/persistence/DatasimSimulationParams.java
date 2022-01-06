package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
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
public class DatasimSimulationParams {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter
    private Long max;

    @Getter
    @Setter
    @Positive
    private Long seed;

    // Timezone is included here
    @Getter
    @Setter
    private LocalDateTime start;

    @Getter
    @Setter
    private LocalDateTime end;

    public DatasimSimulationParams(Long max, Long seed, LocalDateTime start, LocalDateTime end) {
        this.max = max;
        this.seed = seed;
        this.start = start;
        this.end = end;
    }
}
