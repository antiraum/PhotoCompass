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
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ViewGroup.LayoutParams;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.IApplicationModelCallback;
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

/**
 * This class is the Activity component for the camera view screen (phone held vertically) of the application.
 */
public class FinderActivity extends Activity {

    private static final int STATUSBAR_HEIGHT = 25; // FIXME no hardcoded values
    private static final int BOTTOM_CONTROLS_HEIGHT = 35;

	FinderActivity finderActivity;
    
	double currentLat; // package scoped for faster access by inner classes
	double currentLng; // package scoped for faster access by inner classes
	double currentAlt; // package scoped for faster access by inner classes
	float currentYaw; // package scoped for faster access by inner classes
	
    PhotosView photosView; // package scoped for faster access by inner classes
	private long _lastPhotoViewUpdate;
	private static final int PHOTO_VIEW_UPDATE_IVAL = 300; // in milliseconds

    ILocationService locationService; // package scoped for faster access by inner classes
    private boolean _boundToLocationService;
    IOrientationService orientationService; // package scoped for faster access by inner classes
    private boolean _boundToOrientationService;

    private ApplicationModel _applicationModel;

    /**
     * {@link GestureDetector} with a {@link SimpleOnGestureListener} that detects the gestures used for interacting
     * with the displayed photos. Calls the {@link #photosView} to deal with the events.
     */
	private GestureDetector _gestureDetector = new GestureDetector(
	    new GestureDetector.SimpleOnGestureListener() {
	    	
	    	/**
	    	 * Gets called when a fling/swipe gesture is detected.
	    	 */
	        @Override
	        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
	        	
	        	// pass on to photos view
	        	return photosView.onFling(event1.getRawX(), event1.getRawY() - STATUSBAR_HEIGHT,
	        							   event2.getRawX(), event2.getRawY() - STATUSBAR_HEIGHT);
	        }
	        
	        /**
	         * Gets called when a tap gesture is completed.
	         */
	        @Override
	        public boolean onSingleTapUp(MotionEvent event) {
	        	
	        	// pass on to photos view
	        	return photosView.onSingleTapUp(event.getRawX(), event.getRawY() - STATUSBAR_HEIGHT);
	        }
	    });

    /**
     * Connection object for the connection with the {@link LocationService}.
     */
    private final ServiceConnection _locationServiceConn = new ServiceConnection() {

    	/**
    	 * Gets called when the service connection is established.
    	 * Creates the {@link #locationService} object from the service interface and
    	 * registers the {@link #locationServiceCallback}.
    	 */
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: connected to location service");
	    	
	    	// generate service object
	    	locationService = ILocationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	locationService.registerCallback(locationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: failed to register to location service");
            }
	    }

    	/**
    	 * Gets called when the service connection is closed down.
    	 * Frees {@link #locationService}.
    	 */
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: disconnected from location service");
	    	locationService = null;
	    }
    };

    /**
     * Callback object for the {@link LocationService}.
     * Gets registered and unregistered at the {@link #locationService} object.
     * Package scoped for faster access by inner classes.
     */
    final ILocationServiceCallback locationServiceCallback = new ILocationServiceCallback.Stub() {

		/**
		 * Gets called when new data is provided by the {@link LocationService}.
		 * Stores the new location and initiates an update of the map view. 
		 */
        public void onLocationEvent(double lat, double lng, double alt) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onLocationEvent: lat = "+lat+", lng = "+lng+", alt = "+alt);
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
            
        	boolean latChanged = (lat == currentLat) ? false : true;
        	boolean lngChanged = (lng == currentLng) ? false : true;
        	boolean altChanged = (alt == currentAlt) ? false : true;
	    	
	    	// update variables
	    	currentLat = lat;
	    	currentLng = lng;
	    	currentAlt = alt;
        	
            // update photo view
	    	updatePhotoView(latChanged, lngChanged, altChanged, false, false, false);
        }
    };

    /**
     * Connection object for the connection with the {@link OrientationService}.
     */
    private final ServiceConnection _orientationServiceConn = new ServiceConnection() {

    	/**
    	 * Gets called when the service connection is established.
    	 * Creates the {@link #orientationService} object from the service interface and
    	 * registers the {@link #orientationServiceCallback}.
    	 */
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: connected to orientation service");
	    	
	    	// generate service object
	    	orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	orientationService.registerCallback(orientationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: failed to register to orientation service");
            }
	    }

    	/**
    	 * Gets called when the service connection is closed down.
    	 * Frees {@link #orientationService}.
    	 */
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: disconnected from orientation service");
	    	orientationService = null;
	    }
    };

    /**
     * Callback object for the {@link OrientationService}.
     * Gets registered and unregistered at the {@link #orientationService} object.
     * Package scoped for faster access by inner classes.
     */
    final IOrientationServiceCallback orientationServiceCallback = new IOrientationServiceCallback.Stub() {
		
		private float _roll;

		/**
		 * Gets called when new data is provided by the {@link OrientationService}.
		 * Initiates switch to {@link PhotoMapActivity} when the phone is held horizontally.
		 * Also updates the {@link #photosView} when the yaw value changed. 
		 */
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
	    	if (currentYaw != yaw) {
	    		
		    	// update variable
	    		currentYaw = yaw;

	            // update photo view
		    	updatePhotoView(false, false, false, true, false, false);
	    	}
        }
    };

    /**
     * Callback object for the {@link ApplicationModel}.
     * Gets registered and unregistered at the {@link #_applicationModel} object.
     */
	private final IApplicationModelCallback _applicationModelCallback = new IApplicationModelCallback.Stub() {

		/**
		 * Gets called when variables in the {@link ApplicationModel} change.
		 * Initiates a update of {@link #photosView}. 
		 */
		public void onApplicationModelChange() {
			Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: received event from application model");

            // update photo view
	    	updatePhotoView(false, false, false, false, true, true);
		}
	};
    
    /**
     * Constructor.
     * Initializes the state variables.
     */
    public FinderActivity() {
    	super();
    	finderActivity = this;
    	
    	// initialize service variables
        locationService = null;
        _boundToLocationService = false;
        orientationService = null;
        _boundToOrientationService = false;
    	
        // initialize application model and register as callback
    	_applicationModel = ApplicationModel.getInstance();
    	_applicationModel.registerCallback(_applicationModelCallback);
    }

    /**
     * Gets called when a touch events occurs.
     * Passes the event on to the {@link #_gestureDetector}.
     */
    public boolean onTouchEvent(MotionEvent event) {
    	if (photosView == null) return false; // photos view not yet created
        return _gestureDetector.onTouchEvent(event);
    }
    
    /**
     * Called when the activity is first created.
     * Initializes the views.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // initialize views
        FinderView finderView = new FinderView(this);
        Display display = getWindowManager().getDefaultDisplay();
        photosView = new PhotosView(this, display.getWidth(), display.getHeight() - STATUSBAR_HEIGHT - BOTTOM_CONTROLS_HEIGHT);
        ControlsView controlsView = new ControlsView(this);

        // setup views
        setContentView(finderView);
        addContentView(photosView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        addContentView(controlsView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }
    
    /**
     * Called before the activity becomes visible.
     * Connects to the {@link LocationService} and the {@link OrientationService}.
     * Initiates a update of the {@link Photos} model.
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
    	
    	// let photos model check if the available photos have changed
    	Photos.getInstance().updatePhotos();
    }
    
    /**
     * Called when the activity is no longer visible.
     * Unregisters the callbacks from the services and then disconnects from the services.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onStop");
    	
    	if (_boundToLocationService) {
	    	
	    	// unregister from location service
	    	if (locationService != null) {
	    		try {
	    			locationService.unregisterCallback(locationServiceCallback);
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
	    	if (orientationService != null) {
	    		try {
	    			orientationService.unregisterCallback(orientationServiceCallback);
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
     * Updates the photo view based on the current location and phone orientation as well as the settings in the {@link ApplicationModel}.
     * Package scoped for faster access by inner classes.
     */
    void updatePhotoView(boolean latChanged, boolean lngChanged, boolean altChanged, boolean yawChanged, boolean modelChanged,
    				     boolean forceRedraw) {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: updatePhotoView");
    	
    	// redraw the view only if either forced or last redraw older than PHOTO_VIEW_UPDATE_IVAL
		if (forceRedraw || SystemClock.uptimeMillis() - _lastPhotoViewUpdate > PHOTO_VIEW_UPDATE_IVAL) {
			forceRedraw = true;
	    	_lastPhotoViewUpdate = SystemClock.uptimeMillis();
		}
    	
    	if (latChanged || lngChanged || modelChanged) {
    		
    		// update photos
        	Photos photosModel = Photos.getInstance();
    		ApplicationModel appModel = ApplicationModel.getInstance();
    		photosModel.updatePhotoProperties(currentLat, currentLng, currentAlt);
    		photosView.addPhotos(photosModel.getNewlyVisiblePhotos(photosView.getPhotos(),
    															   appModel.getMaxDistance(), appModel.getMinAge(), appModel.getMaxAge()),
    															   forceRedraw);
    		photosView.removePhotos(photosModel.getNoLongerVisiblePhotos(photosView.getPhotos(),
																 		 appModel.getMaxDistance(), appModel.getMinAge(), appModel.getMaxAge()));
    	}
    	
    	if (latChanged || lngChanged) {
    		
    		// update sizes of the photos
    		_updateCurrentPhotosProperties();
    		photosView.updateSizes(forceRedraw);
    	}
    	
    	if (altChanged) {
    		
    		// update y positions of the photos
    		_updateCurrentPhotosProperties();
    		photosView.updateYPositions(forceRedraw);
    	}
    	
    	if (yawChanged) {
    		
    		// update x positions of the photos
    		photosView.updateXPositions(currentYaw, forceRedraw);
    	}
    }
    
    /**
     * Updates distance, direction, and altitude offset of the photos currently used by the photos view.
     */
    private void _updateCurrentPhotosProperties() {
    	for (int resourceId : photosView.getPhotos())
    		Photos.getInstance().getPhoto(resourceId).updateDistanceDirectionAndAltitudeOffset(currentLat, currentLng, currentAlt);
    }
}
