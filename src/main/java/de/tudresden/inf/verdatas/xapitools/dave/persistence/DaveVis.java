package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("daveVis")
public class DaveVis extends AbstractDocument {
    @Getter
    @Setter
    private DaveQuery query;

    @Getter
    @Setter
    private DaveGraphDescription description;
}
