package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.ManyToOne;

@Document("daveVis")
@NoArgsConstructor
public class DaveVis extends AbstractDocument {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    @ManyToOne
    @Cascade(CascadeType.ALL)
    private DaveQuery query;

    @Getter
    @Setter
    @ManyToOne
    @Cascade(CascadeType.ALL)
    private DaveGraphDescription description;

    @Getter
    @Setter
    private boolean finalized;

    public DaveVis(String name, DaveQuery query, DaveGraphDescription description, boolean finalized) {
        this.name = name;
        this.query = query;
        this.description = description;
        this.finalized = finalized;
    }
}
