package de.tudresden.inf.rn.xapi.datatools.lrs;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

/**
 * Transfer Object for {@link LrsConnection}
 *
 * Always use this class to for transfer from or to client.
 */
@AllArgsConstructor
@EqualsAndHashCode
final class LrsConnectionTO {
    @NonNull @Getter private final Optional<UUID> uuid;
    @NonNull @NotBlank @Getter private final String name;
    @NonNull @Getter private final URL endpoint;
    @NonNull @NotBlank @Getter private final String clientKey;
    @NonNull @NotBlank @Getter private final String clientSecret;
    @NonNull @Getter private final Optional<Boolean> enabled;

    /**
     * Create a new LRS connection Entity from incoming data
     *
     * The UUID will be generated and the entity will be in enabled state.
     */
    LrsConnection toNewLrsConnection() {
        return new LrsConnection(
                this.getName(),
                this.getEndpoint(),
                this.getClientKey(),
                this.getClientSecret()
        );
    }

    /**
     * Create a Transfer Object from a given LRS connection entity for safe exchange.
     */
    static LrsConnectionTO of(LrsConnection connection) {
        return new LrsConnectionTO(
                Optional.of(connection.getConnectionId()),
                connection.getFriendlyName(),
                connection.getXApiEndpoint(),
                connection.getXApiClientKey(),
                connection.getXApiClientSecret(),
                Optional.of(connection.isEnabled())
                );
    }
}
