package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.util.Pair;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.UUID;

/**
 * Document representing a Dashboard
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Document("daveDashboard")
@NoArgsConstructor
public class DaveDashboard extends AbstractDocument {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    @ManyToOne
    private LrsConnection lrsConnection;

    @Getter
    @Setter
    @ManyToMany
    private List<Pair<String, UUID>> visualisations;

    @Getter
    @Setter
    private boolean finalized;

    /**
     * Create a new Dashboard
     *
     * @param name           Title of the Dashboard
     * @param lrsConnection  {@link LrsConnection} to use as data source
     * @param visualisations {@link List} of {@link Pair}s, which contain an indication if the Analysis' execution is limited to a specific activity or the whole associated LRS and the UUID of the used Analysis
     * @param finalized      indicates if the Dashboards' configuration is completed
     */
    public DaveDashboard(String name, LrsConnection lrsConnection, List<Pair<String, UUID>> visualisations, boolean finalized) {
        this.name = name;
        this.lrsConnection = lrsConnection;
        this.visualisations = visualisations;
        this.finalized = finalized;
    }
}
