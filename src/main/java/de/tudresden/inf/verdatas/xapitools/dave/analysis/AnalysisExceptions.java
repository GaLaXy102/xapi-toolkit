package de.tudresden.inf.verdatas.xapitools.dave.analysis;

public class AnalysisExceptions {
    public static class ConfigurationConflict extends IllegalStateException {
        public ConfigurationConflict(String message) {
            super(message);
        }
    }

    public static class InvalidConfiguration extends IllegalStateException {
        public InvalidConfiguration(String message) {
            super(message);
        }
    }

    public static class SideEffectsError extends RuntimeException {
        public SideEffectsError(String message) {
            super(message);
        }
    }
}
