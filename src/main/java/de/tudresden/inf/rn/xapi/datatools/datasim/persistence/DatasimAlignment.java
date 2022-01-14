package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import de.tudresden.inf.rn.xapi.datatools.datasim.validators.AlignmentWeight;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.net.URL;
import java.util.UUID;

@Entity
@Validated
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimAlignment {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private URL component;

    @Getter
    @Setter
    @AlignmentWeight
    private Float weight;

    DatasimAlignment(URL component, Float weight) {
        this.component = component;
        this.weight = weight;
    }
}
