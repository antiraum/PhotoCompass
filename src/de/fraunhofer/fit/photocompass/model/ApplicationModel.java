package de.fraunhofer.fit.photocompass.model;

import android.os.DeadObjectException;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.util.OutputFormatter;

/**
 * This model stores the current values for distance and age limitations of the
 * displayed photos. The values are set by the controls in the
 * {@link de.fraunhofer.fit.photocompass.views.ControlsView}. This is an active
 * model, where Activities can register as callbacks in order to get updates
 * when the values change. This is a Singleton.
 */
public final class ApplicationModel {

	// minimum & maximum values
	private final float MIN_DISTANCE_LIMIT = 1000F; // in meters
	final float MAX_DISTANCE_LIMIT = 5 * 1000F; // in meters
	float MAX_MAX_DISTANCE = MAX_DISTANCE_LIMIT; // in meters
	private final long MIN_AGE_LIMIT = 60 * 60 * 1000L; // in milliseconds
	final long MAX_AGE_LIMIT = 54 * 7 * 24 * 60 * 60 * 1000L; // in milliseconds
	long MAX_MAX_AGE = MAX_AGE_LIMIT; // in milliseconds

	private static final ApplicationModel _instance = new ApplicationModel();

	// current settings
	public float minDistance = 0F; // The current minimum distance for photos to
	// be displayed. In meters.
	public float minDistanceRel = 0F; // The current minimum distance for photos
	// to be displayed. From 0..1.
	public String minDistanceStr = OutputFormatter.formatDistance(minDistance); // The
	// current
	// minimum
	// distance
	// for
	// photos
	// to
	// be
	// displayed.
	// As
	// a
	// formated
	// string
	// for
	// display.
	public float maxDistance = MAX_MAX_DISTANCE; // The current maximum distance
	// for photos to be
	// displayed. In meters.
	public float maxDistanceRel = 1F; // The current maximum distance for photos
	// to be displayed. From 0..1.
	public String maxDistanceStr = OutputFormatter.formatDistance(maxDistance); // The
	// current
	// maximum
	// distance
	// for
	// photos
	// to
	// be
	// displayed.
	// As
	// a
	// formated
	// string
	// for
	// display.
	public long minAge = 0L; // The current minimum age for photos to be
	// displayed. In milliseconds.
	public float minAgeRel = 0F; // The current minimum age for photos to be
	// displayed. From 0..1.
	public String minAgeStr = OutputFormatter.formatAge(minAge); // The current
	// minimum
	// age for
	// photos to
	// be
	// displayed.
	// As a
	// formated
	// string
	// for
	// display.
	public long maxAge = MAX_MAX_AGE; // The current maximum age for photos to
	// be displayed. In milliseconds.
	public float maxAgeRel = 1F; // The current maximum age for photos to be
	// displayed. From 0..1.
	public String maxAgeStr = OutputFormatter.formatAge(maxAge); // The current
	// maximum
	// age for
	// photos to
	// be
	// displayed.
	// As a
	// formated
	// string
	// for
	// display.

	private final RemoteCallbackList<IApplicationModelCallback> _remoteCallbacks = new RemoteCallbackList<IApplicationModelCallback>();

	/**
	 * Constructor. Private because Singleton. Use {@link #getInstance()}.
	 */
	private ApplicationModel() {
	}

	/**
	 * @return The instance of this Singleton model.
	 */
	public static ApplicationModel getInstance() {
		return _instance;
	}

	/**
	 * Register a callback object.
	 */
	public void registerCallback(final IApplicationModelCallback cb) {
		if (cb != null)
			_remoteCallbacks.register(cb);
	}

	/**
	 * Unregister a callback object.
	 */
	public void unregisterCallback(final IApplicationModelCallback cb) {
		if (cb != null)
			_remoteCallbacks.unregister(cb);
	}

