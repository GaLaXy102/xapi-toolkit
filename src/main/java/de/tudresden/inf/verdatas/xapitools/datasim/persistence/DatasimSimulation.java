package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.validation.annotation.Validated;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Hibernate Entity representing a DATASIM Simulation Description
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
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

    /**
     * Create a new Datasim Simulation
     *
     * @param remark        Title of the Simulation
     * @param personaGroups Groups of Personae that participate in this Simulation
     * @param alignments    Mapping Alignment -> Persona weighting the interaction with Simulation content
     * @param parameters    Parameters of the Simulation
     * @param profiles      Profile document in use
     */
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
