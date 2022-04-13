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
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This service handles all Entity-related concerns to configure a Dashboard
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Service
@EnableScheduling
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveDashboardService {
    private final DaveDashboardRepository dashboardRepository;
    private final DaveVisRepository visRepository;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;
    private final FileManagementService fileManagementService;

    private final Logger logger = Logger.getLogger(DaveDashboardService.class.getName());

    /**
     * Modify the query of the given Analysis to be executed using the data of a special Activity of an LRS or the whole data set
     *
     * @param analysis   Entity to use
     * @param activityId URL, indicates if the Analysis is executed using the whole LRS data set or only the data belonging to a specific activity
     * @return the modified query description
     */
    public static String prepareQueryLimit(DaveVis analysis, Optional<String> activityId) {
        String query = analysis.getQuery().getQuery();
        if (activityId.isPresent()) {
            query = query.substring(0, query.length() - 1) +
                    "[?s :statement/object ?so][?so :activity/id \"" + activityId.get() + "\"]"
                    + "]";
        }
        return query;
    }

    /**
     * Get all Dashboard descriptions saved in the System
     *
     * @param finalizedOnly if true: filters for Dashboards, whose configuration is completed
     */
    public Stream<DaveDashboard> getAllDashboards(boolean finalizedOnly) {
        if (finalizedOnly) {
            return this.dashboardRepository.findAllByFinalizedIsTrue().stream();
        } else {
            return this.dashboardRepository.findAll().stream();
        }
    }

    /**
     * Get a Dashboard description by its ID
     *
     * @param dashboardId UUID of the Dashboard to find
     * @throws NoSuchElementException when the ID is not known to the system
     */
    public DaveDashboard getDashboard(UUID dashboardId) {
        return this.dashboardRepository.findById(dashboardId).orElseThrow(() -> new NoSuchElementException("No such dashboard."));
    }

    /**
     * Get all Analysis descriptions saved in the System
     *
     * @param finalizedOnly if true: filters for Analyses, whose configuration is completed
     */
    public Stream<DaveVis> getAllAnalysis(boolean finalizedOnly) {
        if (finalizedOnly) {
            return this.visRepository.findAllByFinalizedIsTrue().stream();
        } else {
            return this.visRepository.findAll().stream();
        }
    }

    /**
     * Get an Analysis description by its ID
     *
     * @param analysisId UUID of the Analysis to find
     * @throws NoSuchElementException when the ID is not known to the system
     */
    public DaveVis getAnalysisById(UUID analysisId) {
        return this.visRepository.findById(analysisId).orElseThrow(() -> new NoSuchElementException("No such analysis."));
    }

    /**
     * Get an Analysis description by its Title
     *
     * @param name Title of the Analysis to find
     * @throws NoSuchElementException when the Title is not known to the system
     */
    public DaveVis getAnalysisByName(String name) {
        return this.visRepository.findByName(name).orElseThrow(() -> new NoSuchElementException("No such analysis."));
    }

    /**
     * Get all Analyses of an existing Dashboard.
     * If an Analysis' execution was configured to use a specific activity of the associated LRS as data set, the corresponding activityID will be specified for this Analysis in the Dashboard's Analyses List.
     * Else, the entry for this Analysis will contain the {@link String} "all".
     *
     * @param dashboard Entity to use
     * @return {@link List} of {@link Pair}s, which contain an indication if the Analysis' execution is limited to a specific activity or the whole associated LRS and the UUID of the used Analysis
     */
    public List<Pair<String, DaveVis>> getVisualisationsOfDashboard(DaveDashboard dashboard) {
        List<Pair<String, UUID>> visualisations = dashboard.getVisualisations();
        List<Pair<String, DaveVis>> analysis = new LinkedList<>();
        for (Pair<String, UUID> vis : visualisations) {
            analysis.add(Pair.of(vis.getFirst(), this.getAnalysisById(vis.getSecond())));
        }
        return analysis;
    }

    /**
     * Create a Dashboard description skeleton.
     * The created entity has nothing attached to it and its parameters are chosen to have somewhat reasonable values.
     *
     * @param name Title of the Dashboard. Has to be set to prevent {@link NullPointerException} when showing all Dashboards. Must not be already used
     * @throws de.tudresden.inf.verdatas.xapitools.dave.dashboards.DashboardExceptions.ConfigurationConflict when {@param name} is already used
     */
    @Transactional
    public DaveDashboard createEmptyDashboard(String name) {
        // Detect duplicates
        this.dashboardRepository.findByName(name)
                .ifPresent((d) -> {
                            throw new DashboardExceptions.ConfigurationConflict(
                                    "Duplicate dashboard identifier. Please change the name of your dashboard.");
                        }
                );
        DaveDashboard emptyDashboard = new DaveDashboard(name, null, new LinkedList<>(), false);
        this.dashboardRepository.save(emptyDashboard);
        return emptyDashboard;
    }

    /**
     * Create and persist a copy of an existing Dashboard description
     *
     * @param dashboard Entity to copy
     */
    @Transactional
    public DaveDashboard createCopyOfDashboard(DaveDashboard dashboard) {
        DaveDashboard created = this.createEmptyDashboard("Copy of " + dashboard.getName());
        this.setDashboardSource(created, dashboard.getLrsConnection());
        this.setDashboardVisualisations(created, dashboard.getVisualisations());
        return created;
    }

    /**
     * Delete a Dashboard description
     *
     * @param dashboard Entity to delete
     */
    @Transactional
    public void deleteDashboard(DaveDashboard dashboard) {
        this.dashboardRepository.delete(dashboard);
    }

    /**
     * Set and persist the Title of the Dashboard
     *
     * @param dashboard Entity to modify
     * @param name      modified Title of the Dashboard
     */
    @Transactional
    public void setDashboardName(DaveDashboard dashboard, String name) {
        dashboard.setName(name);
        this.dashboardRepository.save(dashboard);
    }

    /**
     * Set and persist the source LRS of the Dashboard
     *
     * @param dashboard     Entity to modify
     * @param lrsConnection {@link LrsConnection} to use as data source
     */
    @Transactional
    public void setDashboardSource(DaveDashboard dashboard, LrsConnection lrsConnection) {
        dashboard.setLrsConnection(lrsConnection);
        this.dashboardRepository.save(dashboard);
    }

    /**
     * Set and persist the Analyses of the Dashboard.
     * If an Analysis' execution was configured to use a specific activity of the associated LRS as data set, the corresponding activityID will be specified for this Analysis in the Dashboard's Analyses List.
     * Else, the entry for this Analysis will contain the {@link String} "all".
     *
     * @param dashboard      Entity to modify
     * @param visualisations {@link List} of {@link Pair}s, which contain an indication if the Analysis' execution is limited to a specific activity or the whole associated LRS and the UUID of the used Analysis
     */
    @Transactional
    public void setDashboardVisualisations(DaveDashboard dashboard, List<Pair<String, UUID>> visualisations) {
        dashboard.setVisualisations(visualisations);
        if (visualisations.isEmpty()) {
            dashboard.setFinalized(false);
        }
        this.dashboardRepository.save(dashboard);
    }

    /**
     * Validate configuration of a Dashboard
     *
     * @param dashboard Entity to validate
     * @throws de.tudresden.inf.verdatas.xapitools.dave.dashboards.DashboardExceptions.InvalidConfiguration when the configuration is not correct
     */
    public void checkDashboardConfiguration(DaveDashboard dashboard) {
        if (!(dashboard.getLrsConnection() == null || dashboard.getVisualisations().isEmpty())) {
            this.finalizeDashboard(dashboard);
        } else {
            if (dashboard.isFinalized()) {
                throw new DashboardExceptions.InvalidConfiguration("Dashboards must have a LRS connection and at least one analysis.");
            }
        }
    }

    /**
     * Check if corresponding DAVE-Connector for the LRS use in the given Dashboard description is initialised
     *
     * @param dashboard Entity to use
     * @return true, if the initialisation is completed
     */
    public Boolean checkConnectorInitialisation(DaveDashboard dashboard) {
        return this.daveConnectorLifecycleManager.getConnector(dashboard.getLrsConnection()).getHealth() != null;
    }

    /**
     * Check if the given Analysis' execution can be limited to a special Activity.
     * Because of how the limitation of an Analysis' execution is done in the method prepareQueryLimit(), the occurrences of the {@link String} "statement/object" are counted.
     * If it occurs more than twice in the Analysis it is assumed that this Analysis can not be limited to an Activity
     *
     * @param analysis {@link DaveVis} to check if the limitation of its execution is possible
     * @return true, if it is assumed that the Analysis' execution can be limited to an Activity
     */
    public Boolean checkLimitationOfAnalysis(DaveVis analysis) {
        String query = analysis.getQuery().getQuery();
        return 2L > Arrays
                .stream(
                        query.substring(1, query.length() - 1).split(" +")
                )
                .filter((s -> s.contains("statement/object")))
                .count();
    }

    /**
     * Schedule to clean the cache for LRS' activities every ten minutes
     */
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.MINUTES)
    @CacheEvict(
            cacheNames = "lrsActivities",
            allEntries = true
    )
    public void cleanCaches() {
        this.logger.info("Cleaned Caches.");
    }

    /**
     * Request all activities of the given LRS and save them in a cache.
     * Has to request the activities' IDs and Types with separate queries because the query's result will be empty, if an activity type is not given for a single activity in the dataset
     *
     * @param connection {@link LrsConnection} to use
     * @return {@link Map} with activity types as keys and IDs of the corresponding activities in a {@link List}
     */
    @Cacheable(
            cacheNames = "lrsActivities",
            key = "#connection.connectionId"
    )
    public Map<String, List<String>> getActivitiesOfLrs(LrsConnection connection) {
        List<String> allActivities = getActivities(connection);
        Map<String, List<String>> activitiesByType = getActivitiesByType(connection);
        List<String> activitiesWithKnownType = activitiesByType.values().stream().flatMap(List::stream).toList();
        List<String> activitiesWithUnknownType = allActivities.stream()
                .filter((activity) -> !(activitiesWithKnownType.contains(activity)))
                .toList();
        if (!activitiesWithUnknownType.isEmpty()) {
            activitiesByType.put("unknown", activitiesWithUnknownType);
        }
        return activitiesByType;
    }

    private List<String> getActivities(LrsConnection connection) {
        DaveVis getActivities = this.prepareGetActivitiesOfLRS(false);
        List<String> activityList = this.daveConnectorLifecycleManager.getConnector(connection)
                .getAnalysisResult(this.fileManagementService.prepareQuery(getActivities, "all").getAbsolutePath(),
                        this.fileManagementService.prepareVisualisation(getActivities).getAbsolutePath());
        return activityList.stream()
                .map((s) -> s.replace("\n", ""))
                .map((s) -> s.substring(1, s.length() - 1))
                .map((s) -> s.replace("\"", ""))
                .map((s) -> s.split(" +")[1])
                .toList();
    }

    private Map<String, List<String>> getActivitiesByType(LrsConnection connection) {
        DaveVis getActivitiesType = this.prepareGetActivitiesOfLRS(true);
        List<String> activitiesWithType = this.daveConnectorLifecycleManager.getConnector(connection)
                .getAnalysisResult(this.fileManagementService.prepareQuery(getActivitiesType, "all").getAbsolutePath(),
                        this.fileManagementService.prepareVisualisation(getActivitiesType).getAbsolutePath());
        return activitiesWithType.stream()
                .map((s) -> s.replace("\n", ""))
                .map((s) -> s.substring(1, s.length() - 1))
                .map((s) -> s.replace("\"", ""))
                .map((s) -> s.split(" +"))
                .map((sarr) -> Pair.of(sarr[1], sarr[2]))
                .collect(
                        Collectors.groupingBy(
                                Pair::getSecond,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        (list) -> list.stream().map(Pair::getFirst).toList()
                                )
                        )
                );
    }

    /**
     * Add an Analysis to an existing Dashboard description
     *
     * @param dashboard  Entity to modify
     * @param activityId ID of the corresponding LRS' activity which should be used to execute the Analysis on. If no limitation was specified this will be indicated by the {@link String} "all"
     * @param analysisId UUID of the Analysis to add
     */
    @Transactional
    public void addVisualisationToDashboard(DaveDashboard dashboard, String activityId, UUID analysisId) {
        List<Pair<String, UUID>> visualisations = dashboard.getVisualisations();
        visualisations.add(Pair.of(activityId, analysisId));
        this.setDashboardVisualisations(dashboard, visualisations);
    }

    /**
     * Change position of an element in the Analyses' List of an existing Dashboard description
     *
     * @param dashboard Entity to modify
     * @param position  of Analysis, which should be moved for one position in the Analyses' List
     * @param move      indicates if the position shift should be forwards ({@link Move}.UP) or backwards ({@link Move}.DOWN)
     */
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

    /**
     * Delete an Analysis from the Analyses' List of an existing Dashboard description
     *
     * @param dashboard Entity to modify
     * @param position  of Analysis, which should be deleted from the Analyses' List
     */
    @Transactional
    public void deleteVisualisationFromDashboard(DaveDashboard dashboard, int position) {
        List<Pair<String, UUID>> visualisations = dashboard.getVisualisations();
        visualisations.remove(position);
        this.setDashboardVisualisations(dashboard, visualisations);
    }

    /**
     * Finalize a Dashboard description.
     * This means that the configuration is completed and the Entity can be executed
     *
     * @param dashboard Entity to finalize
     * @throws de.tudresden.inf.verdatas.xapitools.dave.dashboards.DashboardExceptions.InvalidConfiguration when the configuration is not correct
     */
    @Transactional
    public void finalizeDashboard(DaveDashboard dashboard) {
        if (dashboard.getLrsConnection() == null) {
            throw new DashboardExceptions.InvalidConfiguration("Dashboards must have a LRS Connection.");
        } else if (dashboard.getVisualisations().isEmpty()) {
            throw new DashboardExceptions.InvalidConfiguration("Dashboards must have at least one analysis.");
        }
        dashboard.setFinalized(true);
        this.dashboardRepository.save(dashboard);
    }

    /**
     * Execute an Analysis of a configured Dashboard and return the resulting diagram
     *
     * @param dashboard   Dashboard, which contains the Analysis
     * @param activityURL indicates if the Analysis is executed using the whole LRS data set or only the data belonging to a specific activity
     * @param analysisId  UUID of the Analysis to execute
     * @throws de.tudresden.inf.verdatas.xapitools.dave.dashboards.DashboardExceptions.InvalidConfiguration when the provided Dashboard does not contain the specified Analysis
     */
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
                .orElseThrow(() -> new DashboardExceptions.InvalidConfiguration("Could not find dashboard analyses for execution."));
    }

    /**
     * Get the name corresponding to the provided Analysis-UUID
     *
     * @param analysisId UUID of the Analysis to use
     */
    public String getNameOfAnalysis(UUID analysisId) {
        return this.getAnalysisById(analysisId).getName();
    }

    /**
     * Helper function to create Analysis for requesting the Activities of an LRS.
     * Must not be deleted or modified and is therefore not seeded like the predefined analyses
     *
     * @param withType indicates if Activities' type should also be requested
     */
    public DaveVis prepareGetActivitiesOfLRS(Boolean withType) {
        DaveVis analysis = new DaveVis("Activities of LRS",
                new DaveQuery("Activities of LRS", ""),
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
        if (withType) {
            analysis.setQuery(new DaveQuery("Activities of LRS", """
                    [:find (count ?s) ?c ?t :where [?s :statement/object ?o][?o :activity/id ?c][?o :activity.definition/type ?t]]"""));
        } else {
            analysis.setQuery(new DaveQuery("Activities of LRS", """
                    [:find (count ?s) ?c :where [?s :statement/object ?o][?o :activity/id ?c]]"""));
        }
        return analysis;
    }

    /**
     * UI helper enum, controls movement of analysis
     */
    public enum Move {
        UP,
        DOWN
    }
}
