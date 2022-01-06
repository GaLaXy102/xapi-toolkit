package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatasimPersonaGroup implements DatasimActor {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter
    private String name;

    @ManyToMany
    @Getter
    @Setter
    // Members are reusable, thus no Cascade
    private Set<DatasimPersona> member;

    public DatasimPersonaGroup(String name, Set<DatasimPersona> member) {
        this.name = name;
        this.member = member;
    }

    @Override
    public String getIri() {
        // There is no documentation on this yet.
        return "group::" + this.name;
    }

    @Override
    public DatasimActorType getType() {
        return DatasimActorType.GROUP;
    }
}
