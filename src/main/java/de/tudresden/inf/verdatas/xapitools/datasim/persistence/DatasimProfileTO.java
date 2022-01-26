package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Transfer Object for Communication with DATASIM, representing a Profile
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Validated
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimProfileTO {
    @Getter
    @NonNull
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    private Optional<UUID> id;

    @Getter
    @Setter
    @NotBlank
    private String name;

    @Getter
    @Setter
    @NotBlank
    private String filename;

    /**
     * Create a TO from an Entity
     *
     * @param profile Base Entity to get a representation of
     * @return Decoupled Transfer Object
     */
    public static DatasimProfileTO of(DatasimProfile profile) {
        return new DatasimProfileTO(
                Optional.of(profile.getId()),
                profile.getName(),
                profile.getFilename()
        );
    }

    /**
     * Get the possible Alignments in this Profile by Type
     */
    public Map<DatasimProfileAlignableElementType, List<URL>> getPossibleAlignmentsByType() {
        return DatasimProfileAlignableElementHelper.calculatePossibleAlignments(this);
    }

    /**
     * The JSON representation of this document for export or communication with DATASIM.
     *
     * @return Content of the Profile document
     */
    @JsonValue
    public JsonNode getProfileContent() {
        JsonMapper objectMapper = new JsonMapper();
        try {
            return objectMapper.readTree(new ClassPathResource("xapi/profiles/" + this.filename).getFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }
}
