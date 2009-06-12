package de.fraunhofer.fit.photocompass.services;

/**
 * Callback interface for orientation service events.
 */
interface IOrientationServiceCallback {

    /**
     * Called when the service has a new value.
     */
    void onOrientationEvent(float yaw, float pitch, float roll);
}