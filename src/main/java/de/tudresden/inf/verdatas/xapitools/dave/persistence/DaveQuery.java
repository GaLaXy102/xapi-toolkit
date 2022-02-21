package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("daveQuery")
@NoArgsConstructor
public class DaveQuery extends AbstractDocument {
    @Getter
    @Setter
    private String query;

    public DaveQuery(String name, String query) {
        super(name);
        this.query = query;
    }
}
