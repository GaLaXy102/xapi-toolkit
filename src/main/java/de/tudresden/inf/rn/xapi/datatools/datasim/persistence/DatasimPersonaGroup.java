package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimPersonaGroup {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String name;

    @ManyToMany
    @Getter
    @Setter(AccessLevel.PACKAGE)
    // Members are reusable, thus no Cascade
    private Set<DatasimPersona> member;

    DatasimPersonaGroup(String name, Set<DatasimPersona> member) {
        this.name = name;
        this.member = member;
    }


}
