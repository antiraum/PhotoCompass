package de.fraunhofer.fit.photocompass.activities;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.IApplicationModelCallback;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.model.data.Photo;
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
    
	private double _currentLat;
	private double _currentLng;
	private float _currentYaw;
	
    private PhotosView _photosView;

    private ILocationService _locationService;
    private boolean _boundToLocationService;
    private IOrientationService _orientationService;
    private boolean _boundToOrientationService;

    private ApplicationModel _applicationModel;

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
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: received event from location service");
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
            
	    	// update variables
	    	_currentLat = latitude;
	    	_currentLng = longitude;
            
            // update photo view
	    	_updatePhotoView();
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
		
		private float _yaw;
		private float _roll;
    	
        public void onOrientationEvent(float yaw, float pitch, float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: received event from orientation service");
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here

	    	// roll value has changed
        	// TODO make this activity represent changing pitch values
	    	if (roll != _roll) {
		    	_roll = roll;
            
	            // switch to activity based on orientation
	        	int activity = PhotoCompassApplication.getActivityForRoll(_roll);
		    	if (activity == PhotoCompassApplication.MAP_ACTIVITY) {
		    		Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: switching to map activity");
		    		if (PhotoCompassApplication.TARGET_PLATFORM == 3) {
		    			startActivity(new Intent(finderActivity, PhotoMapActivity.class));
		    		} else {
		    			startActivity(new Intent(finderActivity, DummyMapActivity.class));
		    		}
			        finish(); // close this activity
		    	}
	    	}

	    	// yaw value has changed
        	// TODO make this activity represent changing pitch values
	    	int yawTolerance = 3; // reduces the number of update, cause the performance is not so great up to now / TODO make this work without
	    	if (yaw < _yaw - yawTolerance || yaw > _yaw + yawTolerance) {
//	    	if (yaw != _yaw) {
		    	_yaw = yaw;
	    		
		    	// update variables
		    	_currentYaw = _yaw;
	            
	            // update photo view
		    	_updatePhotoView();
	    	}
        }
    };

	private IApplicationModelCallback _applicationModelCallback = new IApplicationModelCallback.Stub() {
	
		public void onApplicationModelChange() {
			Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: received event from application model");
		    
		    // update photo view
			_updatePhotoView();
		}
	};
    
    public FinderActivity() {
    	super();
    	finderActivity = this;
    	
    	// initialize service variables
        _locationService = null;
        _boundToLocationService = false;
        _orientationService = null;
        _boundToOrientationService = false;
    	
        // initialize application model and register as callback
    	_applicationModel = ApplicationModel.getInstance();
    	_applicationModel.registerCallback(_applicationModelCallback);
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
        Display display = getWindowManager().getDefaultDisplay();
        int statusbarHeight = 25; // FIXME no hardcoded values
        _photosView = new PhotosView(this, display.getWidth(), display.getHeight() - statusbarHeight);
        ControlsView controlsView = new ControlsView(this);

        // setup views
        setContentView(finderView);
        addContentView(_photosView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//        setContentView(_photosView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
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
    
    /**
     * Updates the photo view based on the current location and orientation parameters.
     */
    private void _updatePhotoView() {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: _updatePhotoView");
    	
    	// dummy values - override actual location with B-IT cause our dummy photos are next to B-IT
    	// TODO make this proper
    	_currentLat = Location.convert("50:43:12.59"); // B-IT
    	_currentLng = Location.convert("7:7:16.2"); // B-IT
    	
    	List<Photo> photos = Photos.getInstance().getPhotos(_currentLat, _currentLng, _currentYaw,
    														ApplicationModel.getInstance().getMaxDistance(),
    												 		ApplicationModel.getInstance().getMinAge(),
    												 		ApplicationModel.getInstance().getMaxAge());
    	_photosView.setPhotos(photos, _currentYaw);
    }
}
