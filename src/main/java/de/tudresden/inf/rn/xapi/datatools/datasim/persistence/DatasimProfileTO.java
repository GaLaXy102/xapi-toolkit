package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.Optional;
import java.util.UUID;

@Validated
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimProfileTO {
    @Getter
    @NonNull
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
        return new DatasimProfileTO(Optional.of(profile.getId()), profile.getName(), profile.getFilename());
    }

    public DatasimProfile toNewDatasimProfile() {
        this.id.ifPresent((id) -> {throw new IllegalStateException("UUID must be empty when creating new.");});
        return new DatasimProfile(this.name, this.filename);
    }

    public DatasimProfile toExistingDatasimProfile() {
        return new DatasimProfile(this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")), this.name, this.filename);
    }
}
