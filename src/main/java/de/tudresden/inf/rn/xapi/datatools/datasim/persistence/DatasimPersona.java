package de.tudresden.inf.rn.xapi.datatools.datasim.persistence;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DatasimPersona implements DatasimActor {
    @GeneratedValue
    @Id
    @Getter
    private UUID id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String mbox;

    public DatasimPersona(String name, String mbox) {
        this.name = name;
        this.mbox = mbox;
    }

    @Override
    public String getIri() {
        return this.mbox.replace(":", "::");
    }

    @Override
    public DatasimActorType getType() {
        return DatasimActorType.AGENT;
    }
}
