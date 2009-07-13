package de.fraunhofer.fit.photocompass.services;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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
 * notified when the orientation changes.
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
	private final SensorEventListener _sensorListener = new SensorEventListener() {

        private final float _rad2deg = (float) (180.0F / Math.PI);
	    private final float[] _gravityData = new float[3];
	    private final float[] _magneticData = new float[3];
	    private final float[] _rotationMatrix = new float[16];
	    private final float[] _orientation = new float[3];
//	    private float _lastYaw = 0;
//	    private float _lastPitch = 0;
//	    private float _lastRoll = 0;
	    private float _yaw = 0;
	    private float _pitch = 0;
	    private float _roll = 0;

		/**
		 * Called when sensor values have changed.
		 * Broadcasts the new sensor data to all registered callbacks.
		 */
		public void onSensorChanged(final SensorEvent event) {
	    	
	    	// check if there are callbacks registered
		    final int numCallbacks = remoteCallbacks.beginBroadcast();
	    	if (numCallbacks == 0) {
	    		remoteCallbacks.finishBroadcast();
	    		return;
	    	}
			
	    	// read sensor event data
	        final int type = event.sensor.getType();
	        float[] data;
	        switch (type) {
	        	case Sensor.TYPE_ACCELEROMETER:
	        		data = _gravityData;
	        		break;
	        	case Sensor.TYPE_MAGNETIC_FIELD:
	        		data = _magneticData;
	        		break;
	        	default:
		            // we should not be here.
		            return;
	        }
	        for (int i = 0; i < 3; i++) data[i] = event.values[i];

	        // get orientation
	        SensorManager.getRotationMatrix(_rotationMatrix, null, _gravityData, _magneticData);
	        SensorManager.getOrientation(_rotationMatrix, _orientation);
	        
	        // set parameters
	        _yaw = (_orientation[0] * _rad2deg + 90) % 360; // fix for 90 degree wrong values
	        _pitch = _orientation[1] * _rad2deg;
	        _roll = _orientation[2] * _rad2deg;
	        
//	        if (Math.abs(_lastYaw - _yaw) > 5 || Math.abs(_lastPitch - _pitch) > 5 || Math.abs(_lastRoll - _roll) > 5)
//		    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onSensorChanged: "+
//						   							   "yaw = "+_yaw+", pitch = "+_pitch+", roll = "+_roll);
//	        _lastYaw = _yaw;
//	        _lastPitch = _pitch;
//	        _lastRoll = _roll;
		
	        // broadcast the new orientation to all registered callbacks
	        for (int i = 0; i < numCallbacks; i++) {
	            try {
	                remoteCallbacks.getBroadcastItem(i).onOrientationEvent(_yaw, _pitch, _roll);
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
	    public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
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

        // initialize sensor manager
		_sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
		
		// TODO check if required sensors are available
    	
    	// start listening to sensors
        _sensorManager.registerListener(_sensorListener,
        								_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        								SensorManager.SENSOR_DELAY_GAME);
        _sensorManager.registerListener(_sensorListener,
        								_sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
        								SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Called when an activity connects to the service.
     * 
     * @return The {@link #_binder} interface to the service.
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