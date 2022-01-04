package de.tudresden.inf.rn.xapi.datatools.lrs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
final class LrsConnectionTO {
    @NonNull @Getter private final Optional<UUID> uuid;
    @NonNull @NotBlank @Getter private final String name;
    @NonNull @Getter private final URL endpoint;
    @NonNull @NotBlank @Getter private final String clientKey;
    @NonNull @NotBlank @Getter private final String clientSecret;
    @NonNull @Getter private final Optional<Boolean> enabled;

    LrsConnection toNewLrsConnection() {
        LrsConnection result = new LrsConnection(
                this.getName(),
                this.getEndpoint(),
                this.getClientKey(),
                this.getClientSecret()
        );
        result.setEnabled(true);
        return result;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LrsConnectionTO that = (LrsConnectionTO) o;
        return uuid.equals(that.uuid) && name.equals(that.name) && endpoint.equals(that.endpoint) && clientKey.equals(that.clientKey) && clientSecret.equals(that.clientSecret) && enabled.equals(that.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, endpoint, clientKey, clientSecret, enabled);
    }
}
