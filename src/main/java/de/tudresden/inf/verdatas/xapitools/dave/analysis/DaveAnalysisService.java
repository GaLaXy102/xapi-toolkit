package de.tudresden.inf.verdatas.xapitools.dave.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import de.tudresden.inf.verdatas.xapitools.dave.FileManagementService;
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

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveAnalysisService {
    private final DaveVisRepository visRepository;
    private final DaveQueryRepository queryRepository;
    private final DaveGraphDescriptionRepository graphDescriptionRepository;
    private final DaveDashboardRepository dashboardRepository;
    private final DaveConnectorLifecycleManager daveConnectorLifecycleManager;
    private final FileManagementService fileManagementService;

    public Stream<DaveVis> getAllAnalysis(boolean finalizedOnly) {
        if (finalizedOnly) {
            return this.visRepository.findAllByFinalizedIsTrue().stream();
        } else {
            return this.visRepository.findAll().stream();
        }
    }

    public Stream<DaveQuery> getAllQueries() {
        return this.queryRepository.findAll().stream();
    }

    public Stream<DaveGraphDescription> getAllGraphDescriptions() {
        return this.graphDescriptionRepository.findAll().stream();
    }

    public DaveVis getAnalysis(UUID analysisId) {
        return this.visRepository.findById(analysisId).orElseThrow(() -> new NoSuchElementException("No such analysis."));
    }

    @Transactional
    public DaveVis createEmptyAnalysis() {
        DaveVis emptyAnalysis = new DaveVis(null, null, null, false);
        this.visRepository.save(emptyAnalysis);
        return emptyAnalysis;
    }

    @Transactional
    public DaveVis createAnalysis(String name, String query, String queryName, String graphDescription, String graphName) {
        this.checkValidityOfInput(query, queryName, graphDescription, graphName);
        if (this.visRepository.findByName(name).isPresent()) {
            throw new IllegalStateException("Conflicting analysis objects. Please rename your analysis.");
        }
        DaveVis created = new DaveVis(
                name,
                this.updateQuery(queryName, query),
                this.updateGraphDescription(graphName, graphDescription),
                true
        );
        return this.visRepository.save(created);
    }

    @Transactional
    public DaveVis createCopyOfAnalysis(DaveVis analysis) {
        DaveVis created = this.createEmptyAnalysis();
        this.setAnalysisName(created, "Copy of " + analysis.getName());
        this.setAnalysisQuery(created, analysis.getQuery());
        this.setAnalysisGraphDescription(created, analysis.getDescription());
        return created;
    }

    @Transactional
    public void updateAnalysis(DaveVis analysis, String name, String query, String queryName,
                               String graphDescription, String graphName) {
        Optional<DaveVis> copy = this.visRepository.findByName(name);
        if (copy.isPresent() && !(copy.get().getId().equals(analysis.getId()))) {
            throw new IllegalStateException("Conflicting analysis objects. Please rename your analysis.");
        } else {
            this.setAnalysisName(analysis, name);
        }
        this.checkForQueryDuplicates(queryName, query);
        this.checkForGraphDescriptionDuplicates(graphName, graphDescription);
        this.setAnalysisQuery(analysis, this.updateQuery(queryName, query));
        this.setAnalysisGraphDescription(analysis, this.updateGraphDescription(graphName, graphDescription));
    }

    @Transactional
    public DaveQuery updateQuery(String queryName, String query) {
        DaveQuery updatedOrCreated = this.queryRepository.findByName(queryName).map(found -> {
            found.setQuery(query);
            return found;
        }).orElseGet(() -> new DaveQuery(queryName, query));
        return this.queryRepository.save(updatedOrCreated);
    }

    @Transactional
    public DaveGraphDescription updateGraphDescription(String graphName, String graphDescription) {
        DaveGraphDescription updatedOrCreated = this.graphDescriptionRepository.findByName(graphName).map(found -> {
            found.setDescription(graphDescription);
            return found;
        }).orElseGet(() -> new DaveGraphDescription(graphName, graphDescription));
        return this.graphDescriptionRepository.save(updatedOrCreated);
    }

    @Transactional
    public void setAnalysisName(DaveVis analysis, String name) {
        analysis.setName(name);
        this.visRepository.save(analysis);
    }

    @Transactional
    public void setAnalysisQuery(DaveVis analysis, DaveQuery query) {
        analysis.setQuery(query);
        this.visRepository.save(analysis);
    }

    @Transactional
    public void setAnalysisGraphDescription(DaveVis analysis, DaveGraphDescription graphDescription) {
        analysis.setDescription(graphDescription);
        this.visRepository.save(analysis);
    }

    public void checkAnalysisConfiguration(DaveVis analysis) {
        if (!(analysis.getQuery() == null || analysis.getDescription() == null)) {
            this.finalizeAnalysis(analysis);
        } else {
            if (analysis.isFinalized()) {
                throw new IllegalStateException("Analysis must have a query and a graph description.");
            }
        }
    }

    @Transactional
    public void finalizeAnalysis(DaveVis analysis) {
        if (analysis.getQuery() == null) {
            throw new IllegalStateException("Analysis must have a query.");
        } else if (analysis.getDescription() == null) {
            throw new IllegalStateException("Analysis must have a graph description.");
        }
        analysis.setFinalized(true);
        this.visRepository.save(analysis);
    }

    @Transactional
    public void deleteAnalysis(DaveVis analysis) {
        Set<String> used = this.checkUsageOfAnalysis(analysis);
        if (used.isEmpty()) {
            this.visRepository.delete(analysis);
        } else {
            throw new IllegalStateException("Deletion of " + analysis.getName() + " not possible.\n Still in use for " + used);
        }
    }

    public Set<String> checkUsageOfAnalysis(DaveVis analysis) {
        Set<String> useAnalysis = new HashSet<>();
        List<DaveDashboard> dashboards = this.dashboardRepository.findAll();
        for (DaveDashboard d :
                dashboards) {
            for (Pair<String, UUID> visualisations :
                    d.getVisualisations()) {
                if (visualisations.getSecond().equals(analysis.getId())) {
                    useAnalysis.add(d.getName());
                }
            }
        }
        return useAnalysis;
    }

    public Set<String> checkUsageOfQuery(DaveVis analysis, String queryName, String query) {
        return this.checkForQueryConflicts(queryName, query)
                .stream()
                .filter(daveVis -> !(daveVis.getId().equals(analysis.getId())))
                .map(DaveVis::getName)
                .collect(Collectors.toSet());
    }

    public Set<String> checkUsageOfGraphDescription(DaveVis analysis, String graphName, String graphDescription) {
        return this.checkForGraphDescriptionConflicts(graphName, graphDescription)
                .stream()
                .filter(daveVis -> !(daveVis.getId().equals(analysis.getId())))
                .map(DaveVis::getName)
                .collect(Collectors.toSet());
    }

    public Set<DaveVis> checkForQueryConflicts(String queryName, String query) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> daveVis.getQuery().getName().equals(queryName))
                .filter(daveVis -> !(daveVis.getQuery().getQuery().equals(query)))
                .collect(Collectors.toSet());
    }

    public Set<DaveVis> checkForQueryDuplicates(String queryName, String query) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> (daveVis.getQuery().getQuery().equals(query)))
                .filter(daveVis -> !(daveVis.getQuery().getName().equals(queryName)))
                .collect(Collectors.toSet());
    }

    public Set<DaveVis> checkForGraphDescriptionConflicts(String graphName, String graphDescription) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> daveVis.getDescription().getName().equals(graphName))
                .filter(daveVis -> !(daveVis.getDescription().getDescription().equals(graphDescription)))
                .collect(Collectors.toSet());
    }

    public Set<DaveVis> checkForGraphDescriptionDuplicates(String graphName, String graphDescription) {
        return this.getAllAnalysis(false)
                .filter(daveVis -> (daveVis.getDescription().getDescription().equals(graphDescription)))
                .filter(daveVis -> !(daveVis.getDescription().getName().equals(graphName)))
                .collect(Collectors.toSet());
    }

    public void checkValidityOfInput(String query, String queryName, String graphDescription, String graphName) {
        if (!(this.checkForQueryDuplicates(queryName, query).isEmpty())) {
            throw new IllegalStateException("Duplication of query objects.");
        }
        if (!(this.checkForGraphDescriptionDuplicates(graphName, graphDescription).isEmpty())) {
            throw new IllegalStateException("Duplication of graph description objects.");
        }
    }

    public void checkValidityOfAnalysisDescription(String query, String graphDescription) {
        Pair<String, String> analysisDescriptionPaths = this.fileManagementService
                .prepareValidityCheck(query, graphDescription);
        Optional<String> error = this.daveConnectorLifecycleManager.getTestConnector()
                .testAnalysisExecution(analysisDescriptionPaths.getFirst(), analysisDescriptionPaths.getSecond());
        if (error.isPresent()) {
            throw new IllegalStateException(error.get());
        }
    }

    public void retrieveAnalysisDescriptions(List<JsonNode> analysisData) {
        for (JsonNode analysisDescription:
             analysisData) {
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
