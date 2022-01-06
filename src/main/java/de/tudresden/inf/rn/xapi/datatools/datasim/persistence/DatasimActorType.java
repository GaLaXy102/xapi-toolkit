package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.Getter;

public enum DatasimActorType {

    AGENT("Agent"),
    GROUP("Group");

    @Getter
    private final String value;

    DatasimActorType(String value) {
        this.value = value;
    }
}
