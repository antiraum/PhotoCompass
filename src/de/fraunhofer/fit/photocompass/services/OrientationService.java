package de.fraunhofer.fit.photocompass.services;

import org.openintents.hardware.SensorManagerSimulator;
import org.openintents.provider.Hardware;

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
		
		private float _yaw, _pitch, _roll;

		public void onSensorChanged(int sensor, float[] values) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: onSensorChanged");
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "OrientationService: yaw = "+values[0]+", pitch = "+values[1]+", roll = "+values[2]);
			
			_yaw = values[0];
			// the values are exchanged on the G1, so we have to switch between these code blocks
	        /* begin sensor simulator code */
			_pitch = values[1];
			_roll = values[2];
	        /* end sensor simulator code */
	        /* begin real sensor code */
//			_pitch = values[2];
//			_roll = values[1];
	        /* end real sensor code */
			
	        // broadcast the new location to all registered callbacks
	        final int numCallbacks = remoteCallbacks.beginBroadcast();
	        for (int i = 0; i < numCallbacks; i++) {
	            try {
	                remoteCallbacks.getBroadcastItem(i).onOrientationEvent(_yaw, _pitch, _roll);
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
        Hardware.mContentResolver = getContentResolver(); 
        _sensorManager = (SensorManager) new SensorManagerSimulator((SensorManager) getSystemService(SENSOR_SERVICE));
		SensorManagerSimulator.connectSimulator(); 
        /* end sensor simulator code */
        /* begin real sensor code */
//        _sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        /* end real sensor code */
    	
    	// TODO test for orientation sensor and notify the user that he cannot use the application without it
    	
    	// start listening to sensors
        // TODO we should aim to be able to handle events at SensorManager.SENSOR_DELAY_NORMAL rate
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