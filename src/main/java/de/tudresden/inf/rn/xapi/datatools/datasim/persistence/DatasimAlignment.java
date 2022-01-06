package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.net.URL;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatasimAlignment {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter
    private URL component;

    @Getter
    @Setter
    private Integer weight;

    public DatasimAlignment(URL component, Integer weight) {
        this.component = component;
        this.weight = weight;
    }
}
