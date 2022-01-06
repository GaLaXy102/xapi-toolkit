package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatasimSimulation {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String remark;

    // The properties below are required by DATASIM
    @Getter
    @Setter(AccessLevel.PACKAGE)
    @ManyToMany
    @Cascade(CascadeType.ALL)
    private Set<DatasimPersonaGroup> personaGroups;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @OneToMany
    @Cascade(CascadeType.ALL)
    // each alignment has one Persona associated
    private Map<DatasimAlignment, DatasimPersona> alignments;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @OneToOne
    @Cascade(CascadeType.ALL)
    private DatasimSimulationParams parameters;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @ManyToOne
    // Profiles are reusable, thus no cascade
    private DatasimProfile profile;

    DatasimSimulation(String remark, Set<DatasimPersonaGroup> personaGroups, Map<DatasimAlignment, DatasimPersona> alignments,
                      DatasimSimulationParams parameters, DatasimProfile profile) {
        this.remark = remark;
        this.personaGroups = personaGroups;
        this.alignments = alignments;
        this.parameters = parameters;
        this.profile = profile;
    }
}
