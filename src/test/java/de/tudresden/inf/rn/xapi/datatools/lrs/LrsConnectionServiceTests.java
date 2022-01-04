package de.tudresden.inf.rn.xapi.datatools.lrs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validation;
import javax.validation.Validator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class LrsConnectionServiceTests {

    @Autowired
    LrsService lrsService;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private LrsConnectionTO sample;

    @BeforeEach @Transactional
    void setUp() throws MalformedURLException {
        assertThat(this.lrsService.getConnections(false)).hasSize(0);
        this.sample = new LrsConnectionTO(Optional.empty(), "TestConnection", new URL("https://sut:1234"), "key", "secret", Optional.empty());
    }

    @Test @Transactional
    void testInsertWorks() throws MalformedURLException {
        LrsConnectionTO input = new LrsConnectionTO(Optional.empty(), "TestConnection", new URL("https://sut:1234"), "key", "secret", Optional.empty());
        LrsConnection created = this.lrsService.createConnection(input);
        LrsConnection saved = this.lrsService.getConnection(created.getConnectionId()); // Catches not saved
        assertThat(created).isEqualTo(saved);             // Checks that the returned object matches the saved one
    }

    @Test @Transactional
    void testParametersChecked() throws MalformedURLException {
        LrsConnectionTO input = new LrsConnectionTO(Optional.empty(), "", new URL("https://sut:1234"), "", "", Optional.empty());
        assertThat(this.validator.validate(input)).hasSize(3);
        assertThatThrownBy(() -> new LrsConnectionTO(Optional.empty(), null, new URL("https://sut:1234"), "", "", Optional.empty())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LrsConnectionTO(Optional.empty(), "", new URL("https://sut:1234"), null, "", Optional.empty())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LrsConnectionTO(Optional.empty(), "", new URL("https://sut:1234"), "", null, Optional.empty())).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new LrsConnectionTO(Optional.empty(), "", new URL("https://sut:1234"), "", "", null)).isInstanceOf(NullPointerException.class);
        input = new LrsConnectionTO(Optional.empty(), "TestConnection", new URL("https://sut:1234"), "key", "secret", Optional.empty());
        assertThat(this.validator.validate(input)).hasSize(0);
    }

    @Test @Transactional
    void testDeactivate() {
        LrsConnection created = this.lrsService.createConnection(this.sample);
        LrsConnection expected = this.lrsService.deactivateConnection(created.getConnectionId());
        LrsConnection saved = this.lrsService.getConnection(created.getConnectionId());
        assertThat(saved).isEqualTo(expected);
        assertThat(saved.isEnabled()).isFalse();
    }

    @Test @Transactional
    void testGetConnections() {
        LrsConnection created = this.lrsService.createConnection(this.sample);
        assertThat(this.lrsService.getConnections(true)).hasSize(1);
        this.lrsService.deactivateConnection(created.getConnectionId());
        assertThat(this.lrsService.getConnections(true)).hasSize(0);
    }

    @Test @Transactional
    void testUpdateConnection() throws MalformedURLException {
        LrsConnection created = this.lrsService.createConnection(this.sample);
        LrsConnectionTO updated = new LrsConnectionTO(Optional.of(created.getConnectionId()), "AnotherName", new URL("http://sut:12345"), "foo", "bar", Optional.of(true));
        LrsConnectionTO received = LrsConnectionTO.of(this.lrsService.updateConnection(updated));
        LrsConnectionTO saved = LrsConnectionTO.of(this.lrsService.getConnection(created.getConnectionId()));
        assertThat(received).isEqualTo(updated);
        assertThat(received).isEqualTo(saved);
    }
}
