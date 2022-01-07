package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimProfileTO {
    @Getter
    private Optional<UUID> id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
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
