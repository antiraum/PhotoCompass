package de.fraunhofer.fit.photocompass.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.util.OutputFormatter;

/**
 * This model stores the current values for distance and age limitations of the displayed photos. The values are set by
 * the controls in the {@link de.fraunhofer.fit.photocompass.views.ControlsView}. This is an active model, where
 * Activities can register as callbacks in order to get updates when the values change. This is a Singleton.
 */
public final class Settings {
    
    // minimum & maximum values
    private float MIN_DISTANCE_LIMIT = 1000F; // in meters
    public float MAX_DISTANCE_LIMIT = 5 * 1000F; // in meters
    public float MAX_MAX_DISTANCE = MAX_DISTANCE_LIMIT; // in meters
    private long MIN_AGE_LIMIT = 60 * 60 * 1000L; // in milliseconds
    public long MAX_AGE_LIMIT = 54 * 7 * 24 * 60 * 60 * 1000L; // in milliseconds
    public long MAX_MAX_AGE = MAX_AGE_LIMIT; // in milliseconds
    
    /**
     * The current minimum distance for photos to be displayed. In meters.
     */
    public float minDistance = 0F;
    /**
     * The current minimum distance for photos to be displayed. From 0..1.
     */
    public float minDistanceRel = 0F;
    /**
     * The current minimum distance for photos to be displayed. As a formated string for display.
     */
    public String minDistanceStr = OutputFormatter.formatDistance(minDistance);
    /**
     * The current maximum distance for photos to be displayed. In meters.
     */
    public float maxDistance = MAX_MAX_DISTANCE;
    /**
     * The current maximum distance for photos to be displayed. From 0..1.
     */
    public float maxDistanceRel = 1F;
    /**
     * The current maximum distance for photos to be displayed. As a formated string for display.
     */
    public String maxDistanceStr = OutputFormatter.formatDistance(maxDistance);
    /**
     * The current minimum age for photos to be displayed. In milliseconds.
     */
    public long minAge = 0L;
    /**
     * The current minimum age for photos to be displayed. From 0..1.
     */
    public float minAgeRel = 0F;
    /**
     * The current minimum age for photos to be displayed. As a formated string for display.
     */
    public String minAgeStr = OutputFormatter.formatAge(minAge);
    /**
     * The current maximum age for photos to be displayed. In milliseconds.
     */
    public long maxAge = MAX_MAX_AGE;
    /**
     * The current maximum age for photos to be displayed. From 0..1.
     */
    public float maxAgeRel = 1F;
    /**
     * The current maximum age for photos to be displayed. As a formated string for display.
     */
    public String maxAgeStr = OutputFormatter.formatAge(maxAge);
    
    /**
     * List of callbacks that have been registered with the model.
     */
    private final RemoteCallbackList<ISettingsCallback> _remoteCallbacks = new RemoteCallbackList<ISettingsCallback>();
    
    /**
     * Constructor.
     */
    public Settings() {

    // nothing to do here
    }
    
    /**
     * Register a callback object.
     * 
     * @param cb Callback object.
     */
    public void registerCallback(final ISettingsCallback cb) {

        Log.d(PhotoCompassApplication.LOG_TAG, "Settings: registerCallback");
        
        if (cb == null) return;
        _remoteCallbacks.register(cb);
    }
    
    /**
     * Unregister a callback object.
     * 
     * @param cb Callback object.
     */
    public void unregisterCallback(final ISettingsCallback cb) {

        if (cb == null) return;
        _remoteCallbacks.unregister(cb);
    }
    
    /**
     * Set the maximum value for maximum distance. Call this from the {@link PhotosModel} model when the photos are read
     * of the device.
     * 
     * @param value
     * @return <code>true</code> if {@link #MAX_MAX_DISTANCE} was changed, or <code>false</code> if no change.
     */
    public boolean setMaxMaxDistance(final float value) {

//		Log.d(PhotoCompassApplication.LOG_TAG, "Settings: setMaxMaxDistance = "+value);
        
        final float oldValue = MAX_MAX_DISTANCE;
        MAX_MAX_DISTANCE = (value > MAX_DISTANCE_LIMIT) ? MAX_DISTANCE_LIMIT
                                                       : (value < MIN_DISTANCE_LIMIT) ? MIN_DISTANCE_LIMIT : value;
        if (maxDistance != MAX_MAX_DISTANCE) {
            setMaxDistance(MAX_MAX_DISTANCE);
        }
        return (oldValue == MAX_MAX_DISTANCE) ? false : true;
    }
    
