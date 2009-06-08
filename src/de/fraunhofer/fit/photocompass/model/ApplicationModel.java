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
	
	// default values
	private static final int MAX_MAX_DISTANCE = 10 * 1000; // in meters TODO get this from Photos
	private static final long MAX_MAX_AGE = 30 * 24 * 60 * 60 * 1000L; // in milliseconds

    private static ApplicationModel _instance;

    private float _minDistance = 0; // in meters
	private float _maxDistance = MAX_MAX_DISTANCE; // in meters
	// TODO age should not be set relative
	private long _minAge = 0; // in milliseconds
	private long _maxAge = MAX_MAX_AGE; // in milliseconds

    private final RemoteCallbackList<IApplicationModelCallback> _remoteCallbacks = new RemoteCallbackList<IApplicationModelCallback>();
	
    /**
     * Constructor.
     * Sets the initial values.
     */
	protected ApplicationModel() {
		super();
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
     * @return The current minimum distance for photos to be displayed. In meters.
     */
	public float getMinDistance() {
		return _minDistance;
	}

	/**
	 * @return The current minimum distance for photos to be displayed. From 0..1;
	 */
	public float getRelativeMinDistance() {
		return _minDistance / MAX_MAX_DISTANCE;
	}
	
	public String getFormattedMinDistance() {
		return OutputFormatter.formatDistance(_minDistance);
	}

	/**
	 * @param value The new minimum distance for photos to be displayed.
	 */
	public void setMinDistance(final float minDistance) {
		_minDistance = minDistance;
		_broadcastChange();
	}
	
	public void setRelativeMinDistance(final float relativeMinDistance) {
		setMinDistance(relativeMinDistance * MAX_MAX_DISTANCE);
	}

    /**
     * @return The current maximum distance for photos to be displayed. In meters.
     */
	public float getMaxDistance() {
		return _maxDistance;
	}

	/**
	 * @return The current maximum distance for photos to be displayed. From 0..1;
	 */
	public float getRelativeMaxDistance() {
		return _maxDistance / MAX_MAX_DISTANCE;
	}
	
	public String getFormattedMaxDistance() {
		return OutputFormatter.formatDistance(_maxDistance);
	}

	/**
	 * @param value The new maximum distance for photos to be displayed.
	 */
	public void setMaxDistance(final float maxDistance) {
		_maxDistance = maxDistance;
		_broadcastChange();
	}
	
	public void setRelativeMaxDistance(final float relativeMaxDistance) {
		setMaxDistance(relativeMaxDistance * MAX_MAX_DISTANCE);
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
		return (float) _minAge / (float) MAX_MAX_AGE;
	}

    /**
     * @return The current minimum age for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMinAge() {
		return OutputFormatter.formatAge(_minAge);
	}

	/**
	 * @param value The new minimum age for photos to be displayed.
	 */
	public void setMinAge(final long minAge) {
		_minAge = minAge;
		_broadcastChange();
	}
	
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
		return (float) _maxAge / (float) MAX_MAX_AGE;
	}

    /**
     * @return The current maximum age for photos to be displayed. As a formated string for display.
     */
	public String getFormattedMaxAge() {
		return OutputFormatter.formatAge(_maxAge);
	}

	/**
	 * @param value The new maximum age for photos to be displayed.
	 */
	public void setMaxAge(final long maxAge) {
		_maxAge = maxAge;
		_broadcastChange();
	}
	
	public void setRelativeMaxAge(final float relativeMaxAge) {
		setMaxAge(Math.round(relativeMaxAge * MAX_MAX_AGE));
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
		    	Log.e(PhotoCompassApplication.LOG_TAG, "ApplicationModel: broadcast to callback failed");
	        }
	    }
	    _remoteCallbacks.finishBroadcast();
	}
}
