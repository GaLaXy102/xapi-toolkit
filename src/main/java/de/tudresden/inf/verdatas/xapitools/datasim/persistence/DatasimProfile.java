package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

/**
 * Hibernate Entity representing a DATASIM Profile
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
@Entity
@Validated
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class DatasimProfile {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    @NotBlank
    private String name;

    @Getter
    @NotBlank
    private String filename;

    /**
     * Create a new Profile
     *
     * @param name     Friendly name of the Profile
     * @param filename Filename (relative to classpath:/xapi/profiles; see {@link DatasimProfileTO}
     */
    DatasimProfile(String name, String filename) {
        this.name = name;
        this.filename = filename;
    }
}
