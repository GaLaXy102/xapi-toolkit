package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

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

    public static DatasimPersonaGroupTO of(DatasimPersonaGroup personaGroup) {
        Set<DatasimPersonaTO> members = personaGroup.getMember().stream().map(DatasimPersonaTO::of).collect(Collectors.toSet());
        return new DatasimPersonaGroupTO(Optional.of(personaGroup.getId()), personaGroup.getName(), members);
    }

    public DatasimPersonaGroup toNewPersonaGroup() {
        this.id.ifPresent((id) -> {throw new IllegalStateException("UUID must be empty when creating new.");});
        Set<DatasimPersona> members = this.member.stream().map(DatasimPersonaTO::toExistingDatasimPersona).collect(Collectors.toSet());
        return new DatasimPersonaGroup(this.name, members);
    }

    DatasimPersonaGroup toExistingPersonaGroup() {
        Set<DatasimPersona> members = this.member.stream().map(DatasimPersonaTO::toExistingDatasimPersona).collect(Collectors.toSet());
        return new DatasimPersonaGroup(this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")), this.name, members);
    }

    public static DatasimPersonaGroupTO empty(String name) {
        return new DatasimPersonaGroupTO(Optional.empty(), name, Set.of());
    }

    @Override
    @JsonIgnore
    public String getIri() {
        // There is no documentation on this yet.
        return "group::" + this.name.toLowerCase().replace(" ", "_");
    }

    @Override
    @JsonProperty("objectType")
    public DatasimActorType getType() {
        return DatasimActorType.GROUP;
    }

    public DatasimPersonaGroupTO forExport() {
        return new DatasimPersonaGroupTO(
                Optional.empty(),
                this.name,
                this.member.stream().map(DatasimPersonaTO::forExport).collect(Collectors.toSet())
        );
    }
}
