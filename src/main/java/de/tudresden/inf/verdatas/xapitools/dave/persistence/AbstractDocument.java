package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

public class AbstractDocument {
    @Id
    @Getter
    private final UUID id = UUID.randomUUID();

    @Getter
    @Setter
    private String identifier;
}
