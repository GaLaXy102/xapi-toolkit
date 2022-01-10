package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DatasimAlignmentTO {
    @Getter
    private Optional<UUID> id;

    @Getter
    @Setter
    private URL component;

    @Getter
    @Setter
    private Float weight;

    public static DatasimAlignmentTO of(DatasimAlignment alignment) {
        return new DatasimAlignmentTO(Optional.of(alignment.getId()), alignment.getComponent(), alignment.getWeight());
    }

    public static DatasimAlignmentTO neutral(URL componentUrl) {
        return new DatasimAlignmentTO(Optional.empty(), componentUrl, 0.0f);
    }

    public DatasimAlignment toNewDatasimAlignment() {
        if (Math.abs(this.weight) > 1) {
            throw new IllegalArgumentException("Alignment weight must be in [-1, 1]");
        }
        this.id.ifPresent((id) -> {throw new IllegalStateException("UUID must be empty when creating new.");});
        return new DatasimAlignment(this.component, this.weight);
    }

    DatasimAlignment toExistingDatasimAlignment() {
        if (Math.abs(this.weight) > 1) {
            throw new IllegalArgumentException("Alignment weight must be in [-1, 1]");
        }
        return new DatasimAlignment(this.id.orElseThrow(() -> new IllegalStateException("UUID must not be empty when updating.")), this.component, this.weight);
    }
}
