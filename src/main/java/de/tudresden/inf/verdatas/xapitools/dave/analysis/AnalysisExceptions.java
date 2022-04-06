package de.tudresden.inf.verdatas.xapitools.dave.analysis;

/**
 * Customized Exceptions for Analysis handling
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
public class AnalysisExceptions {
    /**
     * Duplication of Analysis or its parts
     */
    public static class ConfigurationConflict extends IllegalStateException {
        public ConfigurationConflict(String message) {
            super(message);
        }
    }

    /**
     * Configuration of an Analysis is not completed properly
     */
    public static class InvalidConfiguration extends IllegalStateException {
        public InvalidConfiguration(String message) {
            super(message);
        }
    }

    /**
     * Deletion of an Analysis not possible because it is still used
     */
    public static class SideEffectsError extends RuntimeException {
        public SideEffectsError(String message) {
            super(message);
        }
    }
}