    /**
     * Set the maximum value for maximum age. Call this from the {@link PhotosModel} model when the photos are read of
     * the device.
     * 
     * @param value Maximum value for maximum age. In milliseconds.
     * @return <code>true</code> if {@link #MAX_MAX_DISTANCE} was changed, or <code>false</code> if no change.
     */
    public boolean setMaxMaxAge(final long value) {

//		Log.d(PhotoCompassApplication.LOG_TAG, "Settings: setMaxMaxAge = "+value);
        
        final long oldValue = MAX_MAX_AGE;
        MAX_MAX_AGE = (value > MAX_AGE_LIMIT) ? MAX_AGE_LIMIT : (value < MIN_AGE_LIMIT) ? MIN_AGE_LIMIT : value;
        if (maxAge != MAX_MAX_AGE) {
            setMaxAge(MAX_MAX_AGE);
        }
        return (oldValue == MAX_MAX_AGE) ? false : true;
    }
    
    /**
     * @param value The new minimum distance for photos to be displayed. In meters.
     */
    public void setMinDistance(final float value) {

        // update values
        minDistance = value;
        minDistanceRel = absoluteToRelativeDistance(minDistance);
        minDistanceStr = OutputFormatter.formatDistance(minDistance);
        
//		Log.d(PhotoCompassApplication.LOG_TAG,
//		      "Settings: setMaxDistance: minDist = "+minDistance+", maxDist = "+maxDistance);
        
        // broadcast change
        final int numCallbacks = _remoteCallbacks.beginBroadcast();
        for (int i = 0; i < numCallbacks; i++) {
            try {
                _remoteCallbacks.getBroadcastItem(i).onMinDistanceChange(minDistance, minDistanceRel);
            } catch (final RemoteException e) {
                Log.d(PhotoCompassApplication.LOG_TAG, "Settings: broadcast to callback failed");
            }
        }
        _remoteCallbacks.finishBroadcast();
    }
    
    /**
     * @param value The new minimum distance for photos to be displayed. From 0..1.
     */
    public void setRelativeMinDistance(final float value) {

        setMinDistance(value * MAX_MAX_DISTANCE);
    }
    
    /**
     * @param value The new maximum distance for photos to be displayed.
     */
    public void setMaxDistance(final float value) {

        // update values
        maxDistance = value;
        maxDistanceRel = absoluteToRelativeDistance(maxDistance);
        maxDistanceStr = OutputFormatter.formatDistance(maxDistance);
        
//		Log.d(PhotoCompassApplication.LOG_TAG,
//		      "Settings: setMaxDistance: minDist = "+minDistance+", maxDist = "+maxDistance);
        
        // broadcast change
        final int numCallbacks = _remoteCallbacks.beginBroadcast();
        for (int i = 0; i < numCallbacks; i++) {
            try {
                _remoteCallbacks.getBroadcastItem(i).onMaxDistanceChange(maxDistance, maxDistanceRel);
            } catch (final RemoteException e) {
                Log.d(PhotoCompassApplication.LOG_TAG, "Settings: broadcast to callback failed");
            }
        }
        _remoteCallbacks.finishBroadcast();
    }
    
    /**
     * Translates an absolute distance in meters to a relative distance (0..1).
     * 
     * @param absoluteDistance Distance in meters.
     * @return Distance in 0..1.
     */
    public float absoluteToRelativeDistance(final float absoluteDistance) {

        return (MAX_MAX_DISTANCE == 0) ? 0F : absoluteDistance / MAX_MAX_DISTANCE;
    }
    
    /**
     * @param value The new maximum distance for photos to be displayed. From 0..1.
     */
    public void setRelativeMaxDistance(final float value) {

        setMaxDistance(value * MAX_MAX_DISTANCE);
    }
    
    /**
     * @param value The new minimum age for photos to be displayed. In milliseconds.
     */
    public void setMinAge(final long value) {

        // update values
        minAge = value;
        minAgeRel = absoluteToRelativeAge(minAge);
        minAgeStr = OutputFormatter.formatAge(minAge);
        
        // broadcast change
        final int numCallbacks = _remoteCallbacks.beginBroadcast();
        for (int i = 0; i < numCallbacks; i++) {
            try {
                _remoteCallbacks.getBroadcastItem(i).onMinAgeChange(minAge, minAgeRel);
            } catch (final RemoteException e) {
                Log.d(PhotoCompassApplication.LOG_TAG, "Settings: broadcast to callback failed");
            }
        }
        _remoteCallbacks.finishBroadcast();
    }
    
