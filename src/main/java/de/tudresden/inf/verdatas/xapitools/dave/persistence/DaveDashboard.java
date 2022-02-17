package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.List;

@Document("daveDashboard")
public class DaveDashboard extends AbstractDocument {
    @Getter
    @Setter
    @ManyToOne
    private LrsConnection lrsConnection;

    @Getter
    @Setter
    @ManyToMany
    private List<DaveVis> visualisations;

    @Getter
    @Setter
    @ManyToMany
    private List<String> visActivities;

    public DaveDashboard(LrsConnection lrsConnection, List<DaveVis> visualisations, List<String> visActivities) {
        this.lrsConnection = lrsConnection;
        this.visualisations = visualisations;
        this.visActivities = visActivities;
    }
}
