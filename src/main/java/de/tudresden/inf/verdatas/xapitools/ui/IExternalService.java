package de.tudresden.inf.verdatas.xapitools.ui;

/**
 * Interface to represent an External Service which runs outside this application.
 * Implement this for the Service to appear on the UI with a Health Check.
 * External Services are ordered naturally.
 *
 * @author Konstantin Köhring (@Galaxy102)
 */
public interface IExternalService {
    /**
     * Get the Human readable name of this Service.
     *
     * @return Name of service
     */
    String getName();

    /**
     * Get the Path to the Health Check Endpoint for this Service.
     *
     * @return Path to Health Endpoint
     */
    String getCheckEndpoint();
}
