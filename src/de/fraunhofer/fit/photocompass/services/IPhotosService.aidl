package de.fraunhofer.fit.photocompass.services;

import de.fraunhofer.fit.photocompass.services.IPhotosServiceCallback;
import de.fraunhofer.fit.photocompass.model.data.Photo;

/**
 * Interface for registering to the photos service
 */
interface IPhotosService {
    
    void updatePhotos();
    
    Photo getPhoto(int id);
    
    int[] getNewlyVisiblePhotos(in int[] currentPhotos, boolean limitByDistance, boolean limitByAge);
    
    int[] getNoLongerVisiblePhotos(in int[] currentPhotos, boolean limitByDistance, boolean limitByAge);
    
    void updatePhotoProperties(double lat, double lng, double alt);
    
    void updateAppModelMaxValues();

    void registerCallback(IPhotosServiceCallback cb);

    void unregisterCallback(IPhotosServiceCallback cb);
}