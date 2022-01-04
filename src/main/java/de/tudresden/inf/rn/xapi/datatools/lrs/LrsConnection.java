package de.tudresden.inf.rn.xapi.datatools.lrs;

import lombok.*;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.net.URL;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LrsConnection {
    @GeneratedValue
    @Id
    @Getter
    private UUID connectionId;

    @Getter
    @Setter
    @NonNull
    @NotBlank
    private String friendlyName;

    @Getter
    @Setter
    @NonNull
    private URL xApiEndpoint;

    @Getter
    @Setter
    @NonNull
    @NotBlank
    private String xApiClientKey;

    @Getter
    @Setter
    @NonNull
    @NotBlank
    private String xApiClientSecret;

    @Getter
    @Setter
    private boolean enabled;

    public LrsConnection(@NonNull String friendlyName, @NonNull URL xApiEndpoint, @NonNull String xApiClientKey, @NonNull String xApiClientSecret) {
        this.friendlyName = friendlyName;
        this.xApiEndpoint = xApiEndpoint;
        this.xApiClientKey = xApiClientKey;
        this.xApiClientSecret = xApiClientSecret;
    }
}
