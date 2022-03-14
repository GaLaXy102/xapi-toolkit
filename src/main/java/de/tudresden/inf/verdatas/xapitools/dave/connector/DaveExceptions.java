package de.tudresden.inf.verdatas.xapitools.dave.connector;

public class DaveExceptions {

    public static class NoDaveConnection extends RuntimeException {
        public NoDaveConnection(String message) {
            super(message);
        }
    }

    public static class AnalysisExecutionException extends RuntimeException {
        public AnalysisExecutionException(String message) {
            super(message);
        }
    }

    public static class AnalysisConfigurationException extends RuntimeException {
        public AnalysisConfigurationException(String message) {
            super(message);
        }
    }

    public static class AnalysisResultError extends RuntimeException {
        public AnalysisResultError(String message) {
            super(message);
        }
    }
}
