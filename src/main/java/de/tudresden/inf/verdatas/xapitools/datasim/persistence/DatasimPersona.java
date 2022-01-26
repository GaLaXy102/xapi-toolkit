package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.util.Comparator;
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
public class DatasimPersona implements Comparable<DatasimPersona> {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @NotBlank
    private String name;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @NotBlank
    private String mbox;

    /**
     * Create a new Persona
     *
     * @param name Friendly name of the Persona
     * @param mbox Sample Mail Address used to identify a Persona
     */
    DatasimPersona(String name, String mbox) {
        this.name = name;
        this.mbox = mbox;
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(@NonNull DatasimPersona o) {
        return Comparator.comparing(DatasimPersona::getName).thenComparing(DatasimPersona::getMbox).thenComparing(DatasimPersona::getId).compare(this, o);
    }
}
