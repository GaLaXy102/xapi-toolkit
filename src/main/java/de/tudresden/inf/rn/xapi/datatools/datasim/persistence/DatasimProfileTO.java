package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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

    public static DatasimProfileTO of(DatasimProfile profile) {
        return new DatasimProfileTO(
                Optional.of(profile.getId()),
                profile.getName(),
                profile.getFilename()
        );
    }

    public DatasimProfile toNewDatasimProfile() {
        this.id.ifPresent((id) -> {throw new IllegalStateException("UUID must be empty when creating new.");});
        return new DatasimProfile(this.name, this.filename);
    }

    public DatasimProfile toExistingDatasimProfile() {
        return new DatasimProfile(this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")), this.name, this.filename);
    }

    public Map<DatasimProfileAlignableElementType, List<URL>> getPossibleAlignmentsByType() {
        return DatasimProfileAlignableElementHelper.calculatePossibleAlignments(this);
    }

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
