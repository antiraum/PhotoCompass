package de.fraunhofer.fit.photocompass.services;

/**
 * Callback interface for location service events.
 */
interface ILocationServiceCallback {

    /**
     * Called when the service has a new value.
     */
    void onLocationEvent(double lat, double lng, boolean hasAlt, double alt);
}