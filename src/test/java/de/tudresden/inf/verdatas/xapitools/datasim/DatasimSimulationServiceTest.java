package de.tudresden.inf.verdatas.xapitools.datasim;

import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimPersona;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimPersonaTO;
import de.tudresden.inf.verdatas.xapitools.datasim.persistence.DatasimSimulation;
import de.tudresden.inf.verdatas.xapitools.util.TestContainerTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class DatasimSimulationServiceTest extends TestContainerTest {

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
    @Transactional
    void finalizeSimulation() {
        assertThat(this.sample.isFinalized()).isFalse();
        DatasimPersona persona = this.service.createPersona(DatasimPersonaTO.with("test", "test@example.org").toNewDatasimPersona());
        this.service.addPersonaToSimulation(this.sample, persona);
        this.service.finalizeSimulation(this.service.getSimulation(this.sample.getId()));
        Assertions.assertThat(this.service.getSimulation(this.sample.getId()).isFinalized()).isTrue();
        assertThrows(ConstraintViolationException.class, () -> this.service.finalizeSimulation(this.sample));
    }
}