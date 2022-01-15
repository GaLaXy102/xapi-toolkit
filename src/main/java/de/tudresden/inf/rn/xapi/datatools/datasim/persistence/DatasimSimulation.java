package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Validated
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatasimSimulation {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter
    private String remark;

    // The properties below are required by DATASIM
    @Getter
    @Setter
    @ManyToMany
    @Cascade(CascadeType.ALL)
    private Set<DatasimPersonaGroup> personaGroups;

    @Getter
    @Setter
    @ManyToMany
    // each alignment has one Persona associated
    private Map<DatasimAlignment, DatasimPersona> alignments;

    @Getter
    @Setter
    @OneToOne
    @Cascade(CascadeType.ALL)
    private DatasimSimulationParams parameters;

    @Getter
    @Setter
    @ManyToMany
    // Profiles are reusable, thus no cascade
    private List<DatasimProfile> profiles;

    @Getter
    @Setter
    private boolean finalized;

    public DatasimSimulation(String remark, Set<DatasimPersonaGroup> personaGroups, Map<DatasimAlignment, DatasimPersona> alignments,
                             DatasimSimulationParams parameters, List<DatasimProfile> profiles) {
        this.remark = remark;
        this.personaGroups = personaGroups;
        this.alignments = alignments;
        this.parameters = parameters;
        this.profiles = profiles;
        this.finalized = false;
    }
}
