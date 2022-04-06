package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Id;
import java.util.UUID;

/**
 * Base Mongo Document class. Contains only an ID abstraction.
 */
@NoArgsConstructor
public class AbstractDocument {
    @Id
    @Getter
    @Setter
    private UUID id = UUID.randomUUID();
}
