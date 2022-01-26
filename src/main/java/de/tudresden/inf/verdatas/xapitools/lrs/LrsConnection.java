package de.tudresden.inf.verdatas.xapitools.lrs;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.net.URL;
import java.util.UUID;

/**
 * Connection Entity for LRS
 * <p>
 * This class is used for persistence only.
 * Never send or accept this in a request.
 * Instead use {@link LrsConnectionTO}
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Entity
@Validated
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

    /**
     * Create a new LRS Connection persistence unit
     * <p>
     * The created entity will be enabled by default and receive a generated UUID.
     * To set properties, use the Setters instead.
     */
    LrsConnection(@NonNull String friendlyName, @NonNull URL xApiEndpoint, @NonNull String xApiClientKey, @NonNull String xApiClientSecret) {
        this.friendlyName = friendlyName;
        this.xApiEndpoint = xApiEndpoint;
        this.xApiClientKey = xApiClientKey;
        this.xApiClientSecret = xApiClientSecret;
        this.enabled = true;
    }
}
