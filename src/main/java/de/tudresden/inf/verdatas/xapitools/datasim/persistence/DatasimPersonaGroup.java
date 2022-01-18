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
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

@Entity
@Validated
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimPersonaGroup {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @NotBlank
    private String name;

    @ManyToMany
    @Getter
    @Setter
    @NotNull
    // Members are reusable, thus no Cascade
    private Set<DatasimPersona> member;

    DatasimPersonaGroup(String name, Set<DatasimPersona> member) {
        this.name = name;
        this.member = member;
    }


}
