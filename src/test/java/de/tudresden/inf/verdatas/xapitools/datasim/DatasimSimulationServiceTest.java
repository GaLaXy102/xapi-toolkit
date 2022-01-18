package de.tudresden.inf.verdatas.xapitools.datasim;

import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DatasimSimulationServiceTest {

    @Autowired
    DatasimSimulationService service;

    private DatasimSimulation sample;

    @BeforeEach
    @Transactional
    void setUp() {
        Assertions.assertThat(this.service.getAllSimulations()).hasSize(0);
        this.sample = this.service.createEmptySimulation();
    }

    @Test
    void finalizeSimulation() {
        assertThat(this.sample.isFinalized()).isFalse();
        this.service.finalizeSimulation(this.sample);
        Assertions.assertThat(this.service.getSimulation(this.sample.getId()).isFinalized()).isTrue();
        assertThrows(ConstraintViolationException.class, () -> this.service.finalizeSimulation(this.sample));
    }
}