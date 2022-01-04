package de.tudresden.inf.rn.xapi.datatools.lrs;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LrsService {

    private final LrsConnectionRepository lrsConnectionRepository;

    @Transactional
    LrsConnection createConnection(LrsConnectionTO lrsData) {
        LrsConnection created = lrsData.toNewLrsConnection();
        this.lrsConnectionRepository.save(created);
        return created;
    }

    @Transactional
    LrsConnection deactivateConnection(UUID connectionId) {
        LrsConnection connection = this.getConnection(connectionId);
        connection.setEnabled(false);
        return connection;
    }

    List<LrsConnection> getConnections(boolean activeOnly) {
        if (activeOnly) {
            return this.lrsConnectionRepository.findByEnabledIsTrue().toList();
        } else {
            return this.lrsConnectionRepository.findAll().toList();
        }
    }

    LrsConnection getConnection(UUID connectionId) {
        return this.lrsConnectionRepository.findById(connectionId).orElseThrow(IllegalArgumentException::new);
    }

    @Transactional
    LrsConnection updateConnection(LrsConnectionTO lrsData) {
        LrsConnection saved = this.getConnection(lrsData.getUuid().orElseThrow(IllegalArgumentException::new));
        saved.setFriendlyName(lrsData.getName());
        saved.setXApiEndpoint(lrsData.getEndpoint());
        saved.setXApiClientKey(lrsData.getClientKey());
        saved.setXApiClientSecret(lrsData.getClientSecret());
        saved.setEnabled(lrsData.getEnabled().orElse(saved.isEnabled()));
        return saved;
    }
}
