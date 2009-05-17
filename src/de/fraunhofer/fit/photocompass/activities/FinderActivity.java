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
import android.view.ViewGroup.LayoutParams;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.services.ILocationService;
import de.fraunhofer.fit.photocompass.services.ILocationServiceCallback;
import de.fraunhofer.fit.photocompass.services.IOrientationService;
import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;
import de.fraunhofer.fit.photocompass.services.LocationService;
import de.fraunhofer.fit.photocompass.services.OrientationService;
import de.fraunhofer.fit.photocompass.views.ControlsView;
import de.fraunhofer.fit.photocompass.views.FinderView;
import de.fraunhofer.fit.photocompass.views.PhotosView;

public class FinderActivity extends Activity {

	FinderActivity finderActivity;
    
    private PhotosView _photosView;

    private ILocationService _locationService;
    private boolean _boundToLocationService;
    private IOrientationService _orientationService;
    private boolean _boundToOrientationService;

    private ServiceConnection _locationServiceConn = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: connected to location service");
	    	
	    	// generate service object
	    	_locationService = ILocationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	_locationService.registerCallback(_locationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: failed to register to location service");
            }
	    }
	
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: disconnected from location service");
	    	_locationService = null;
	    }
    };
    
    private ILocationServiceCallback _locationServiceCallback = new ILocationServiceCallback.Stub() {
    	
        public void onLocationEvent(double latitude, double longitude, double altitude) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: received event from location service");
            
            // get location
            
            // update photo view
	    	// TODO
        }
    };

    private ServiceConnection _orientationServiceConn = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: connected to orientation service");
	    	
	    	// generate service object
	    	_orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	_orientationService.registerCallback(_orientationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: failed to register to orientation service");
            }
	    }
	
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: disconnected from orientation service");
	    	_orientationService = null;
	    }
    };
    
    private IOrientationServiceCallback _orientationServiceCallback = new IOrientationServiceCallback.Stub() {
		
		private float _azimuth;
		private float _pitch;
		private float _roll;
    	
        public void onOrientationEvent(float azimuth, float pitch, float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: received event from orientation service");
	    	
	    	if (azimuth == _azimuth && pitch == _pitch && roll == _roll) return; // values have not changed
	    	_azimuth = azimuth;
	    	_pitch = pitch;
	    	_roll = roll;
            
            // switch to activity based on orientation
        	int activity = PhotoCompassApplication.getActivityForRoll(_roll);
	    	if (activity == PhotoCompassApplication.MAP_ACTIVITY) {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: switching to map activity");
//	    		startActivity(new Intent(finderActivity, PhotoMapActivity.class));
	    		startActivity(new Intent(finderActivity, DummyMapActivity.class));
	    	}
            
            // update photo views
	    	_photosView.setPhotos(Photos.getInstance().getPhotos());
//	    	// TODO
//        	photosScrollView.setMValues(values);
//            photosScrollView.invalidate();
        }
    };
    
    public FinderActivity() {
    	super();
    	finderActivity = this;
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
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // initialize views
        FinderView finderView = new FinderView(this);
        _photosView = new PhotosView(this);
        ControlsView controlsView = new ControlsView(this);

        // setup views
        setContentView(finderView);
        addContentView(_photosView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        addContentView(controlsView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }
    
    /**
     * Called before the activity becomes visible.
     */
    @Override
    public void onStart() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onStart");
    	super.onStart();
    	
        // connect to location service
    	Intent locationServiceIntent = new Intent(this, LocationService.class);
    	_boundToLocationService = bindService(locationServiceIntent, _locationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToLocationService) Log.e(PhotoCompassApplication.LOG_TAG, "failed to connect to location service");
    	
        // connect to orientation service
    	Intent orientationServiceIntent = new Intent(this, OrientationService.class);
    	_boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToOrientationService) Log.e(PhotoCompassApplication.LOG_TAG, "failed to connect to orientation service");
    }
    
    /**
     * Called when the activity is no longer visible.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onStop");
    	
    	if (_boundToLocationService) {
	    	
	    	// unregister from location service
	    	if (_locationService != null) {
	    		try {
	    			_locationService.unregisterCallback(_locationServiceCallback);
	            } catch (DeadObjectException e) {
	            	// the service has crashed
	    		} catch (RemoteException e) {
	    			Log.w(PhotoCompassApplication.LOG_TAG, "FinderActivity: failed to unregister from location service");
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
	    			Log.w(PhotoCompassApplication.LOG_TAG, "FinderActivity: failed to unregister from orientation service");
	    		}
	    	}

	        // disconnect from orientation service
	        unbindService(_orientationServiceConn);
	        _boundToOrientationService = false;
    	}
        
        super.onStop();
    }
}
