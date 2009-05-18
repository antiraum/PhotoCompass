package de.fraunhofer.fit.photocompass.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Window;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.services.ILocationService;
import de.fraunhofer.fit.photocompass.services.ILocationServiceCallback;
import de.fraunhofer.fit.photocompass.services.IOrientationService;
import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;
import de.fraunhofer.fit.photocompass.services.LocationService;
import de.fraunhofer.fit.photocompass.services.OrientationService;

public class DummyMapActivity extends Activity {

	DummyMapActivity mapActivity;

    private ILocationService _locationService;
    private boolean _boundToLocationService;
    private IOrientationService _orientationService;
    private boolean _boundToOrientationService;

    private ServiceConnection _locationServiceConn = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: connected to location service");
	    	
	    	// generate service object
	    	_locationService = ILocationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	_locationService.registerCallback(_locationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to register to location service");
            }
	    }
	
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: disconnected from location service");
	    	_locationService = null;
	    }
    };
    
    private ILocationServiceCallback _locationServiceCallback = new ILocationServiceCallback.Stub() {
    	
        public void onLocationEvent(double latitude, double longitude, double altitude) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: received event from location service");
            
            // get location
            
            // update map view
	    	// TODO
        }
    };

    private ServiceConnection _orientationServiceConn = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: connected to orientation service");
	    	
	    	// generate service object
	    	_orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	_orientationService.registerCallback(_orientationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to register to orientation service");
            }
	    }
	
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: disconnected from orientation service");
	    	_orientationService = null;
	    }
    };
    
    private IOrientationServiceCallback _orientationServiceCallback = new IOrientationServiceCallback.Stub() {
		
		private float _yaw;
		private float _pitch;
		private float _roll;
    	
        public void onOrientationEvent(float yaw, float pitch, float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: received event from orientation service");
	    	
	    	if (yaw == _yaw && pitch == _pitch && roll == _roll) return; // values have not changed
	    	_yaw = yaw;
	    	_pitch = pitch;
	    	_roll = roll;
            
            // switch to activity based on orientation
        	int activity = PhotoCompassApplication.getActivityForRoll(_roll);
	    	if (activity == PhotoCompassApplication.FINDER_ACTIVITY) {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: switching to finder activity");
	    		startActivity(new Intent(mapActivity, FinderActivity.class));
	    	}
            
            // update map view
	    	// TODO
        }
    };
    
    public DummyMapActivity() {
    	super();
    	mapActivity = this;
        _locationService = null;
        _boundToLocationService = false;
        _orientationService = null;
        _boundToOrientationService = false;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // setup view
	    setContentView(R.layout.dummymap_layout);
	}
    
    /**
     * Called before the activity becomes visible.
     */
    @Override
    public void onStart() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: onStart");
    	super.onStart();
    	
        // connect to location service
    	Intent locationServiceIntent = new Intent(this, LocationService.class);
    	_boundToLocationService = bindService(locationServiceIntent, _locationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToLocationService) Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to connect to location service");
    	
        // connect to orientation service
    	Intent orientationServiceIntent = new Intent(this, OrientationService.class);
    	_boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToOrientationService) Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to connect to orientation service");
    }
    
    /**
     * Called when the activity is no longer visible.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: onStop");
    	
    	if (_boundToLocationService) {
	    	
	    	// unregister from location service
	    	if (_locationService != null) {
	    		try {
	    			_locationService.unregisterCallback(_locationServiceCallback);
	            } catch (DeadObjectException e) {
	            	// the service has crashed
	    		} catch (RemoteException e) {
	    			Log.w(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to unregister from location service");
	    		}
	    	}

	        // disconnect from location service
	        unbindService(_locationServiceConn);
	        _boundToLocationService = false;
    	}
    	
    	if (_boundToOrientationService) {
	    	
	    	// unregister from orientation service
	    	if (_orientationService != null) {
	    		try {
	    			_orientationService.unregisterCallback(_orientationServiceCallback);
	            } catch (DeadObjectException e) {
	            	// the service has crashed
	    		} catch (RemoteException e) {
	    			Log.w(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to unregister from orientation service");
	    		}
	    	}

	        // disconnect from orientation service
	        unbindService(_orientationServiceConn);
	        _boundToOrientationService = false;
    	}
        
        super.onStop();
    }
}
