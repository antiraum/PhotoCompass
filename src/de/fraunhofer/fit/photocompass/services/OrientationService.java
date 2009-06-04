package de.fraunhofer.fit.photocompass.services;

import org.openintents.hardware.SensorManagerSimulator;
import org.openintents.provider.Hardware;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

/**
 * This class is a Service component that reads data from the orientation sensor.
 * Activities should bind to this Service when they become visible and disconnect when they are no longer visible, so that this
 * Service only runs when needed. After the connection to the service is established activities can register as callbacks to get
 * notified when the location changes.
 */
public final class OrientationService extends Service {

    /**
     * List of callbacks that have been registered with the service.
     * Package scoped for faster access by inner classes.
     */
    final RemoteCallbackList<IOrientationServiceCallback> remoteCallbacks = new RemoteCallbackList<IOrientationServiceCallback>();

    /**
     * Implementation of the interface to this service.
     * Is provided to activities when they connect ({@see #onBind(Intent)}).
     */
    private final IOrientationService.Stub _binder = new IOrientationService.Stub() {
        public void registerCallback(final IOrientationServiceCallback cb) {
            if (cb != null) remoteCallbacks.register(cb);
        }
        public void unregisterCallback(final IOrientationServiceCallback cb) {
            if (cb != null) remoteCallbacks.unregister(cb);
        }
    };

    /**
     * {@link SensorManager}.
     */
	private SensorManager _sensorManager;

	/**
	 * {@link SensorListener} for the {@link #_sensorManager}.
	 */
	private final SensorListener _sensorListener = new SensorListener() {

		/**
		 * Called when sensor values have changed.
		 * Broadcasts the new sensor data to all registered callbacks.
		 */
		public void onSensorChanged(final int sensor, final float[] values) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onSensorChanged: "+
//	    										   "yaw = "+values[0]+", pitch = "+values[1]+", roll = "+values[2]);
			
			float yaw = values[0], pitch, roll;
			// the values are exchanged on the G1, so we have to switch them
			if (PhotoCompassApplication.RUNNING_ON_EMULATOR) {
				pitch = values[1];
				roll = values[2];
			} else {
				pitch = values[2];
				roll = values[1];
			}
		
	        // broadcast the new location to all registered callbacks
	        final int numCallbacks = remoteCallbacks.beginBroadcast();
	        for (int i = 0; i < numCallbacks; i++) {
	            try {
	                remoteCallbacks.getBroadcastItem(i).onOrientationEvent(yaw, pitch, roll);
	            } catch (final DeadObjectException e) {
	                // the RemoteCallbackList will take care of removing the dead object
	            } catch (final RemoteException e) {
	    	    	Log.e(PhotoCompassApplication.LOG_TAG, "OrientationService: broadcast to callback failed");
                }
	        }
	        remoteCallbacks.finishBroadcast();
		}
		
		/**
		 * Called when the accuracy of a sensor has changed.
		 */
		public void onAccuracyChanged(final int sensor, final int accuracy) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onAccuracyChanged: sensor = "+sensor+", accuracy = "+accuracy);
		}
    };

    /**
     * Called by the system when the service is first created.
     * Initializes the {@link #_sensorManager}, checks if an orientation sensor is available, and starts listening to it.
     */
    @Override
    public void onCreate() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onCreate");
        super.onCreate();

        // initialize location manager
		if (PhotoCompassApplication.RUNNING_ON_EMULATOR) {
			// running on emulator with sensor simulator
	        Hardware.mContentResolver = getContentResolver(); 
	        _sensorManager = (SensorManager) new SensorManagerSimulator((SensorManager) getSystemService(SENSOR_SERVICE));
			SensorManagerSimulator.connectSimulator(); 
		} else {
			// running on phone
			_sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
		}
    	
		// check if there is no orientation sensor
		if (_sensorManager.getSensorList(SensorManager.SENSOR_ORIENTATION).size() == 0) {
        	Log.e(PhotoCompassApplication.LOG_TAG, "OrientationService: no orientation sensor found");
        	// TODO notify the user and tell him that he cannot use the application
    		return;
    	}
    	
    	// start listening to sensors
    	_sensorManager.registerListener(_sensorListener, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Called when an activity connects to the service.
     * 
     * @return The {@field #_binder} interface to the service.
     */
	@Override
	public IBinder onBind(final Intent intent) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onBind");
		return _binder;
	}

	/**
	 * Called by the system to notify a Service that it is no longer used and is being removed.
	 * Shuts down the service.
	 */
    @Override
    public void onDestroy() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onDestroy");
    	
    	// stop listening
    	_sensorManager.unregisterListener(_sensorListener);
    	_sensorManager = null;
    	
        super.onDestroy();
    }
}