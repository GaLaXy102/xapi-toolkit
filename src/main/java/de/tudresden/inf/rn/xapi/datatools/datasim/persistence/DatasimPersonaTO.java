package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.Optional;
import java.util.UUID;

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

    public static DatasimPersonaTO of(DatasimPersona persona) {
        return new DatasimPersonaTO(Optional.of(persona.getId()), persona.getName(), persona.getMbox());
    }

    public DatasimPersona toNewDatasimPersona() {
        this.id.ifPresent((id) -> {throw new IllegalStateException("UUID must be empty when creating new.");});
        return new DatasimPersona(this.name, "mailto:" + this.mbox);
    }

    DatasimPersona toExistingDatasimPersona() {
        return new DatasimPersona(this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")), this.name, this.mbox);
    }

    public DatasimPersonaTO forExport() {
        return new DatasimPersonaTO(
                Optional.empty(),
                this.name,
                this.mbox
        );
    }

    @Override
    @JsonIgnore
    public String getIri() {
        return "mbox::" + this.mbox;
    }

    @Override
    public DatasimActorType getType() {
        return DatasimActorType.AGENT;
    }
}
