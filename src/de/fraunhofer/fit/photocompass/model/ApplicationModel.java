package de.fraunhofer.fit.photocompass.model;

import android.os.DeadObjectException;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.util.OutputFormatter;

/**
 * This model stores the current values for distance and age limitations of the displayed photos.
 * The values are set by the controls in the {@link de.fraunhofer.fit.photocompass.views.ControlsView}.
 * This is an active model, where Activities can register as callbacks in order to get updates when the values change.
 * This is a Singleton.
 */
public final class ApplicationModel {
	
	// minimum & maximum values
	private final float MIN_DISTANCE_LIMIT = 1000F; // in meters
	final float MAX_DISTANCE_LIMIT = 15 * 1000F; // in meters
	private float MAX_MAX_DISTANCE = MAX_DISTANCE_LIMIT; // in meters
	private final long MIN_AGE_LIMIT = 60 * 60 * 1000L; // in milliseconds
	final long MAX_AGE_LIMIT = 30 * 24 * 60 * 60 * 1000L; // in milliseconds
	private long MAX_MAX_AGE = MAX_AGE_LIMIT; // in milliseconds

    private static ApplicationModel _instance;

    // current settings
    private float _minDistance = 0F; // in meters
    private float _minDistanceRel = 0F;
    private String _minDistanceStr = OutputFormatter.formatDistance(_minDistance);
	private float _maxDistance = MAX_MAX_DISTANCE; // in meters
	private float _maxDistanceRel = 1F;
	private String _maxDistanceStr = OutputFormatter.formatDistance(_maxDistance);
	private long _minAge = 0L; // in milliseconds
	private float _minAgeRel = 0F;
	private String _minAgeStr = OutputFormatter.formatAge(_minAge);
	private long _maxAge = MAX_MAX_AGE; // in milliseconds
	private float _maxAgeRel = 1F;
	private String _maxAgeStr = OutputFormatter.formatAge(_maxAge);

    private final RemoteCallbackList<IApplicationModelCallback> _remoteCallbacks = new RemoteCallbackList<IApplicationModelCallback>();

	/**
	 * @return The instance of this Singleton model.
	 */
    public static ApplicationModel getInstance() {
        if (_instance == null) _instance = new ApplicationModel();
        return _instance;
    }
    
    /**
     * Register a callback object.
     */
    public void registerCallback(final IApplicationModelCallback cb) {
        if (cb != null) _remoteCallbacks.register(cb);
    }
    
    /**
     * Unregister a callback object.
     */
    public void unregisterCallback(final IApplicationModelCallback cb) {
        if (cb != null) _remoteCallbacks.unregister(cb);
    }
    
    /**
     * Set the maximum value for maximum distance.
     * Call this from the {@link Photos} model when the photos are read of the device.
     */
    public void setMaxMaxDistance(final float value) {
//		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: setMaxMaxDistance = "+value);
    	MAX_MAX_DISTANCE = (value > MAX_DISTANCE_LIMIT) ? MAX_DISTANCE_LIMIT
    													: (value < MIN_DISTANCE_LIMIT) ? MIN_DISTANCE_LIMIT
    																				   : value;
    	if (_maxDistance != MAX_MAX_DISTANCE) setMaxDistance(MAX_MAX_DISTANCE);
    }
    
    /**
     * Set the maximum value for maximum age.
     * Call this from the {@link Photos} model when the photos are read of the device.
     */
    public void setMaxMaxAge(final long value) {
//		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: setMaxMaxAge = "+value);
    	MAX_MAX_AGE = (value > MAX_AGE_LIMIT) ? MAX_AGE_LIMIT
											  : (value < MIN_AGE_LIMIT) ? MIN_AGE_LIMIT
													  					: value;
    	if (_maxAge != MAX_MAX_AGE) setMaxAge(MAX_MAX_AGE);
    }

	/**
	 * @param value The new minimum distance for photos to be displayed. In meters.
	 */
	public void setMinDistance(final float minDistance) {
		
		// update values
		_minDistance = minDistance;
		_minDistanceRel = (MAX_MAX_DISTANCE == 0) ? 0F : _minDistance / MAX_MAX_DISTANCE;
		_minDistanceStr = OutputFormatter.formatDistance(_minDistance);

//		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: setMaxDistance: minDist = "+_minDistance+", maxDist = "+_maxDistance);
		
		// broadcast change
	    final int numCallbacks = _remoteCallbacks.beginBroadcast();
	    for (int i = 0; i < numCallbacks; i++) {
	        try {
	            _remoteCallbacks.getBroadcastItem(i).onMinDistanceChange(_minDistance, _minDistanceRel);
	        } catch (final DeadObjectException e) {
	            // the RemoteCallbackList will take care of removing the dead object
	        } catch (final RemoteException e) {
		    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: broadcast to callback failed");
	        }
	    }
	    _remoteCallbacks.finishBroadcast();
	}

	/**
	 * @param value The new minimum distance for photos to be displayed. From 0..1.
	 */
	public void setRelativeMinDistance(final float relativeMinDistance) {
		setMinDistance(relativeMinDistance * MAX_MAX_DISTANCE);
	}

    /**
     * @return The current minimum distance for photos to be displayed. In meters.
     */
	public float getMinDistance() {
		return _minDistance;
	}

