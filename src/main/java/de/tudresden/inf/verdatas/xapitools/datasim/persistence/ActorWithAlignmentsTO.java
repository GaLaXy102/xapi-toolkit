package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Transfer Object for Communication with DATASIM, mapping an Actor to his Alignments
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
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

    /**
     * Create a TO with the specified Actor and Alignments
     */
    public static ActorWithAlignmentsTO with(DatasimActor actor, Set<DatasimAlignmentTO> alignments) {
        return new ActorWithAlignmentsTO(
                actor.getIri(),
                alignments
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
        return this.id;
    }

    /**
     * Get the Type of the Actor
     *
     * @return Type, i.e. Agent or Group
     */
    @Override
    public DatasimActorType getType() {
        return DatasimActorType.AGENT;
    }

    /**
     * Recursive adaptions for sending to DATASIM or export
     */
    public ActorWithAlignmentsTO forExport() {
        return new ActorWithAlignmentsTO(
                this.id,
                this.alignments.stream().map(DatasimAlignmentTO::forExport).collect(Collectors.toSet())
        );
    }
}
