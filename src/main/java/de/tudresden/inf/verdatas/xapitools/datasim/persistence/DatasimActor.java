package de.tudresden.inf.verdatas.xapitools.datasim.persistence;

/**
 * Actor from DATASIMs API specification
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface DatasimActor {
    /**
     * Get the Identifier of the Actor
     *
     * @return Identifier in IRI format
     */
    String getIri();

    /**
     * Get the Type of the Actor
     *
     * @return Type, i.e. Agent or Group
     */
    DatasimActorType getType();
}