	/**
	 * Set the maximum value for maximum distance. Call this from the
	 * {@link Photos} model when the photos are read of the device.
	 * 
	 * @param value
	 * @return <code>true</code> if {@link #MAX_MAX_DISTANCE} was changed, or
	 *         <code>false</code> if no change.
	 */
	public boolean setMaxMaxDistance(final float value) {
		// Log.d(PhotoCompassApplication.LOG_TAG,
		// "ApplicationModel: setMaxMaxDistance = "+value);
		final float oldValue = MAX_MAX_DISTANCE;
		MAX_MAX_DISTANCE = (value > MAX_DISTANCE_LIMIT) ? MAX_DISTANCE_LIMIT
				: (value < MIN_DISTANCE_LIMIT) ? MIN_DISTANCE_LIMIT : value;
		if (maxDistance != MAX_MAX_DISTANCE)
			setMaxDistance(MAX_MAX_DISTANCE);
		return (oldValue == MAX_MAX_DISTANCE) ? false : true;
	}

	/**
	 * Set the maximum value for maximum age. Call this from the {@link Photos}
	 * model when the photos are read of the device.
	 * 
	 * @param value
	 * @return <code>true</code> if {@link #MAX_MAX_DISTANCE} was changed, or
	 *         <code>false</code> if no change.
	 */
	public boolean setMaxMaxAge(final long value) {
		// Log.d(PhotoCompassApplication.LOG_TAG,
		// "ApplicationModel: setMaxMaxAge = "+value);
		final long oldValue = MAX_MAX_AGE;
		MAX_MAX_AGE = (value > MAX_AGE_LIMIT) ? MAX_AGE_LIMIT
				: (value < MIN_AGE_LIMIT) ? MIN_AGE_LIMIT : value;
		if (maxAge != MAX_MAX_AGE)
			setMaxAge(MAX_MAX_AGE);
		return (oldValue == MAX_MAX_AGE) ? false : true;
	}

	/**
	 * @param value
	 *            The new minimum distance for photos to be displayed. In
	 *            meters.
	 */
	public void setMinDistance(final float value) {

		// update values
		minDistance = value;
		minDistanceRel = absoluteToRelativeDistance(minDistance);
		minDistanceStr = OutputFormatter.formatDistance(minDistance);

		// Log.d(PhotoCompassApplication.LOG_TAG,
		// "ApplicationModel: setMaxDistance: minDist = "+_minDistance+", maxDist = "+_maxDistance);

		// broadcast change
		final int numCallbacks = _remoteCallbacks.beginBroadcast();
		for (int i = 0; i < numCallbacks; i++) {
			try {
				_remoteCallbacks.getBroadcastItem(i).onMinDistanceChange(
						minDistance, minDistanceRel);
			} catch (final DeadObjectException e) {
				// the RemoteCallbackList will take care of removing the dead
				// object
			} catch (final RemoteException e) {
				Log.d(PhotoCompassApplication.LOG_TAG,
						"ApplicationModel: broadcast to callback failed");
			}
		}
		_remoteCallbacks.finishBroadcast();
	}

	/**
	 * @param value
	 *            The new minimum distance for photos to be displayed. From
	 *            0..1.
	 */
	public void setRelativeMinDistance(final float relativeMinDistance) {
		setMinDistance(relativeMinDistance * MAX_MAX_DISTANCE);
	}

	/**
	 * @param value
	 *            The new maximum distance for photos to be displayed.
	 */
	public void setMaxDistance(final float value) {

		// update values
		maxDistance = value;
		maxDistanceRel = absoluteToRelativeDistance(maxDistance);
		maxDistanceStr = OutputFormatter.formatDistance(maxDistance);

		// Log.d(PhotoCompassApplication.LOG_TAG,
		// "ApplicationModel: setMaxDistance: minDist = "+_minDistance+", maxDist = "+_maxDistance);

		// broadcast change
		final int numCallbacks = _remoteCallbacks.beginBroadcast();
		for (int i = 0; i < numCallbacks; i++) {
			try {
				_remoteCallbacks.getBroadcastItem(i).onMaxDistanceChange(
						maxDistance, maxDistanceRel);
			} catch (final DeadObjectException e) {
				// the RemoteCallbackList will take care of removing the dead
				// object
			} catch (final RemoteException e) {
				Log.d(PhotoCompassApplication.LOG_TAG,
						"ApplicationModel: broadcast to callback failed");
			}
		}
		_remoteCallbacks.finishBroadcast();
	}

