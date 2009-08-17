package de.fraunhofer.fit.photocompass.services;

/**
 * Callback interface for location service events.
 */
interface ILocationServiceCallback {

    /**
     * Called when new location data is available.
     *
     * @param lat Latitude.
     * @param lng Longitude.
     * @param hasAlt <code>true</code> if altitude data is available, <code>false</code> if not.
     * @param alt Altitude. 
     */
    void onLocationEvent(double lat, double lng, boolean hasAlt, double alt);
}