    /**
     * @param value The new minimum age for photos to be displayed. From 0..1.
     */
    public void setRelativeMinAge(final float value) {

//		Log.d(PhotoCompassApplication.LOG_TAG, "Settings: setRelativeMinAge = "+relativeMinAge);
        
        setMinAge(Math.round((double) value * MAX_MAX_AGE));
    }
    
    /**
     * @param value The new maximum age for photos to be displayed.
     */
    public void setMaxAge(final long value) {

//		Log.d(PhotoCompassApplication.LOG_TAG, "Settings: setMaxAge = "
//				+ value + ", i.e. " + OutputFormatter.formatAge(value));
        
        // update values
        maxAge = value;
        maxAgeRel = absoluteToRelativeAge(maxAge);
        maxAgeStr = OutputFormatter.formatAge(maxAge);
        
        // broadcast change
        final int numCallbacks = _remoteCallbacks.beginBroadcast();
        for (int i = 0; i < numCallbacks; i++) {
            try {
                _remoteCallbacks.getBroadcastItem(i).onMaxAgeChange(maxAge, maxAgeRel);
            } catch (final RemoteException e) {
                Log.d(PhotoCompassApplication.LOG_TAG, "Settings: broadcast to callback failed");
            }
        }
        _remoteCallbacks.finishBroadcast();
    }
    
    /**
     * Translates an absolute age in milliseconds to a relative age (0..1).
     * 
     * @param absoluteAge Age in milliseconds.
     * @return Age in 0..1.
     */
    public float absoluteToRelativeAge(final long absoluteAge) {

        return (MAX_MAX_AGE == 0) ? 0F : (float) absoluteAge / (float) MAX_MAX_AGE;
    }
    
    /**
     * @param value The new maximum age for photos to be displayed. From 0..1.
     */
    public void setRelativeMaxAge(final float value) {

        setMaxAge(Math.round((double) value * MAX_MAX_AGE));
    }
    
    public int describeContents() {

        return 0;
    }
    
    public void writeToParcel(final Parcel out, final int flags) {

        out.writeFloat(MIN_DISTANCE_LIMIT);
        out.writeFloat(MAX_DISTANCE_LIMIT);
        out.writeFloat(MAX_MAX_DISTANCE);
        out.writeLong(MIN_AGE_LIMIT);
        out.writeLong(MAX_AGE_LIMIT);
        out.writeLong(MAX_MAX_AGE);
        out.writeFloat(minDistance);
        out.writeFloat(minDistanceRel);
        out.writeString(minDistanceStr);
        out.writeFloat(maxDistance);
        out.writeFloat(maxDistanceRel);
        out.writeString(maxDistanceStr);
        out.writeLong(minAge);
        out.writeFloat(minAgeRel);
        out.writeString(minAgeStr);
        out.writeLong(maxAge);
        out.writeFloat(maxAgeRel);
        out.writeString(maxAgeStr);
    }
    
    public static final Parcelable.Creator<Settings> CREATOR = new Parcelable.Creator<Settings>() {
        
        public Settings createFromParcel(final Parcel in) {

            return new Settings(in);
        }
        
        public Settings[] newArray(final int size) {

            return new Settings[size];
        }
    };
    
    /**
     * Package scoped for faster access by inner classes.
     * 
     * @param in
     */
    Settings(final Parcel in) {

        MIN_DISTANCE_LIMIT = in.readFloat();
        MAX_DISTANCE_LIMIT = in.readFloat();
        MAX_MAX_DISTANCE = in.readFloat();
        MIN_AGE_LIMIT = in.readLong();
        MAX_AGE_LIMIT = in.readLong();
        MAX_MAX_AGE = in.readLong();
        minDistance = in.readFloat();
        minDistanceRel = in.readFloat();
        minDistanceStr = in.readString();
        maxDistance = in.readFloat();
        maxDistanceRel = in.readFloat();
        maxDistanceStr = in.readString();
        minAge = in.readLong();
        minAgeRel = in.readFloat();
        minAgeStr = in.readString();
        maxAge = in.readLong();
        maxAgeRel = in.readFloat();
        maxAgeStr = in.readString();
    }
}
