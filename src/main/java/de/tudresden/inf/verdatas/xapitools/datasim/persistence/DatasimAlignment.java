package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import de.tudresden.inf.verdatas.xapitools.datasim.validators.AlignmentWeight;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.net.URL;
import java.util.UUID;

/**
 * Hibernate Entity representing a DATASIM Alignment
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Entity
@Validated
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimAlignment {
    @GeneratedValue
    @Id
    @Getter
    @Column(columnDefinition = "BINARY(16)")  // Setting this manually is necessary as Hibernate does not handle it properly
    private UUID id;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private URL component;

    @Getter
    @Setter
    @AlignmentWeight
    private Float weight;

    /**
     * Create a new Alignment for the given Component and weight. It is mapped 1 -> n to a Persona
     *
     * @param component URL of the Component
     * @param weight    Weight of the Alignment
     */
    DatasimAlignment(URL component, Float weight) {
        this.component = component;
        this.weight = weight;
    }
}
