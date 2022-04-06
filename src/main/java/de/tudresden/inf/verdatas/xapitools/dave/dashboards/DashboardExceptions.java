package de.tudresden.inf.verdatas.xapitools.dave.dashboards;

/**
 * Customized Exceptions for Dashboard handling
 *
 * @author Ylvi Sarah Bachmann (@ylvion)
 */
public class DashboardExceptions {
    /**
     * Duplication of Dashboard Title
     */
    public static class ConfigurationConflict extends IllegalStateException {
        public ConfigurationConflict(String message) {
            super(message);
        }
    }

    /**
     * Configuration of a Dashboard is not completed properly
     */
    public static class InvalidConfiguration extends IllegalStateException {
        public InvalidConfiguration(String message) {
            super(message);
        }
    }
}
