package de.tudresden.inf.verdatas.xapitools.dave;

import de.tudresden.inf.verdatas.xapitools.dave.persistence.*;
import de.tudresden.inf.verdatas.xapitools.lrs.LrsConnection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DaveAnalysisService {
    private final DaveDashboardRepository dashboardRepository;
    private final DaveVisRepository visRepository;
    private final DaveQueryRepository queryRepository;
    private final DaveGraphDescriptionRepository graphDescriptionRepository;

    public Stream<DaveDashboard> getAllDashboards() {
        return this.dashboardRepository.findAll().stream();
    }

    public DaveDashboard getDashboard(UUID dashboardId) {
        return this.dashboardRepository.findById(dashboardId).orElseThrow(() -> new NoSuchElementException("No such dashboard."));
    }

    @Transactional
    public DaveDashboard createEmptyDashboard() {
        DaveDashboard emptyDashboard = new DaveDashboard(null, new LinkedList<>());
        dashboardRepository.save(emptyDashboard);
        return emptyDashboard;
    }

    @Transactional
    public void setDashboardIdentifier(DaveDashboard dashboard, String identifier) {
        dashboard.setIdentifier(identifier);
        dashboardRepository.save(dashboard);
    }

    @Transactional
    public void setDashboardSource(DaveDashboard dashboard, LrsConnection lrsConnection) {
        dashboard.setLrsConnection(lrsConnection);
        dashboardRepository.save(dashboard);
    }
}
