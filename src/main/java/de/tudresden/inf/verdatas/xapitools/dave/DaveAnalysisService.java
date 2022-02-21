package de.tudresden.inf.verdatas.xapitools.dave;

import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnector;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.*;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@DependsOn("daveVisSeeder")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
// TODO Zugriff auf Dokumente und deren Inhalt nur mit Überprüfung, ob vorhanden!! Auch in Controllern prüfen!!
public class DaveAnalysisService {
    private final DaveDashboardRepository dashboardRepository;
    private final DaveVisRepository visRepository;
    private final DaveQueryRepository queryRepository;
    private final DaveGraphDescriptionRepository graphDescriptionRepository;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;

    /**
     * UI helper enum, controls movement of analysis
     */
    public enum Move {
        UP,
        DOWN;
    }

    public DaveConnector getDaveConnector(LrsConnection lrsConnection) {
        return this.daveConnectorLifecycleManager.getConnector(lrsConnection);
    }

    public Stream<DaveDashboard> getAllDashboards() {
        return this.dashboardRepository.findAll().stream();
    }

    public Stream<DaveDashboard> getFinalizedDashboards() {
        return this.dashboardRepository.findAll().stream().filter(DaveDashboard::isFinalized);
    }

    public DaveDashboard getDashboard(UUID dashboardId) {
        return this.dashboardRepository.findById(dashboardId).orElseThrow(() -> new NoSuchElementException("No such dashboard."));
    }

    public Stream<DaveVis> getAllAnalysis() {
        return this.visRepository.findAll().stream();
    }

    public DaveVis getAnalysisById(UUID analysisId) {
        return this.visRepository.findById(analysisId).orElseThrow(() -> new NoSuchElementException("No such analysis."));
    }

    public DaveVis getAnalysisByName(String name) {
        return this.visRepository.findByName(name).orElseThrow(() -> new NoSuchElementException("No such analysis."));
    }

    //public List<String> getPathsForAnalysisDescription(DaveVis vis, DaveConnector connector) {}

    public List<Pair<URL, DaveVis>> getVisualisationsOfDashboard(DaveDashboard dashboard) {
        return dashboard.getVisualisations();
    }

    @Transactional
    public DaveDashboard createEmptyDashboard() {
        DaveDashboard emptyDashboard = new DaveDashboard(null,null, new LinkedList<>(), false);
        this.dashboardRepository.save(emptyDashboard);
        return emptyDashboard;
    }

    @Transactional
    public void setDashboardName(DaveDashboard dashboard, String name) {
        dashboard.setName(name);
        this.dashboardRepository.save(dashboard);
    }

    @Transactional
    public void setDashboardSource(DaveDashboard dashboard, LrsConnection lrsConnection) {
        dashboard.setLrsConnection(lrsConnection);
        this.dashboardRepository.save(dashboard);
    }

    @Transactional
    public void setDashboardVisualisations(DaveDashboard dashboard, List<Pair<URL, DaveVis>> visualisations) {
        dashboard.setVisualisations(visualisations);
        this.dashboardRepository.save(dashboard);
    }

    // TODO use path from DaveVis Object
    public List<String> getActivitiesOfLrs(LrsConnection connection) {
        List<String> paths = List.of("/home/ylvion/Downloads/query (5).json", "/home/ylvion/Downloads/query (6).json");
        List<String> activities = this.daveConnectorLifecycleManager.getConnector(connection).getAnalysisResult(paths);
        return activities.stream().map((s) -> s.substring(1, s.length() - 1)).map((s) -> s.split(" ")[1]).map((s) -> s.replace("\"", "")).toList();
    }

    @Transactional
    public void addVisualisationToDashboard(DaveDashboard dashboard, String activityId, DaveVis analysis) {
        List<Pair<URL, DaveVis>> visualisations = dashboard.getVisualisations();
        URL activityUrl = null;
        try {
            if (activityId.equals("all")) {
                activityUrl = new URL("http://all");
            } else {
                activityUrl = new URL(activityId);
            }
        } catch (MalformedURLException e) {
            new MalformedURLException("Unsuccessfull conversion of " + activityId + " to URL.");
        }
        visualisations.add(Pair.of(activityUrl, analysis));
        this.setDashboardVisualisations(dashboard, visualisations);
    }

    @Transactional
    public void moveVisualisationOfDashboard(DaveDashboard dashboard, int position, Move move) {
        List<Pair<URL, DaveVis>> visualisations = getVisualisationsOfDashboard(dashboard);
        if (move.equals(Move.UP)) {
            Pair<URL, DaveVis> vis = visualisations.remove(position);
            visualisations.add(position - 1, vis);

        } else {
            Pair<URL, DaveVis> vis = visualisations.remove(position);
            visualisations.add(position + 1, vis);
        }
        this.setDashboardVisualisations(dashboard, visualisations);
    }

    @Transactional
    public void deleteVisualisationFromDashboard(DaveDashboard dashboard, int position) {
        List<Pair<URL, DaveVis>> visualisations = getVisualisationsOfDashboard(dashboard);
        visualisations.remove(position);
        this.setDashboardVisualisations(dashboard, visualisations);
    }

    @Transactional
    public void finalizeDashboard(DaveDashboard dashboard) {
        if (dashboard.getVisualisations().isEmpty()) {
            throw new IllegalStateException("Dashboards must have at least one analysis.");
        }
        dashboard.setFinalized(true);
        this.dashboardRepository.save(dashboard);
    }
}
