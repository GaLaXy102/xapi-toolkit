package de.tudresden.inf.verdatas.xapitools.dave.dashboards;

import de.tudresden.inf.verdatas.xapitools.dave.FileManagementService;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnector;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.*;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
@DependsOn("daveVisSeeder")
@EnableScheduling
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveDashboardService {
    private final DaveDashboardRepository dashboardRepository;
    private final DaveVisRepository visRepository;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;
    private final FileManagementService fileManagementService;

    private final Logger logger = Logger.getLogger(DaveDashboardService.class.getName());

    /**
     * UI helper enum, controls movement of analysis
     */
    public enum Move {
        UP,
        DOWN
    }

    public DaveConnector getDaveConnector(LrsConnection lrsConnection) {
        return this.daveConnectorLifecycleManager.getConnector(lrsConnection);
    }

    public Stream<DaveDashboard> getAllDashboards(boolean finalizedOnly) {
        if (finalizedOnly) {
            return this.dashboardRepository.findAllByFinalizedIsTrue().stream();
        } else {
            return this.dashboardRepository.findAll().stream();
        }
    }

    public DaveDashboard getDashboard(UUID dashboardId) {
        return this.dashboardRepository.findById(dashboardId).orElseThrow(() -> new NoSuchElementException("No such dashboard."));
    }

    public Stream<DaveVis> getAllAnalysis(boolean finalizedOnly) {
        if (finalizedOnly) {
            return this.visRepository.findAllByFinalizedIsTrue().stream();
        } else {
            return this.visRepository.findAll().stream();
        }
    }

    public DaveVis getAnalysisById(UUID analysisId) {
        return this.visRepository.findById(analysisId).orElseThrow(() -> new NoSuchElementException("No such analysis."));
    }

    public DaveVis getAnalysisByName(String name) {
        return this.visRepository.findByName(name).orElseThrow(() -> new NoSuchElementException("No such analysis."));
    }

    public List<Pair<String, DaveVis>> getVisualisationsOfDashboard(DaveDashboard dashboard) {
        List<Pair<String, UUID>> visualisations = dashboard.getVisualisations();
        List<Pair<String, DaveVis>> analysis = new LinkedList<>();
        for (Pair<String, UUID> vis:
             visualisations) {
            analysis.add(Pair.of(vis.getFirst(), this.getAnalysisById(vis.getSecond())));
        }
        return analysis;
    }

    @Transactional
    public DaveDashboard createEmptyDashboard() {
        DaveDashboard emptyDashboard = new DaveDashboard(null,null, new LinkedList<>(), false);
        this.dashboardRepository.save(emptyDashboard);
        return emptyDashboard;
    }

    @Transactional
    public DaveDashboard createCopyOfDashboard(DaveDashboard dashboard) {
        DaveDashboard created = this.createEmptyDashboard();
        this.setDashboardName(created, "Copy of " + dashboard.getName());
        this.setDashboardSource(created, dashboard.getLrsConnection());
        this.setDashboardVisualisations(created, dashboard.getVisualisations());
        return created;
    }

    @Transactional
    public void deleteDashboard(DaveDashboard dashboard) {
        this.dashboardRepository.delete(dashboard);
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
    public void setDashboardVisualisations(DaveDashboard dashboard, List<Pair<String, UUID>> visualisations) {
        dashboard.setVisualisations(visualisations);
        this.dashboardRepository.save(dashboard);
    }

    public void checkDashboardConfiguration(DaveDashboard dashboard) {
        if (!(dashboard.getLrsConnection() == null || dashboard.getVisualisations().isEmpty())) {
            this.finalizeDashboard(dashboard);
        } else {
            if (dashboard.isFinalized()) {
                throw new IllegalStateException("Dashboards must have a LRS connection and at least one analysis.");
            }
        }
    }

    // TODO Hinweis in Benutzerdokumentation
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    @CacheEvict(
            cacheNames = "lrsActivities",
            allEntries = true
    )
    public void cleanCaches() {
        this.logger.info("Cleaned Caches.");
    }

    @Cacheable(
            cacheNames = "lrsActivities",
            key = "#connection.connectionId"
    )
    public List<String> getActivitiesOfLrs(LrsConnection connection) {
        DaveVis getActivities = this.prepareGetActivitiesOfLRS();
        List<String> activities = this.daveConnectorLifecycleManager.getConnector(connection)
                .getAnalysisResult(this.fileManagementService.prepareQuery(getActivities, "all").getAbsolutePath(),
                        this.fileManagementService.prepareVisualisation(getActivities).getAbsolutePath());
        return activities.stream().map((s) -> s.substring(1, s.length() - 1)).map((s) -> s.split(" ")[1]).map((s) -> s.replace("\"", "")).toList();
    }

    @Transactional
    public void addVisualisationToDashboard(DaveDashboard dashboard, String activityId, UUID analysisId) {
        List<Pair<String, UUID>> visualisations = dashboard.getVisualisations();
        visualisations.add(Pair.of(activityId, analysisId));
        this.setDashboardVisualisations(dashboard, visualisations);
    }

    @Transactional
    public void shiftPositionOfVisualisationOfDashboard(DaveDashboard dashboard, int position, Move move) {
        List<Pair<String, UUID>> visualisations = dashboard.getVisualisations();
        Pair<String, UUID> vis = visualisations.remove(position);
        if (move.equals(Move.UP)) {
            visualisations.add(position - 1, vis);
        } else {
            visualisations.add(position + 1, vis);
        }
        this.setDashboardVisualisations(dashboard, visualisations);
    }

    @Transactional
    public void deleteVisualisationFromDashboard(DaveDashboard dashboard, int position) {
        List<Pair<String, UUID>> visualisations = dashboard.getVisualisations();
        visualisations.remove(position);
        this.setDashboardVisualisations(dashboard, visualisations);
    }

    @Transactional
    public void finalizeDashboard(DaveDashboard dashboard) {
        if (dashboard.getLrsConnection() == null) {
            throw new IllegalStateException("Dashboards must have a LRS Connection.");
        } else if (dashboard.getVisualisations().isEmpty()) {
            throw new IllegalStateException("Dashboards must have at least one analysis.");
        }
        dashboard.setFinalized(true);
        this.dashboardRepository.save(dashboard);
    }

    public static String prepareQueryLimit(DaveVis analysis, Optional<String> activityId) {
        String query = analysis.getQuery().getQuery();
        if (activityId.isPresent()) {
            query = query.substring(0, query.length() - 1) +
                    "[?s :statement/object ?so][?so :activity/id \"" + activityId.get() + "\"]"
                    + "]";
        }
        return query;
    }

    public String executeVisualisationOfDashboard(DaveDashboard dashboard, String activityURL, UUID analysisId) {
        DaveConnector connector = this.daveConnectorLifecycleManager.getConnector(dashboard.getLrsConnection());
        return dashboard.getVisualisations()
                .stream()
                .filter((vis) -> vis.getFirst().equals(activityURL) && vis.getSecond().equals(analysisId))
                .findFirst()
                .map((v) -> Pair.of(
                        this.fileManagementService
                                .prepareQuery(this.getAnalysisById(v.getSecond()), v.getFirst()),
                        this.fileManagementService
                                .prepareVisualisation(this.getAnalysisById(v.getSecond()))))
                .map((prep) ->
                        Pair.of(
                                connector.executeAnalysis(prep.getFirst().getAbsolutePath(), prep.getSecond().getAbsolutePath()),
                                Pair.of(prep.getFirst(), prep.getSecond())
                        ))
                .map((visAndFiles) -> {
                    visAndFiles.getSecond().getFirst().delete();
                    visAndFiles.getSecond().getSecond().delete();
                    return visAndFiles.getFirst();
                })
                .orElseThrow(() -> new NoSuchElementException("Could not find dashboard visualisation for execution."));
    }

    public String getNameOfAnalysis(UUID analysisId) {
        return this.getAnalysisById(analysisId).getName();
    }

    public DaveVis prepareGetActivitiesOfLRS() {
        return new DaveVis("Activities of LRS",
                new DaveQuery("Activities of LRS", """
                        [:find (count ?s) ?c :where [?s :statement/object ?o][?o :activity/id ?c]]"""),
                new DaveGraphDescription("Top 10",
                        """
                                {
                                  "$schema": "https://vega.github.io/schema/vega/v5.json",
                                  "width": 400,
                                  "height": 200,
                                  "padding": 15,

                                  "data": [
                                    {     \s
                                      "name": "table",
                                      "source": "result",
                                      "transform": [
                                        { "type": "collect", "sort": {"field": "count_?s", "order" : "descending"} },
                                        {
                                          "type": "window",
                                          "sort": {"field": "count_?s", "order": "descending"},
                                          "ops": ["rank"],
                                          "fields": [null],
                                          "as": ["rank"]
                                        },
                                       \s
                                        { "type": "filter", "expr": "datum.rank < 11"}
                                      ]
                                    }
                                  ],

                                  "signals": [
                                    {
                                      "name": "tooltip",
                                      "value": {},
                                      "on": [
                                        {"events": "rect:mouseover", "update": "datum"},
                                        {"events": "rect:mouseout",  "update": "{}"}
                                      ]
                                    }
                                  ],

                                  "scales": [
                                    {
                                      "name": "xscale",
                                      "type": "band",
                                      "domain": {"data": "table", "field": "?c"},
                                      "range": "width",
                                      "padding": 0.05,
                                      "round": true
                                    },
                                    {
                                      "name": "yscale",
                                      "domain": {"data": "table", "field": "count_?s"},
                                      "nice": true,
                                      "range": "height"
                                    }
                                  ],

                                  "axes": [
                                    { "orient": "bottom", "scale": "xscale", "labelAngle": -35, "zindex": 2 },
                                    { "orient": "left", "scale": "yscale" }
                                  ],

                                  "marks": [
                                    {
                                      "type": "rect",
                                      "from": {"data":"table"},
                                      "encode": {
                                        "enter": {
                                          "x": {"scale": "xscale", "field": "?c"},
                                          "width": {"scale": "xscale", "band": 1},
                                          "y": {"scale": "yscale", "field": "count_?s"},
                                          "y2": {"scale": "yscale", "value": 0}
                                        },
                                        "update": {
                                          "fill": {"value": "steelblue"}
                                        },
                                        "hover": {
                                          "fill": {"value": "red"}
                                        }
                                      }
                                    }
                                  ]
                                }"""), true);
    }
}
