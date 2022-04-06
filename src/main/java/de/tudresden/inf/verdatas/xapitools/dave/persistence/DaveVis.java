package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.ManyToOne;

/**
 * Document representing an Analysis
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
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

    /**
     * Create a new Dashboard
     *
     * @param name        Title of the Dashboard
     * @param query       {@link DaveQuery} to use as query
     * @param description {@link DaveGraphDescription} to use to describe the diagram to this Analysis
     * @param finalized   indicates if the Analysis' configuration is completed
     */
    public DaveVis(String name, DaveQuery query, DaveGraphDescription description, boolean finalized) {
        this.name = name;
        this.query = query;
        this.description = description;
        this.finalized = finalized;
    }
}
