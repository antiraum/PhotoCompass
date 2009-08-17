package de.fraunhofer.fit.photocompass.services;

import de.fraunhofer.fit.photocompass.services.IPhotosServiceCallback;
import de.fraunhofer.fit.photocompass.model.Settings;
import de.fraunhofer.fit.photocompass.model.data.Photo;

/**
 * Interface for registering to the photos service
 */
interface IPhotosService {
    
    void initialize(in Settings settings);
    
    Photo getPhoto(int id);
    
    int[] getNewlyVisiblePhotos(in Settings settings, in int[] currentPhotos, boolean limitByDistance, boolean limitByAge);
    
    int[] getNoLongerVisiblePhotos(in Settings settings, in int[] currentPhotos, boolean limitByDistance, boolean limitByAge);
    
    Settings updatePhotoProperties(in Settings settings, double lat, double lng, double alt);
    
    Settings updateAppModelMaxValues(in Settings settings);

    void registerCallback(IPhotosServiceCallback cb);

    void unregisterCallback(IPhotosServiceCallback cb);
}