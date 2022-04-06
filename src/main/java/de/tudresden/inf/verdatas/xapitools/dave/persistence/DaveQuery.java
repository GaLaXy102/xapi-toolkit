package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Document representing a Query
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Document("daveQuery")
@NoArgsConstructor
public class DaveQuery extends AbstractDocument {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String query;

    /**
     * Create a new Query
     *
     * @param name  Title of the Query
     * @param query definition of a query to execute
     */
    public DaveQuery(String name, String query) {
        this.name = name;
        this.query = query;
    }
}
