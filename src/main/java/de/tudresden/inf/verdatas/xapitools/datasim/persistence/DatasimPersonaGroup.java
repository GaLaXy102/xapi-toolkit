package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

/**
 * Hibernate Entity representing a Group of DATASIM Personae
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
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

    /**
     * Create a new Persona Group.
     *
     * @param name   Name of the Component
     * @param member Members of the Group
     */
    DatasimPersonaGroup(String name, Set<DatasimPersona> member) {
        this.name = name;
        this.member = member;
    }
}
