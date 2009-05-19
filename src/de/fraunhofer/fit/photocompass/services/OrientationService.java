package de.fraunhofer.fit.photocompass.services;

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

public class OrientationService extends Service {

    /**
     * List of callbacks that have been registered with the service.
     * This is package scoped (instead of private) so that it can be accessed more efficiently from inner classes.
     */
    final RemoteCallbackList<IOrientationServiceCallback> remoteCallbacks = new RemoteCallbackList<IOrientationServiceCallback>();
    
    private final IOrientationService.Stub _binder = new IOrientationService.Stub() {
        public void registerCallback(IOrientationServiceCallback cb) {
            if (cb != null) remoteCallbacks.register(cb);
        }
        public void unregisterCallback(IOrientationServiceCallback cb) {
            if (cb != null) remoteCallbacks.unregister(cb);
        }
    };

	private SensorManager _sensorManager;
	private SensorListener _sensorListener = new SensorListener() {

		public void onSensorChanged(int sensor, float[] values) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onSensorChanged");
//	    	Log.i(PhotoCompassApplication.LOG_TAG, "OrientationService: yaw = "+_yaw+", pitch = "+_pitch+", roll = "+_roll);
			
	        // broadcast the new location to all registered callbacks
	        final int numCallbacks = remoteCallbacks.beginBroadcast();
	        for (int i = 0; i < numCallbacks; i++) {
	            try {
	                remoteCallbacks.getBroadcastItem(i).onOrientationEvent(values[0], values[1], values[2]);
	            } catch (DeadObjectException e) {
	                // the RemoteCallbackList will take care of removing the dead object
	            } catch (RemoteException e) {
	    	    	Log.e(PhotoCompassApplication.LOG_TAG, "OrientationService: broadcast to callback failed");
                }
	        }
	        remoteCallbacks.finishBroadcast();
		}
		
		public void onAccuracyChanged(int sensor, int accuracy) {
		}
    };
	
    @Override
    public void onCreate() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onCreate");
        super.onCreate();
        
        // As there seems to be no way to detect if we are running on an emulator, we have to manually switch between the following
        // code blocks. If someone finds a way to do this, please add it. 

        // initialize location manager
        /* begin sensor simulator code */
//        Hardware.mContentResolver = getContentResolver(); 
//        _sensorManager = (SensorManager) new SensorManagerSimulator((SensorManager) getSystemService(SENSOR_SERVICE));
//		SensorManagerSimulator.connectSimulator(); 
        /* end sensor simulator code */
        /* begin real sensor code */
        _sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        /* end real sensor code */
    	
    	// TODO test for orientation sensor and notify the user that he cannot use the application without it
    	
    	// start listening to sensors
    	_sensorManager.registerListener(_sensorListener, SensorManager.SENSOR_ORIENTATION, SensorManager.SENSOR_DELAY_UI);
    }

	@Override
	public IBinder onBind(Intent intent) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onBind");
		return _binder;
	}

    @Override
    public void onDestroy() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onDestroy");
    	
    	// stop listening
    	_sensorManager.unregisterListener(_sensorListener);
    	_sensorManager = null;
    	
        super.onDestroy();
    }
}