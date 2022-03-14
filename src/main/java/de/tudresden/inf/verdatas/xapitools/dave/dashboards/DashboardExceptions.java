package de.tudresden.inf.verdatas.xapitools.dave.dashboards;

public class DashboardExceptions {
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
}
