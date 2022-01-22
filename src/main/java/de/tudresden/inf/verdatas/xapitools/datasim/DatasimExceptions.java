package de.tudresden.inf.verdatas.xapitools.datasim;

import java.util.NoSuchElementException;

public class DatasimExceptions {

    /**
     * Datasim is not connected properly
     */
    public static class NoDatasimConnection extends RuntimeException {
        public NoDatasimConnection(String message) {
            super(message);
        }
    }

    /**
     * Simulation result is unavailable
     *
     * We want to subclass that as they are stored in a volume and may become inaccessible, even though the database still knows about them.
     */
    public static class NoSuchSimulationResult extends NoSuchElementException {
        public NoSuchSimulationResult(String s) {
            super(s);
        }
    }
}
