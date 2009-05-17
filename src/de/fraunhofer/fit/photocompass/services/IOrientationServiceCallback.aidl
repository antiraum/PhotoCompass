package de.fraunhofer.fit.photocompass.services;

/**
 * Callback interface for orientation service events.
 */
interface IOrientationServiceCallback {

    /**
     * Called when the service has a new value.
     */
    void onOrientationEvent(float azimuth, float pitch, float roll);
}