	/**
	 * @return The current minimum distance for photos to be displayed. From 0..1;
	 */
	public float getRelativeMinDistance() {
		return _minDistanceRel;
	}

    /**
     * @return The current minimum distance for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMinDistance() {
		return _minDistanceStr;
	}

	/**
	 * @param value The new maximum distance for photos to be displayed.
	 */
	public void setMaxDistance(final float maxDistance) {
		
		// update values
		_maxDistance = maxDistance;
		_maxDistanceRel = (MAX_MAX_DISTANCE == 0) ? 0F : _maxDistance / MAX_MAX_DISTANCE;
		_maxDistanceStr = OutputFormatter.formatDistance(_maxDistance);

//		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: setMaxDistance: minDist = "+_minDistance+", maxDist = "+_maxDistance);
		
		// broadcast change
	    final int numCallbacks = _remoteCallbacks.beginBroadcast();
	    for (int i = 0; i < numCallbacks; i++) {
	        try {
	            _remoteCallbacks.getBroadcastItem(i).onMaxDistanceChange(_maxDistance, _maxDistanceRel);
	        } catch (final DeadObjectException e) {
	            // the RemoteCallbackList will take care of removing the dead object
	        } catch (final RemoteException e) {
		    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: broadcast to callback failed");
	        }
	    }
	    _remoteCallbacks.finishBroadcast();
	}

	/**
	 * @param value The new maximum distance for photos to be displayed. From 0..1.
	 */
	public void setRelativeMaxDistance(final float relativeMaxDistance) {
		setMaxDistance(relativeMaxDistance * MAX_MAX_DISTANCE);
	}

    /**
     * @return The current maximum distance for photos to be displayed. In meters.
     */
	public float getMaxDistance() {
		return _maxDistance;
	}

	/**
	 * @return The current maximum distance for photos to be displayed. From 0..1.
	 */
	public float getRelativeMaxDistance() {
		return _maxDistanceRel;
	}

    /**
     * @return The current maximum distance for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMaxDistance() {
		return _maxDistanceStr;
	}

	/**
	 * @param value The new minimum age for photos to be displayed. In milliseconds.
	 */
	public void setMinAge(final long minAge) {
		
		// update values
		_minAge = minAge;
		_minAgeRel = (MAX_MAX_AGE == 0) ? 0F : (float) _minAge / (float) MAX_MAX_AGE;
		_minAgeStr = OutputFormatter.formatAge(_minAge);
		
		// broadcast change
	    final int numCallbacks = _remoteCallbacks.beginBroadcast();
	    for (int i = 0; i < numCallbacks; i++) {
	        try {
	            _remoteCallbacks.getBroadcastItem(i).onMinAgeChange(_minAge, _minAgeRel);
	        } catch (final DeadObjectException e) {
	            // the RemoteCallbackList will take care of removing the dead object
	        } catch (final RemoteException e) {
		    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: broadcast to callback failed");
	        }
	    }
	    _remoteCallbacks.finishBroadcast();
	}

	/**
	 * @param value The new minimum age for photos to be displayed. From 0..1.
	 */
	public void setRelativeMinAge(final float relativeMinAge) {
//		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: setRelativeMinAge = "+relativeMinAge);
		setMinAge(Math.round(relativeMinAge * MAX_MAX_AGE));
	}

    /**
     * @return The current minimum age for photos to be displayed.
     */
	public long getMinAge() {
		return _minAge;
	}

    /**
     * @return The current minimum age for photos to be displayed. From 0..1.
     */
	public float getRelativeMinAge() {
		return _minAgeRel;
	}

    /**
     * @return The current minimum age for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMinAge() {
		return _minAgeStr;
	}

	/**
	 * @param value The new maximum age for photos to be displayed.
	 */
	public void setMaxAge(final long maxAge) {
//		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: setMaxAge = "+maxAge);
		
		// update values
		_maxAge = maxAge;
		_maxAgeRel = (MAX_MAX_AGE == 0) ? 0F : (float) _maxAge / (float) MAX_MAX_AGE;
		_maxAgeStr = OutputFormatter.formatAge(_maxAge);
		
		// broadcast change
	    final int numCallbacks = _remoteCallbacks.beginBroadcast();
	    for (int i = 0; i < numCallbacks; i++) {
	        try {
	            _remoteCallbacks.getBroadcastItem(i).onMaxAgeChange(_maxAge, _maxAgeRel);
	        } catch (final DeadObjectException e) {
	            // the RemoteCallbackList will take care of removing the dead object
	        } catch (final RemoteException e) {
		    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: broadcast to callback failed");
	        }
	    }
	    _remoteCallbacks.finishBroadcast();
	}

	/**
	 * @param value The new maximum age for photos to be displayed. From 0..1.
	 */
	public void setRelativeMaxAge(final float relativeMaxAge) {
//		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: setRelativeMaxAge = "+relativeMaxAge);
		setMaxAge(Math.round(relativeMaxAge * MAX_MAX_AGE));
	}

    /**
     * @return The current maximum age for photos to be displayed. In milliseconds.
     */
	public long getMaxAge() {
		return _maxAge;
	}

    /**
     * @return The current maximum age for photos to be displayed. From 0..1.
     */
	public float getRelativeMaxAge() {
		return _maxAgeRel;
	}

    /**
     * @return The current maximum age for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMaxAge() {
		return _maxAgeStr;
	}
}
