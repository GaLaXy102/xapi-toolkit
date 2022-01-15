package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.Set;
import java.util.stream.Collectors;

@Validated
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActorWithAlignmentsTO implements DatasimActor {
    @Getter
    @Setter
    @NotBlank
    private String id;

    @Getter
    @Setter
    private Set<DatasimAlignmentTO> alignments;

    public static ActorWithAlignmentsTO with(DatasimActor actor, Set<DatasimAlignmentTO> alignments) {
        return new ActorWithAlignmentsTO(
                actor.getIri(),
                alignments
        );
    }

    @Override
    @JsonIgnore
    public String getIri() {
        return this.id;
    }

    @Override
    public DatasimActorType getType() {
        return DatasimActorType.AGENT;
    }

    public ActorWithAlignmentsTO forExport() {
        return new ActorWithAlignmentsTO(
                this.id,
                this.alignments.stream().map(DatasimAlignmentTO::forExport).collect(Collectors.toSet())
        );
    }
}
