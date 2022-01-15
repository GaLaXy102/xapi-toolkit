package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum DatasimActorType {

    AGENT("Agent"),
    GROUP("Group");

    @Getter
    @JsonValue
    private final String value;

    DatasimActorType(String value) {
        this.value = value;
    }
}
