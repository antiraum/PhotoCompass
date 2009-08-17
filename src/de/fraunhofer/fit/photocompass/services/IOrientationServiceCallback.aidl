package de.fraunhofer.fit.photocompass.services;

/**
 * Callback interface for orientation service events.
 */
interface IOrientationServiceCallback {

    /**
     * Called when new orientation data is available.
     *
     * @param yaw Yaw (from 0 to 360).
     * @param pitch Pitch (from -90 to 90).
     * @param roll Roll (from -180 to 180).
     */
    void onOrientationEvent(float yaw, float pitch, float roll);
}