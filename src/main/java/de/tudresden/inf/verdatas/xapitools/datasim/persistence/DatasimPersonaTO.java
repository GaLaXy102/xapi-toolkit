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
import java.util.Optional;
import java.util.UUID;

/**
 * Transfer Object for Communication with DATASIM, representing a Persona
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Validated
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimPersonaTO implements DatasimActor {
    @Getter
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<UUID> id;

    @Getter
    @Setter
    @NotBlank
    private String name;

    @Getter
    @Setter
    @NotBlank
    private String mbox;

    /**
     * Create a TO from an Entity
     *
     * @param persona Base Entity to get a representation of
     * @return Decoupled Transfer Object
     */
    public static DatasimPersonaTO of(DatasimPersona persona) {
        return new DatasimPersonaTO(Optional.of(persona.getId()), persona.getName(), persona.getMbox());
    }

    /**
     * Create a new DatasimPersona Entity for use in Services.
     *
     * @return persistable Entity
     * @throws IllegalStateException when the TO has its UUID set
     */
    public DatasimPersona toNewDatasimPersona() {
        this.id.ifPresent((id) -> {
            throw new IllegalStateException("UUID must be empty when creating new.");
        });
        return new DatasimPersona(this.name, "mailto:" + this.mbox);
    }

    /**
     * Create an "existing" (i.e. there is a Persona with this ID) DatasimPersona Entity for use in Services.
     *
     * @return persistable Entity
     * @throws IllegalStateException when the TO has no UUID set
     */
    DatasimPersona toExistingDatasimPersona() {
        return new DatasimPersona(this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")), this.name, this.mbox);
    }

    /**
     * Adaptions for sending to DATASIM or export
     */
    public DatasimPersonaTO forExport() {
        return new DatasimPersonaTO(
                Optional.empty(),
                this.name,
                this.mbox
        );
    }

    /**
     * Get the Identifier of the Actor
     *
     * @return Identifier in IRI format
     */
    @Override
    @JsonIgnore
    public String getIri() {
        return "mbox::" + this.mbox;
    }

    /**
     * Get the Type of the Actor
     *
     * @return Type, i.e. Agent or Group
     */
    @Override
    @JsonProperty("objectType")
    public DatasimActorType getType() {
        return DatasimActorType.AGENT;
    }
}
