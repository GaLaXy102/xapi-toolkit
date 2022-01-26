package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Transfer Object for Communication with DATASIM, representing a Group of Personae
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Validated
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimPersonaGroupTO implements DatasimActor {
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<UUID> id;

    @Getter
    @Setter
    @NotBlank
    private String name;

    @Getter
    @Setter
    @NotNull
    private Set<DatasimPersonaTO> member;

    /**
     * Create a TO from an Entity
     *
     * @param personaGroup Base Entity to get a representation of
     * @return Decoupled Transfer Object
     */
    public static DatasimPersonaGroupTO of(DatasimPersonaGroup personaGroup) {
        Set<DatasimPersonaTO> members = personaGroup.getMember().stream().map(DatasimPersonaTO::of).collect(Collectors.toSet());
        return new DatasimPersonaGroupTO(Optional.of(personaGroup.getId()), personaGroup.getName(), members);
    }

    /**
     * Create a new DatasimPersonaGroup Entity for use in Services. The DatasimPersona contained are taken as existing.
     *
     * @return persistable Entity
     * @throws IllegalStateException when the TO has its UUID set
     */
    public DatasimPersonaGroup toNewPersonaGroup() {
        this.id.ifPresent((id) -> {
            throw new IllegalStateException("UUID must be empty when creating new.");
        });
        Set<DatasimPersona> members = this.member.stream().map(DatasimPersonaTO::toExistingDatasimPersona).collect(Collectors.toSet());
        return new DatasimPersonaGroup(this.name, members);
    }

    /**
     * Create an "existing" (i.e. there is a Group with this ID) DatasimPersonaGroup Entity for use in Services.
     *
     * @return persistable Entity
     * @throws IllegalStateException when the TO has no UUID set
     */
    DatasimPersonaGroup toExistingPersonaGroup() {
        Set<DatasimPersona> members = this.member.stream().map(DatasimPersonaTO::toExistingDatasimPersona).collect(Collectors.toSet());
        return new DatasimPersonaGroup(this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")), this.name, members);
    }

    /**
     * Create an empty PersonaGroup TO
     *
     * @param name Name of the Group to create
     * @return Empty Group TO for further consuming
     */
    public static DatasimPersonaGroupTO empty(String name) {
        return new DatasimPersonaGroupTO(Optional.empty(), name, Set.of());
    }

    /**
     * Get the Identifier of the Actor
     *
     * @return Identifier in IRI format
     */
    @Override
    @JsonIgnore
    public String getIri() {
        // There is no documentation on this yet.
        return "group::" + this.name.toLowerCase().replace(" ", "_");
    }

    /**
     * Get the Type of the Actor
     *
     * @return Type, i.e. Agent or Group
     */
    @Override
    @JsonProperty("objectType")
    public DatasimActorType getType() {
        return DatasimActorType.GROUP;
    }

    /**
     * Recursive adaptions for sending to DATASIM or export
     */
    public DatasimPersonaGroupTO forExport() {
        return new DatasimPersonaGroupTO(
                Optional.empty(),
                this.name,
                this.member.stream().map(DatasimPersonaTO::forExport).collect(Collectors.toSet())
        );
    }
}
