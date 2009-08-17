package de.fraunhofer.fit.photocompass.activities;

import de.fraunhofer.fit.photocompass.model.Settings;

/**
 * This interface must be implemented by all activities that want to use the {@link ServiceConnections} decorator.
 */
public interface IServiceActivity {
    
    /**
     * Called by the onMinDistanceChange method of the settings service callback object in the
     * {@link ServiceConnections} decorator.
     * 
     * @param minDistance Minimum distance in meters.
     * @param minDistanceRel Relative minimum distance (0..1).
     */
    void onSettingsServiceMinDistanceChange(final float minDistance, final float minDistanceRel);
    
    /**
     * Called by the onMaxDistanceChange method of the settings service callback object in the
     * {@link ServiceConnections} decorator.
     * 
     * @param maxDistance Maximum distance in meters.
     * @param maxDistanceRel Relative maximum distance (0..1).
     */
    void onSettingsServiceMaxDistanceChange(final float maxDistance, final float maxDistanceRel);
    
    /**
     * Called by the onMinAgeChange method of the settings service callback object in the {@link ServiceConnections}
     * decorator.
     * 
     * @param minAge Minimum age in milliseconds.
     * @param minAgeRel Relative minimum age (0..1).
     */
    void onSettingsServiceMinAgeChange(final long minAge, final float minAgeRel);
    
    /**
     * Called by the onMaxAgeChange method of the settings service callback object in the {@link ServiceConnections}
     * decorator.
     * 
     * @param maxAge Maximum age in milliseconds.
     * @param maxAgeRel Relative maximum age (0..1).
     */
    void onSettingsServiceMaxAgeChange(final long maxAge, final float maxAgeRel);
    
    /**
     * Called by the onPhotoDistancesChange method of the photos service callback object in the
     * {@link ServiceConnections} decorator.
     * 
     * @param photoDistances Distances of the photos in relative values (0..1).
     */
    void onPhotosServicePhotoDistancesChange(final float[] photoDistances);
    
    /**
     * Called by the onPhotoAgesChange method of the photos service callback object in the {@link ServiceConnections}
     * decorator.
     * 
     * @param photoAges Ages of the photos in relative values (0..1).
     */
    void onPhotosServicePhotoAgesChange(final float[] photoAges);
    
    /**
     * Called by the onLocationEvent method of the location service callback object in the {@link ServiceConnections}
     * decorator.
     * 
     * @param lat Latitude.
     * @param lng Longitude.
     * @param hasAlt <code>true</code> if altitude data is available, <code>false</code> if not.
     * @param alt Altitude.
     */
    void onLocationServiceLocationEvent(final double lat, final double lng, final boolean hasAlt, final double alt);
    
    /**
     * Called by the onOrientationEvent method of the orientation service callback object in the
     * {@link ServiceConnections} decorator.
     * 
     * @param yaw Yaw (from 0 to 360).
     * @param pitch Pitch (from -90 to 90).
     * @param roll Roll (from -180 to 180).
     */
    void onOrientationServiceOrientationEvent(final float yaw, final float pitch, final float roll);
    
    /**
     * Provides access to the {@link Settings} instance from the settings service.
     * 
     * @return {@link Settings}
     */
    Settings getSettings();
    
    /**
     * Initiates an update of the {@link Settings} instance to the settings service.
     * 
     * @param settings {@link Settings}
     */
    void updateSettings(Settings settings);
}
