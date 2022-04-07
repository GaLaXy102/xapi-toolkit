package de.tudresden.inf.verdatas.xapitools.dave.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import de.tudresden.inf.verdatas.xapitools.dave.FileManagementService;
import de.tudresden.inf.verdatas.xapitools.dave.analysis.controllers.AnalysisMavController;
import de.tudresden.inf.verdatas.xapitools.dave.connector.DaveConnectorLifecycleManager;
import de.tudresden.inf.verdatas.xapitools.dave.persistence.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This service handles all Entity-related concerns to configure a DAVE Analysis
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveAnalysisService {
    private final DaveVisRepository visRepository;
    private final DaveQueryRepository queryRepository;
    private final DaveGraphDescriptionRepository graphDescriptionRepository;
    private final DaveDashboardRepository dashboardRepository;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;
    private final FileManagementService fileManagementService;

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
     * Get all Query descriptions saved in the System
     */
    public Stream<DaveQuery> getAllQueries() {
        return this.queryRepository.findAll().stream();
    }

    /**
     * Get all Graph descriptions saved in the System
     */
    public Stream<DaveGraphDescription> getAllGraphDescriptions() {
        return this.graphDescriptionRepository.findAll().stream();
    }

    /**
     * Get an Analysis description by its ID
     *
     * @param analysisId UUID of the Analysis to find
     * @throws NoSuchElementException when the ID is not known to the system
     */
    public DaveVis getAnalysis(UUID analysisId) {
        return this.visRepository.findById(analysisId).orElseThrow(() -> new NoSuchElementException("No such analysis."));
    }

    /**
     * Create an Analysis description skeleton.
     * The created entity has nothing attached to it and its parameters are chosen to have somewhat reasonable values.
     */
    @Transactional
    public DaveVis createEmptyAnalysis() {
        DaveVis emptyAnalysis = new DaveVis(null, null, null, false);
        this.visRepository.save(emptyAnalysis);
        return emptyAnalysis;
    }

    /**
     * Create and persist a new Analysis description
     *
     * @param name             Title of the new Analysis. Must not be already used
     * @param queryName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} should be created this title must not be already used
     * @param query            Query Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. Duplication inhibited
     * @param graphName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} should be created this title must not be used already
     * @param graphDescription Graph Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. Duplication inhibited
     * @throws de.tudresden.inf.verdatas.xapitools.dave.analysis.AnalysisExceptions.ConfigurationConflict when the {@param name} is already used
     */
    @Transactional
    public DaveVis createAnalysis(String name, String query, String queryName, String graphDescription, String graphName) {
        this.checkValidityOfInput(query, queryName, graphDescription, graphName);
        if (this.visRepository.findByName(name).isPresent()) {
            throw new AnalysisExceptions.ConfigurationConflict("The chosen name is already used. Please rename your analysis.");
        }
        DaveVis created = new DaveVis(
                name,
                this.updateQuery(queryName, query),
                this.updateGraphDescription(graphName, graphDescription),
                true
        );
        return this.visRepository.save(created);
    }

    /**
     * Create and persist a copy of an existing Analysis description
     *
     * @param analysis Entity to copy
     */
    @Transactional
    public DaveVis createCopyOfAnalysis(DaveVis analysis) {
        DaveVis created = this.createEmptyAnalysis();
        this.setAnalysisName(created, "Copy of " + analysis.getName());
        this.setAnalysisQuery(created, analysis.getQuery());
        this.setAnalysisGraphDescription(created, analysis.getDescription());
        return created;
    }

    /**
     * Modify and persist an Analysis description
     *
     * @param analysis         Entity to edit
     * @param name             Title of the new Analysis. Must not be already used
     * @param queryName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} should be created this title must not be already used
     * @param query            Query Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. Duplication inhibited
     * @param graphName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} should be created this title must not be used already
     * @param graphDescription Graph Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. Duplication inhibited
     * @throws de.tudresden.inf.verdatas.xapitools.dave.analysis.AnalysisExceptions.ConfigurationConflict when the {@param name} is already used by another Entity or a duplication of the analysis's parts occured
     */
    @Transactional
    public void updateAnalysis(DaveVis analysis, String name, String query, String queryName,
                               String graphDescription, String graphName) {
        Optional<DaveVis> copy = this.visRepository.findByName(name);
        if (copy.isPresent() && !(copy.get().getId().equals(analysis.getId()))) {
            throw new AnalysisExceptions.ConfigurationConflict("Conflicting analysis objects. Please rename your analysis.");
        } else {
            this.setAnalysisName(analysis, name);
        }
        Set<DaveVis> queryConflict = this.checkForQueryDuplicates(queryName, query).stream()
                .filter(con -> !(con.getId().equals(analysis.getId())))
                .collect(Collectors.toSet());
        Set<DaveVis> graphDescriptionConflict = this.checkForGraphDescriptionDuplicates(graphName, graphDescription)
                .stream()
                .filter(con -> !(con.getId().equals(analysis.getId())))
                .collect(Collectors.toSet());
        if (!(queryConflict.isEmpty())) {
            throw new AnalysisExceptions.ConfigurationConflict("Duplication of query objects.");
        }
        if (!(graphDescriptionConflict.isEmpty())) {
            throw new AnalysisExceptions.ConfigurationConflict("Duplication of graph description objects.");
        }
        this.setAnalysisQuery(analysis, this.updateQuery(queryName, query));
        this.setAnalysisGraphDescription(analysis, this.updateGraphDescription(graphName, graphDescription));
    }

    /**
     * Modify and persist a Query description
     *
     * @param queryName Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} should be created this title must not be already used
     * @param query     Query Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. Duplication inhibited
     */
    @Transactional
    public DaveQuery updateQuery(String queryName, String query) {
        DaveQuery updatedOrCreated = this.queryRepository.findByName(queryName).map(found -> {
            found.setQuery(query);
            return found;
        }).orElseGet(() -> new DaveQuery(queryName, query));
        return this.queryRepository.save(updatedOrCreated);
    }

    /**
     * Modify and persist a Graph description
     *
     * @param graphName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} should be created this title must not be used already
     * @param graphDescription Graph Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. Duplication inhibited
     */
    @Transactional
    public DaveGraphDescription updateGraphDescription(String graphName, String graphDescription) {
        DaveGraphDescription updatedOrCreated = this.graphDescriptionRepository.findByName(graphName).map(found -> {
            found.setDescription(graphDescription);
            return found;
        }).orElseGet(() -> new DaveGraphDescription(graphName, graphDescription));
        return this.graphDescriptionRepository.save(updatedOrCreated);
    }

    /**
     * Set and persist the title of the Analysis description
     *
     * @param analysis Entity to modify
     * @param name     modified Title of the Analysis
     */
    @Transactional
    public void setAnalysisName(DaveVis analysis, String name) {
        analysis.setName(name);
        this.visRepository.save(analysis);
    }

    /**
     * Set and persist the Query of the Analysis description
     *
     * @param analysis Entity to modify
     * @param query    {@link DaveQuery} to use
     */
    @Transactional
    public void setAnalysisQuery(DaveVis analysis, DaveQuery query) {
        analysis.setQuery(query);
        this.visRepository.save(analysis);
    }

    /**
     * Set and persist the Graph Description of the Analysis
     *
     * @param analysis         Entity to modify
     * @param graphDescription {@link DaveGraphDescription} to use
     */
    @Transactional
    public void setAnalysisGraphDescription(DaveVis analysis, DaveGraphDescription graphDescription) {
        analysis.setDescription(graphDescription);
        this.visRepository.save(analysis);
    }

    /**
     * Validate configuration of an Analysis
     *
     * @param analysis Entity to validate
     * @throws de.tudresden.inf.verdatas.xapitools.dave.analysis.AnalysisExceptions.InvalidConfiguration when the configuration is not correct
     */
    public void checkAnalysisConfiguration(DaveVis analysis) {
        if (!(analysis.getQuery() == null || analysis.getDescription() == null)) {
            this.finalizeAnalysis(analysis);
        } else if (analysis.isFinalized()) {
            throw new AnalysisExceptions.InvalidConfiguration("Analysis must have a query and a graph description.");
        }
    }

    /**
     * Finalize an Analysis description.
     * This means that the configuration is completed and the Entity can be used in a {@link DaveDashboard}
     *
     * @param analysis Entity to finalize
     * @throws de.tudresden.inf.verdatas.xapitools.dave.analysis.AnalysisExceptions.InvalidConfiguration when the configuration is not correct
     */
    @Transactional
    public void finalizeAnalysis(DaveVis analysis) {
        if (analysis.getQuery() == null) {
            throw new AnalysisExceptions.InvalidConfiguration("Analysis must have a query.");
        } else if (analysis.getDescription() == null) {
            throw new AnalysisExceptions.InvalidConfiguration("Analysis must have a graph description.");
        }
        analysis.setFinalized(true);
        this.visRepository.save(analysis);
    }

    /**
     * Delete an Analysis description.
     * This can only be done, if it's not in use in a {@link DaveDashboard}.
     * The corresponding {@link DaveQuery} and {@link DaveGraphDescription} will also be deleted, if they are not used by another Analysis
     *
     * @param analysis Entity to delete
     * @throws de.tudresden.inf.verdatas.xapitools.dave.analysis.AnalysisExceptions.SideEffectsError when the Analysis is still used in a {@link DaveDashboard}
     */
    @Transactional
    public void deleteAnalysis(DaveVis analysis) {
        Set<String> used = this.checkUsageOfAnalysis(analysis);
        if (used.isEmpty()) {
            if (this.getAnalysesWithSameQuery(analysis, analysis.getQuery()).isEmpty()) {
                this.queryRepository.delete(analysis.getQuery());
            }
            if (this.getAnalysesWithSameGraphDescription(analysis, analysis.getDescription()).isEmpty()) {
                this.graphDescriptionRepository.delete(analysis.getDescription());
            }
            this.visRepository.delete(analysis);
        } else {
            throw new AnalysisExceptions.SideEffectsError("Deletion of "
                    + analysis.getName() + " not possible.\n Still in use for " + used);
        }
    }

    /**
     * Get the Titles of all {@link DaveDashboard}s, which use the Analysis description
     *
     * @param analysis Entity, which usage should be checked
     */
    public Set<String> checkUsageOfAnalysis(DaveVis analysis) {
        Set<String> useAnalysis = new HashSet<>();
        List<DaveDashboard> dashboards = this.dashboardRepository.findAll();
        for (DaveDashboard d : dashboards) {
            for (Pair<String, UUID> visualisations : d.getVisualisations()) {
                if (visualisations.getSecond().equals(analysis.getId())) {
                    useAnalysis.add(d.getName());
                }
            }
        }
        return useAnalysis;
    }

    /**
     * Get all Analyses, which use the same Query
     *
     * @param analysis Entity as reference value
     * @param query    {@link DaveQuery} which usage should be checked
     */
    public Set<DaveVis> getAnalysesWithSameQuery(DaveVis analysis, DaveQuery query) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> daveVis.getQuery().getQuery().equals(query.getQuery()))
                .filter(vis -> !(vis.getId().equals(analysis.getId())))
                .collect(Collectors.toSet());
    }

    /**
     * Get all Analyses, which use the same Graph Description
     *
     * @param analysis         Entity as reference value
     * @param graphDescription {@link DaveGraphDescription} which usage should be checked
     */
    public Set<DaveVis> getAnalysesWithSameGraphDescription(DaveVis analysis, DaveGraphDescription graphDescription) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> daveVis.getDescription().getDescription().equals(graphDescription.getDescription()))
                .filter(vis -> !(vis.getId().equals(analysis.getId())))
                .collect(Collectors.toSet());
    }

    /**
     * Get the Titles of all Analyses, which use the same Query.
     * This is used to check if the modification of this description would lead to errors
     *
     * @param analysis  Entity as reference value
     * @param queryName Title of the {@link DaveQuery}, which usage should be checked
     * @param query     Description of the {@link DaveQuery} to check
     */
    public Set<String> checkUsageOfQuery(DaveVis analysis, String queryName, String query) {
        return this.checkForQueryConflicts(queryName, query)
                .stream()
                .filter(daveVis -> !(daveVis.getId().equals(analysis.getId())))
                .map(DaveVis::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Get the Titles of all Analyses, which use the same Graph Description.
     * This is used to check if the modification of this description would lead to errors
     *
     * @param analysis         Entity as reference value
     * @param graphName        Title of the {@link DaveGraphDescription}, which usage should be checked
     * @param graphDescription Description of the {@link DaveGraphDescription} to check
     */
    public Set<String> checkUsageOfGraphDescription(DaveVis analysis, String graphName, String graphDescription) {
        return this.checkForGraphDescriptionConflicts(graphName, graphDescription)
                .stream()
                .filter(daveVis -> !(daveVis.getId().equals(analysis.getId())))
                .map(DaveVis::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Check if the Title of a Query description is already used
     *
     * @param queryName Title of the Query, which usage should be checked
     * @param query     Description of the Query. Needed to ensure that a different {@link DaveQuery} is used
     * @return {@link Set} of Analyses with {@param queryName} duplicates
     */
    public Set<DaveVis> checkForQueryConflicts(String queryName, String query) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> daveVis.getQuery().getName().equals(queryName))
                .filter(daveVis -> !(daveVis.getQuery().getQuery().equals(query)))
                .collect(Collectors.toSet());
    }

    /**
     * Check if a duplication of a Query description exists with a different Title
     *
     * @param queryName Title of the Query. Needed to ensure that a different {@link DaveQuery} is used
     * @param query     Description of the Query, which usage should be checked
     * @return {@link Set} of Analyses with {@param query} duplicates
     */
    public Set<DaveVis> checkForQueryDuplicates(String queryName, String query) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> (daveVis.getQuery().getQuery().equals(query)))
                .filter(daveVis -> !(daveVis.getQuery().getName().equals(queryName)))
                .collect(Collectors.toSet());
    }

    /**
     * Check if the Title of a Graph Description is already used
     *
     * @param graphName        Title of the Graph Description, which usage should be checked
     * @param graphDescription Description of the Graph Description. Needed to ensure that a different {@link DaveGraphDescription} is used
     * @return {@link Set} of Analyses with {@param graphName} duplicates
     */
    public Set<DaveVis> checkForGraphDescriptionConflicts(String graphName, String graphDescription) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> daveVis.getDescription().getName().equals(graphName))
                .filter(daveVis -> !(daveVis.getDescription().getDescription().equals(graphDescription)))
                .collect(Collectors.toSet());
    }

    /**
     * Check if a duplication of a Graph Description exists with a different Title
     *
     * @param graphName        Title of the Graph Description. Needed to ensure that a different {@link DaveGraphDescription} is used
     * @param graphDescription Description of the Graph Description, which usage should be checked
     * @return {@link Set} of Analyses with {@param graphDescription} duplicates
     */
    public Set<DaveVis> checkForGraphDescriptionDuplicates(String graphName, String graphDescription) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> (daveVis.getDescription().getDescription().equals(graphDescription)))
                .filter(daveVis -> !(daveVis.getDescription().getName().equals(graphName)))
                .collect(Collectors.toSet());
    }

    /**
     * Check if creation or modification of an Analysis will provide side effects and provide error messages if needed
     *
     * @param mode             used to distinguish between creation and modification of Analyses
     * @param analysis         Analysis to be modified. Only available if mode equals {@link AnalysisMavController.Mode}.EDITING
     * @param name             Title of the Analysis. Must not be already used
     * @param queryName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} should be created this title must not be already used
     * @param query            Query Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveQuery} to use or create. Duplication inhibited
     * @param graphName        Title of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. If a new {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} should be created this title must not be used already
     * @param graphDescription Graph Description of the {@link de.tudresden.inf.verdatas.xapitools.dave.persistence.DaveGraphDescription} to use or create. Duplication inhibited
     */
    public Optional<Map<String, Object>> checkForSideEffects(AnalysisMavController.Mode mode, String name, Optional<DaveVis> analysis, String queryName, String query,
                                                             String graphName, String graphDescription) {
        Set<String> analysisWithQueryConflict = Set.of();
        Set<String> analysisWithGraphConflict = Set.of();
        Set<String> dashboardNames = Set.of();
        Map<String, Object> objectMap = new HashMap<>();
        List<String> messages = new LinkedList<>();
        List<String> hints = new LinkedList<>();

        if (mode.equals(AnalysisMavController.Mode.CREATING)) {
            analysisWithQueryConflict = this.checkForQueryConflicts(queryName, query)
                    .stream()
                    .map(DaveVis::getName)
                    .collect(Collectors.toSet());
            analysisWithGraphConflict = this.checkForGraphDescriptionConflicts(graphName, graphDescription)
                    .stream()
                    .map(DaveVis::getName)
                    .collect(Collectors.toSet());
        } else if (analysis.isPresent()) {
            dashboardNames = this.checkUsageOfAnalysis(analysis.get());
            analysisWithQueryConflict = this.checkUsageOfQuery(analysis.get(), queryName, query);
            analysisWithGraphConflict = this.checkUsageOfGraphDescription(analysis.get(), graphName, graphDescription);
        }

        // Needed if checking for side effects provides error and user has to acknowledge changes
        objectMap.put("mode", mode);
        objectMap.put("name", name);
        objectMap.put("queryContent", query);
        objectMap.put("queryName", queryName);
        objectMap.put("graphContent", graphDescription);
        objectMap.put("graphName", graphName);

        if (mode.equals(AnalysisMavController.Mode.EDITING) && analysis.isPresent()) {
            objectMap.put("flow", analysis.get().getId());
            if (!(dashboardNames.isEmpty())) {
                messages.add("Modification of " + analysis.get().getName()
                        + " not possible.\n Still in use for dashboard(s) " + dashboardNames);
                hints.add("Please make a copy the analysis first and modify it afterwards " +
                        "if you do not want the dashboard(s) to be changed.\n Otherwise, continue the modification.");
            }
        }

        if (!(analysisWithQueryConflict.isEmpty())) {
            messages.add("Modification of query not possible.\n Still in use for analysis "
                    + analysisWithQueryConflict);
            hints.add("Please use a different name for your query " +
                    "if you do not want the other analysis to be changed.\n Otherwise, continue the modification.");
        }
        if (!(analysisWithGraphConflict.isEmpty())) {
            messages.add("Modification of graph description not possible.\n Still in use for analysis "
                    + analysisWithGraphConflict);
            hints.add("Please use a different name for your graphDescription " +
                    "if you do not want the other analysis to be changed.\n Otherwise, continue the modification.");
        }

        if (!(messages.isEmpty())) {
            objectMap.put("messages", messages);
            objectMap.put("hints", hints);
            return Optional.of(objectMap);
        }
        return Optional.empty();
    }

    /**
     * Check if there aren't duplicates for the given Query and Graph Description in the system
     *
     * @param query            Description of the Query
     * @param queryName        Title of the Query
     * @param graphDescription Description of the Graph Description
     * @param graphName        Title of the Graph Description
     * @throws de.tudresden.inf.verdatas.xapitools.dave.analysis.AnalysisExceptions.ConfigurationConflict when there are duplicated Entities
     */
    public void checkValidityOfInput(String query, String queryName, String graphDescription, String graphName) {
        if (!(this.checkForQueryDuplicates(queryName, query).isEmpty())) {
            throw new AnalysisExceptions.ConfigurationConflict("Duplication of query objects.");
        }
        if (!(this.checkForGraphDescriptionDuplicates(graphName, graphDescription).isEmpty())) {
            throw new AnalysisExceptions.ConfigurationConflict("Duplication of graph description objects.");
        }
    }

    /**
     * Check if the given Query and Graph descriptions match the scheme for DAVE Analyses
     *
     * @param query            Query description to validate
     * @param graphDescription Description of the Graph Description to validate
     * @throws IllegalStateException when the validation fails
     */
    public void checkValidityOfAnalysisDescription(String query, String graphDescription) {
        Pair<String, String> analysisDescriptionPaths = this.fileManagementService
                .prepareValidityCheck(query, graphDescription);
        Optional<String> error = this.daveConnectorLifecycleManager.getTestConnector()
                .testAnalysisExecution(analysisDescriptionPaths.getFirst(), analysisDescriptionPaths.getSecond());
        if (error.isPresent()) {
            throw new IllegalStateException(error.get());
        }
    }

    /**
     * Import a list of Analyses descriptions.
     * Duplication of an Analysis description or its part is inhibited.
     *
     * @param analysisData List of uploaded Analysis descriptions
     */
    public void retrieveAnalysisDescriptions(List<JsonNode> analysisData) {
        for (JsonNode analysisDescription : analysisData) {
            String analysisName = analysisDescription.get("name").asText();
            Pair<String, String> query = Pair.of(analysisDescription.get("query").get("name").asText(),
                    analysisDescription.get("query").get("query").asText().replace("\r", ""));
            Pair<String, String> graphDescription = Pair.of(analysisDescription.get("description").get("name").asText(),
                    analysisDescription.get("description").get("description").asText().replace("\r", ""));
            this.checkValidityOfAnalysisDescription(query.getSecond(), graphDescription.getSecond());
            this.createAnalysis(analysisName, query.getSecond(), query.getFirst(), graphDescription.getSecond(), graphDescription.getFirst());
        }
    }
}