	/**
	 * Translates an absolute distance in meters to a relative distance (0..1).
	 * 
	 * @param absoluteDistance
	 *            Distance in meters.
	 * @return Distance in 0..1.
	 */
	public float absoluteToRelativeDistance(final float absoluteDistance) {
		return (MAX_MAX_DISTANCE == 0) ? 0F : absoluteDistance
				/ MAX_MAX_DISTANCE;
	}

	/**
	 * @param value
	 *            The new maximum distance for photos to be displayed. From
	 *            0..1.
	 */
	public void setRelativeMaxDistance(final float relativeMaxDistance) {
		setMaxDistance(relativeMaxDistance * MAX_MAX_DISTANCE);
	}

	/**
	 * @param value
	 *            The new minimum age for photos to be displayed. In
	 *            milliseconds.
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
				_remoteCallbacks.getBroadcastItem(i).onMinAgeChange(minAge,
						minAgeRel);
			} catch (final DeadObjectException e) {
				// the RemoteCallbackList will take care of removing the dead
				// object
			} catch (final RemoteException e) {
				Log.d(PhotoCompassApplication.LOG_TAG,
						"ApplicationModel: broadcast to callback failed");
			}
		}
		_remoteCallbacks.finishBroadcast();
	}

	/**
	 * @param value
	 *            The new minimum age for photos to be displayed. From 0..1.
	 */
	public void setRelativeMinAge(final float relativeMinAge) {
		// Log.d(PhotoCompassApplication.LOG_TAG,
		// "ApplicationModel: setRelativeMinAge = "+relativeMinAge);
		setMinAge(Math.round((double) relativeMinAge * MAX_MAX_AGE));
	}

	/**
	 * @param value
	 *            The new maximum age for photos to be displayed.
	 */
	public void setMaxAge(final long value) {
//		Log.d(PhotoCompassApplication.LOG_TAG, "ApplicationModel: setMaxAge = "
//				+ value + ", i.e. " + OutputFormatter.formatAge(value));

		// update values
		maxAge = value;
		maxAgeRel = absoluteToRelativeAge(maxAge);
		maxAgeStr = OutputFormatter.formatAge(maxAge);

		// broadcast change
		final int numCallbacks = _remoteCallbacks.beginBroadcast();
		for (int i = 0; i < numCallbacks; i++) {
			try {
				_remoteCallbacks.getBroadcastItem(i).onMaxAgeChange(maxAge,
						maxAgeRel);
			} catch (final DeadObjectException e) {
				// the RemoteCallbackList will take care of removing the dead
				// object
			} catch (final RemoteException e) {
				Log.d(PhotoCompassApplication.LOG_TAG,
						"ApplicationModel: broadcast to callback failed");
			}
		}
		_remoteCallbacks.finishBroadcast();
	}

	/**
	 * Translates an absolute age in milliseconds to a relative age (0..1).
	 * 
	 * @param absoluteAge
	 *            Age in milliseconds.
	 * @return Age in 0..1.
	 */
	public float absoluteToRelativeAge(final long absoluteAge) {
		return (MAX_MAX_AGE == 0) ? 0F : (float) absoluteAge
				/ (float) MAX_MAX_AGE;
	}

	/**
	 * @param value
	 *            The new maximum age for photos to be displayed. From 0..1.
	 */
	public void setRelativeMaxAge(final float relativeMaxAge) {
		setMaxAge(Math.round((double) relativeMaxAge * MAX_MAX_AGE));
	}
}
