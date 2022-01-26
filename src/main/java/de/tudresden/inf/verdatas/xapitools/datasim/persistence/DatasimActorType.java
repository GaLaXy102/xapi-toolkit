package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * An Actor Type from the DATASIM API specification
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public enum DatasimActorType {
    /**
     * An Agent is a single Persona
     */
    AGENT("Agent"),
    /**
     * A Group is a collection of one or more Personae
     */
    GROUP("Group");

    @Getter
    @JsonValue
    private final String value;

    DatasimActorType(String value) {
        this.value = value;
    }
}
