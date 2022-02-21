package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.OneToMany;

@Document("daveVis")
@NoArgsConstructor
public class DaveVis extends AbstractDocument {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    @OneToMany
    private DaveQuery query;

    @Getter
    @Setter
    @OneToMany
    private DaveGraphDescription description;

    public DaveVis(String name, DaveQuery query, DaveGraphDescription description) {
        this.name = name;
        this.query = query;
        this.description = description;
    }
}
