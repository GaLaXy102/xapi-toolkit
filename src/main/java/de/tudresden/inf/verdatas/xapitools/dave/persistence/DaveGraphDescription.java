package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("daveGraphDescription")
@NoArgsConstructor
public class DaveGraphDescription extends AbstractDocument {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    public DaveGraphDescription(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
