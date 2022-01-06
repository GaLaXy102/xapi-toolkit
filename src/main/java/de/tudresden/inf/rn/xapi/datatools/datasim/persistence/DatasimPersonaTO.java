package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimPersonaTO implements DatasimActor {
    @Getter
    private Optional<UUID> id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String mbox;

    public static DatasimPersonaTO of(DatasimPersona persona) {
        return new DatasimPersonaTO(Optional.of(persona.getId()), persona.getName(), persona.getMbox());
    }

    public DatasimPersona toNewDatasimPersona() {
        this.id.ifPresent((id) -> {throw new IllegalStateException("UUID must be empty when creating new.");});
        return new DatasimPersona(this.name, this.mbox);
    }

    DatasimPersona toExistingDatasimPersona() {
        return new DatasimPersona(this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")), this.name, this.mbox);
    }

    @Override
    public String getIri() {
        return this.mbox.replace(":", "::");
    }

    @Override
    public DatasimActorType getType() {
        return DatasimActorType.AGENT;
    }
}
