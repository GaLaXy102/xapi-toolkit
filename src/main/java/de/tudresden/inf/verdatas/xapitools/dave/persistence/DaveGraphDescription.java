package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("daveGraphDescription")
public class DaveGraphDescription extends AbstractDocument {
    @Getter
    @Setter
    private String description;
}
