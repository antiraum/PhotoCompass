package de.fraunhofer.fit.photocompass.model;

/**
 * Callback interface for application model changes
 */
interface IApplicationModelCallback {

    /**
     * Called when the service has a new value.
     */
    void onApplicationModelChange();
}