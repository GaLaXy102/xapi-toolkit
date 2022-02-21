package de.tudresden.inf.verdatas.xapitools.dave.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.UUID;

@NoArgsConstructor
public class AbstractDocument {
    @Id
    @Getter
    @Setter
    private UUID id = UUID.randomUUID();

    @Getter
    @Setter
    private String name;

    public AbstractDocument(String name) {
        this.name = name;
    }
}
