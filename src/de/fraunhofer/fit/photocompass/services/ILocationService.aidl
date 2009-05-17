package de.fraunhofer.fit.photocompass.services;

import de.fraunhofer.fit.photocompass.services.ILocationServiceCallback;

/**
 * Interface for registering to the location service
 */
interface ILocationService {

    void registerCallback(ILocationServiceCallback cb);

    void unregisterCallback(ILocationServiceCallback cb);
}