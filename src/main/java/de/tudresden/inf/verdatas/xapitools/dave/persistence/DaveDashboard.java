package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.util.Pair;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.net.URL;
import java.util.List;

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
    private List<Pair<URL, DaveVis>> visualisations;

    @Getter
    @Setter
    private boolean finalized;

    public DaveDashboard(String name, LrsConnection lrsConnection, List<Pair<URL, DaveVis>> visualisations, boolean finalized) {
        this.name = name;
        this.lrsConnection = lrsConnection;
        this.visualisations = visualisations;
        this.finalized = finalized;
    }
}
