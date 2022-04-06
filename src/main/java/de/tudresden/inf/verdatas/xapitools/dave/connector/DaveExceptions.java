package de.tudresden.inf.verdatas.xapitools.dave.connector;

/**
 * Customized Exceptions for handling of DAVE Connectors
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
public class DaveExceptions {

    /**
     * No Connection to DAVE
     */
    public static class NoDaveConnection extends RuntimeException {
        public NoDaveConnection(String message) {
            super(message);
        }
    }

    /**
     * Exception occurred while executing Analysis
     */
    public static class AnalysisExecutionException extends RuntimeException {
        public AnalysisExecutionException(String message) {
            super(message);
        }
    }

    /**
     * Exception occurred while validating Analysis
     */
    public static class AnalysisConfigurationException extends RuntimeException {
        public AnalysisConfigurationException(String message) {
            super(message);
        }
    }

    /**
     * Exception occurred while Analysis result was requested
     */
    public static class AnalysisResultError extends RuntimeException {
        public AnalysisResultError(String message) {
            super(message);
        }
    }
}
