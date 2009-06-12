package de.fraunhofer.fit.photocompass.services;

import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;

/**
 * Interface for registering to the orientation service
 */
interface IOrientationService {

    void registerCallback(IOrientationServiceCallback cb);

    void unregisterCallback(IOrientationServiceCallback cb);
}