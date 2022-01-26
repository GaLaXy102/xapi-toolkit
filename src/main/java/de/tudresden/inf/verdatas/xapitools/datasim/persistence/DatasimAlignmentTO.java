package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.tudresden.inf.verdatas.xapitools.datasim.validators.AlignmentWeight;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

/**
 * Transfer Object for Communication with DATASIM, representing an Alignment
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Validated
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimAlignmentTO {
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<UUID> id;

    @Getter
    @Setter
    private URL component;

    @Getter
    @Setter
    @AlignmentWeight
    private Float weight;

    /**
     * Create a TO from an Entity
     *
     * @param alignment Base Entity to get a representation of
     * @return Decoupled Transfer Object
     */
    public static DatasimAlignmentTO of(DatasimAlignment alignment) {
        return new DatasimAlignmentTO(Optional.of(alignment.getId()), alignment.getComponent(), alignment.getWeight());
    }

    /**
     * Create a neutral (i.e. weight = 0) Alignment (TO) for a given component
     *
     * @param componentUrl URL of component to create an Alignment for
     * @return Neutral Alignment TO for further consuming
     */
    public static DatasimAlignmentTO neutral(URL componentUrl) {
        return new DatasimAlignmentTO(Optional.empty(), componentUrl, 0.0f);
    }

    /**
     * Create a new DatasimAlignment Entity for use in Services.
     *
     * @return persistable Entity
     * @throws IllegalArgumentException when the Alignment weight is invalid
     * @throws IllegalStateException    when the TO has its UUID set
     */
    public DatasimAlignment toNewDatasimAlignment() {
        if (Math.abs(this.weight) > 1) {
            throw new IllegalArgumentException("Alignment weight must be in [-1, 1]");
        }
        this.id.ifPresent((id) -> {
            throw new IllegalStateException("UUID must be empty when creating new.");
        });
        return new DatasimAlignment(this.component, this.weight);
    }

    /**
     * Create an "existing" (i.e. there is an Alignment with this ID) DatasimAlignment Entity for use in Services.
     *
     * @return persistable Entity
     * @throws IllegalArgumentException when the Alignment weight is invalid
     * @throws IllegalStateException    when the TO has no UUID set
     */
    DatasimAlignment toExistingDatasimAlignment() {
        if (Math.abs(this.weight) > 1) {
            throw new IllegalArgumentException("Alignment weight must be in [-1, 1]");
        }
        return new DatasimAlignment(this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")), this.component, this.weight);
    }

    /**
     * Adaptions for sending to DATASIM or export
     */
    public DatasimAlignmentTO forExport() {
        return new DatasimAlignmentTO(
                Optional.empty(),
                this.component,
                this.weight
        );
    }
}
