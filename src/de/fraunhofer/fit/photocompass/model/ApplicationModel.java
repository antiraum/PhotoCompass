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
	final float MAX_DISTANCE_LIMIT = 10 * 1000F; // in meters
	private float MIN_MIN_DISTANCE = 0F; // in meters
	private float MAX_MAX_DISTANCE = MAX_DISTANCE_LIMIT; // in meters
	final long MAX_AGE_LIMIT = 30 * 24 * 60 * 60 * 1000L; // in milliseconds
	private long MIN_MIN_AGE = 0L; // in milliseconds
	private long MAX_MAX_AGE = MAX_AGE_LIMIT; // in milliseconds

    private static ApplicationModel _instance;

    // current settings
    private float _minDistance = MIN_MIN_DISTANCE; // in meters
	private float _maxDistance = MAX_MAX_DISTANCE; // in meters
	private long _minAge = MIN_MIN_AGE; // in milliseconds
	private long _maxAge = MAX_MAX_AGE; // in milliseconds

    private final RemoteCallbackList<IApplicationModelCallback> _remoteCallbacks = new RemoteCallbackList<IApplicationModelCallback>();
	
    /**
     * Constructor.
     * Sets the initial values.
     */
	protected ApplicationModel() {
		super();
		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: MAX_MAX_DISTANCE = "+MAX_MAX_DISTANCE+", MAX_MAX_AGE = "+MAX_MAX_AGE);
	}

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
     * Set the minimum value for minimum distance.
     * Call this from the {@link Photos} model when the photos are read of the device.
     */
    public void setMinMinDistance(final float value) {
    	MIN_MIN_DISTANCE = (value < 0) ? 0 : value;
    	if (_minDistance < MIN_MIN_DISTANCE) setMinDistance(MIN_MIN_DISTANCE);
    }
    
    /**
     * Set the maximum value for maximum distance.
     * Call this from the {@link Photos} model when the photos are read of the device.
     */
    public void setMaxMaxDistance(final float value) {
//		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: setMaxMaxDistance: "+value);
    	MAX_MAX_DISTANCE = (value > MAX_DISTANCE_LIMIT) ? MAX_DISTANCE_LIMIT : value;
    	if (_maxDistance > MAX_MAX_DISTANCE) setMaxDistance(MAX_MAX_DISTANCE);
    }
    
    /**
     * Set the minimum value for minimum age.
     * Call this from the {@link Photos} model when the photos are read of the device.
     */
    public void setMinMinAge(final long value) {
    	MIN_MIN_AGE = (value < 0) ? 0 : value;
    	if (_minAge < MIN_MIN_AGE) setMinAge(MIN_MIN_AGE);
    }
    
    /**
     * Set the maximum value for maximum age.
     * Call this from the {@link Photos} model when the photos are read of the device.
     */
    public void setMaxMaxAge(final long value) {
    	MAX_MAX_AGE = (value > MAX_AGE_LIMIT) ? MAX_AGE_LIMIT : value;
    	if (_maxAge > MAX_MAX_AGE) setMaxAge(MAX_MAX_AGE);
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
		float relMinDist = (MAX_MAX_DISTANCE == 0) ? 0F : _minDistance / (MAX_MAX_DISTANCE - MIN_MIN_DISTANCE);
    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: getRelativeMinDistance = "+relMinDist+", _minDistance = "+_minDistance+", MAX_MAX_DISTANCE = "+MAX_MAX_DISTANCE);
		return relMinDist;
	}

    /**
     * @return The current minimum distance for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMinDistance() {
		String str = OutputFormatter.formatDistance(_minDistance);
    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: getFormattedMinDistance = "+str);
		return str;
	}

	/**
	 * @param value The new minimum distance for photos to be displayed. In meters.
	 */
	public void setMinDistance(final float minDistance) {
		_minDistance = minDistance;
		_broadcastChange();
	}

	/**
	 * @param value The new minimum distance for photos to be displayed. From 0..1.
	 */
	public void setRelativeMinDistance(final float relativeMinDistance) {
		setMinDistance(relativeMinDistance * (MAX_MAX_DISTANCE - MIN_MIN_DISTANCE));
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
		float relMaxDist = (MAX_MAX_DISTANCE == 0) ? 0F : _maxDistance / (MAX_MAX_DISTANCE - MIN_MIN_DISTANCE);
    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: getRelativeMaxDistance = "+relMaxDist+", _maxDistance = "+_maxDistance+", MAX_MAX_DISTANCE = "+MAX_MAX_DISTANCE);
		return relMaxDist;
	}

    /**
     * @return The current maximum distance for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMaxDistance() {
		String str = OutputFormatter.formatDistance(_maxDistance);
    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: getFormattedMaxDistance = "+str);
		return str;
	}

	/**
	 * @param value The new maximum distance for photos to be displayed.
	 */
	public void setMaxDistance(final float maxDistance) {
		_maxDistance = maxDistance;
		_broadcastChange();
	}

	/**
	 * @param value The new maximum distance for photos to be displayed. From 0..1.
	 */
	public void setRelativeMaxDistance(final float relativeMaxDistance) {
		setMaxDistance(relativeMaxDistance * (MAX_MAX_DISTANCE - MIN_MIN_DISTANCE));
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
		float relMinAge = (MAX_MAX_AGE == 0) ? 0F : (float) _minAge / (float) (MAX_MAX_AGE - MIN_MIN_AGE);
    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: getRelativeMinAge = "+relMinAge);
		return relMinAge;
	}

    /**
     * @return The current minimum age for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMinAge() {
		String str = OutputFormatter.formatAge(_minAge);
    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: getFormattedMinAge = "+str);
		return str;
	}

	/**
	 * @param value The new minimum age for photos to be displayed. In milliseconds.
	 */
	public void setMinAge(final long minAge) {
		_minAge = minAge;
		_broadcastChange();
	}

	/**
	 * @param value The new minimum age for photos to be displayed. From 0..1.
	 */
	public void setRelativeMinAge(final float relativeMinAge) {
		setMinAge(Math.round(relativeMinAge * MAX_MAX_AGE));
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
		float relMaxAge = (MAX_MAX_AGE == 0) ? 0F : (float) _maxAge / (float) (MAX_MAX_AGE - MIN_MIN_AGE);
    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: getRelativeMaxAge = "+relMaxAge);
		return relMaxAge;
	}

    /**
     * @return The current maximum age for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMaxAge() {
		String str = OutputFormatter.formatAge(_maxAge);
    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: getFormattedMaxAge = "+str+", _maxAge = "+_maxAge);
		return str;
	}

	/**
	 * @param value The new maximum age for photos to be displayed.
	 */
	public void setMaxAge(final long maxAge) {
		_maxAge = maxAge;
		_broadcastChange();
	}

	/**
	 * @param value The new maximum age for photos to be displayed. From 0..1.
	 */
	public void setRelativeMaxAge(final float relativeMaxAge) {
		setMaxAge(Math.round(relativeMaxAge * (MAX_MAX_AGE - MIN_MIN_AGE)));
	}
	
    /**
     * Broadcasts the application model change to all registered callbacks.
     * Gets called by the setter methods.
     */
	private void _broadcastChange() {
	    final int numCallbacks = _remoteCallbacks.beginBroadcast();
	    for (int i = 0; i < numCallbacks; i++) {
	        try {
	            _remoteCallbacks.getBroadcastItem(i).onApplicationModelChange();
	        } catch (final DeadObjectException e) {
	            // the RemoteCallbackList will take care of removing the dead object
	        } catch (final RemoteException e) {
		    	Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: broadcast to callback failed");
	        }
	    }
	    _remoteCallbacks.finishBroadcast();
	}
}
