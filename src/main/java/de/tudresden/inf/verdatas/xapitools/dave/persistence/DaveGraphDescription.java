package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Document representing a Graph description
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Document("daveGraphDescription")
@NoArgsConstructor
public class DaveGraphDescription extends AbstractDocument {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    /**
     * Create a new Graph description
     *
     * @param name        Title of the Graph description
     * @param description to define a diagram with its parameters
     */
    public DaveGraphDescription(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
