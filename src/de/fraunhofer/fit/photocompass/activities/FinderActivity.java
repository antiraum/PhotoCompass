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

/**
 * This class is the Activity component for the camera view screen (phone held vertically) of the application.
 */
public final class FinderActivity extends Activity {

    private static final int STATUSBAR_HEIGHT = 25; // FIXME no hard-coded values
    private static final int BOTTOM_CONTROLS_HEIGHT = 35;

	FinderActivity finderActivity; // package scoped for faster access by inner classes
    
	double currentLat = 0; // package scoped for faster access by inner classes
	double currentLng = 0; // package scoped for faster access by inner classes
	double currentAlt = 0; // package scoped for faster access by inner classes
	float currentYaw = 0; // package scoped for faster access by inner classes
	
    PhotosView photosView; // package scoped for faster access by inner classes
	private long _lastPhotoViewUpdate;
	private static final int PHOTO_VIEW_UPDATE_IVAL = 250; // in milliseconds

    ILocationService locationService; // package scoped for faster access by inner classes
    private boolean _boundToLocationService;
    IOrientationService orientationService; // package scoped for faster access by inner classes
    private boolean _boundToOrientationService;

    private Photos _photosModel;
    private ApplicationModel _appModel;

    /**
     * {@link GestureDetector} with a {@link SimpleOnGestureListener} that detects the gestures used for interacting
     * with the displayed photos. Calls the {@link #photosView} to deal with the events.
     */
	private final GestureDetector _gestureDetector = new GestureDetector(
	    new GestureDetector.SimpleOnGestureListener() {
	    	
	    	/**
	    	 * Gets called when a fling/swipe gesture is detected.
	    	 */
	        @Override
	        public boolean onFling(final MotionEvent event1, final MotionEvent event2, final float velocityX, final float velocityY) {
	        	
	        	// pass on to photos view
	        	return photosView.onFling(event1.getRawX(), event1.getRawY() - STATUSBAR_HEIGHT,
	        							   event2.getRawX(), event2.getRawY() - STATUSBAR_HEIGHT);
	        }
	        
	        /**
	         * Gets called when a tap gesture is completed.
	         */
	        @Override
	        public boolean onSingleTapUp(final MotionEvent event) {
	        	
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
	    public void onServiceConnected(final ComponentName className, final IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: connected to location service");
	    	
	    	// generate service object
	    	locationService = ILocationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	locationService.registerCallback(locationServiceCallback);
            } catch (final DeadObjectException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: location service has crashed");
            } catch (final RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: failed to register to location service");
            }
	    }

    	/**
    	 * Gets called when the service connection is closed down.
    	 * Frees {@link #locationService}.
    	 */
	    public void onServiceDisconnected(final ComponentName name) {
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
        public void onLocationEvent(final double lat, final double lng, final boolean hasAlt, final double alt) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onLocationEvent: lat = "+lat+", lng = "+lng+", alt = "+alt);
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
            
        	final boolean latChanged = (lat == currentLat) ? false : true;
        	final boolean lngChanged = (lng == currentLng) ? false : true;
        	final boolean altChanged = (! hasAlt || alt == currentAlt) ? false : true;
	    	
	    	// update variables
	    	currentLat = lat;
	    	currentLng = lng;
	    	if (hasAlt) currentAlt = alt;
        	
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
	    public void onServiceConnected(final ComponentName className, final IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: connected to orientation service");
	    	
	    	// generate service object
	    	orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	orientationService.registerCallback(orientationServiceCallback);
            } catch (final DeadObjectException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: orientation service has crashed");
            } catch (final RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: failed to register to orientation service");
            }
	    }

    	/**
    	 * Gets called when the service connection is closed down.
    	 * Frees {@link #orientationService}.
    	 */
	    public void onServiceDisconnected(final ComponentName name) {
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
        public void onOrientationEvent(final float yaw, final float pitch, final float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: received event from orientation service");
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here

	    	// roll value has changed
        	// TODO make this activity represent changing pitch values
	    	if (roll != _roll) {
		    	_roll = roll;
            
	            // switch to activity based on orientation
	        	final int activity = PhotoCompassApplication.getActivityForRoll(_roll);
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
     * Gets registered and unregistered at the {@link #_appModel} object.
     */
	private final IApplicationModelCallback _appModelCallback = new IApplicationModelCallback.Stub() {

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
        
        // initialize model variables and register as callback
        _photosModel = Photos.getInstance();
    	_appModel = ApplicationModel.getInstance();
    	_appModel.registerCallback(_appModelCallback);
    }

    /**
     * Gets called when a touch events occurs.
     * Passes the event on to the {@link #_gestureDetector}.
     */
    public boolean onTouchEvent(final MotionEvent event) {
    	if (photosView == null) return false; // photos view not yet created
        return _gestureDetector.onTouchEvent(event);
    }
    
    /**
     * Called when the activity is first created.
     * Initializes the views.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // initialize views
        final FinderView finderView = new FinderView(this);
        final Display display = getWindowManager().getDefaultDisplay();
        photosView = new PhotosView(this, display.getWidth(), display.getHeight() - STATUSBAR_HEIGHT - BOTTOM_CONTROLS_HEIGHT);
        final ControlsView controlsView = new ControlsView(this);

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
    	final Intent locationServiceIntent = new Intent(this, LocationService.class);
    	_boundToLocationService = bindService(locationServiceIntent, _locationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToLocationService) Log.e(PhotoCompassApplication.LOG_TAG, "failed to connect to location service");
    	
        // connect to orientation service
    	final Intent orientationServiceIntent = new Intent(this, OrientationService.class);
    	_boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToOrientationService) Log.e(PhotoCompassApplication.LOG_TAG, "failed to connect to orientation service");
    	
    	// let photos model check if the available photos have changed
    	_photosModel.updatePhotos(this);
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
	            } catch (final DeadObjectException e) {
	    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: location service has crashed");
	    		} catch (final RemoteException e) {
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
	            } catch (final DeadObjectException e) {
	    			Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: orientation service has crashed");
	    		} catch (final RemoteException e) {
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
     * This is called when the overall system is running low on memory, and would like actively running process to try to
     * tighten their belt.
     * We are nice and clear the unneeded photo and border views in the {@link #photosView}. 
     */
    @Override
    public void onLowMemory() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onLowMemory");
    	photosView.clearUnneededViews();
    }
    
    /**
     * Updates the photo view based on the current location and phone orientation as well as the settings in the 
     * {@link ApplicationModel}.
     * Package scoped for faster access by inner classes.
     * 
     * @param latChanged If current latitude has changed.
     * @param lngChanged If current longitude has changed.
     * @param altChanged If current altitude has changed.
     * @param yawChanged If current yaw has changed.
     * @param modelChanged If the application model has changed.
     * @param forceRedraw Force redraw of the view. Otherwise the {@field #PHOTO_VIEW_UPDATE_IVAL} is respected.
     */
    void updatePhotoView(final boolean latChanged, final boolean lngChanged, final boolean altChanged,
    					 final boolean yawChanged, final boolean modelChanged, boolean forceRedraw) {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: updatePhotoView");
    	
    	// redraw the view only if either forced or last redraw older than PHOTO_VIEW_UPDATE_IVAL
		if (forceRedraw || SystemClock.uptimeMillis() - _lastPhotoViewUpdate > PHOTO_VIEW_UPDATE_IVAL) {
			forceRedraw = true;
	    	_lastPhotoViewUpdate = SystemClock.uptimeMillis();
		}
    	
		boolean doRedrawHere;
    	if (latChanged || lngChanged || modelChanged) {
    		
    		doRedrawHere = modelChanged ? forceRedraw : false; // only redraw here if modelChanged, for location changes we do
    														   // other updates first 
    		
    		// update photos
    		_photosModel.updatePhotoProperties(currentLat, currentLng, currentAlt);
    		photosView.addPhotos(_photosModel.getNewlyVisiblePhotos(photosView.getPhotos(),
    																_appModel.getMaxDistance(), _appModel.getMinAge(), _appModel.getMaxAge()),
    															    doRedrawHere);
    		photosView.removePhotos(_photosModel.getNoLongerVisiblePhotos(photosView.getPhotos(),
    																	  _appModel.getMaxDistance(), _appModel.getMinAge(), _appModel.getMaxAge()));
    	}
    	
    	if (latChanged || lngChanged || altChanged) {
    		
    		doRedrawHere = false; // we do other updates first 
    		
    		// update photo text informations
    		if (altChanged && ! latChanged && ! lngChanged) _updateCurrentPhotosProperties(); // already did update on latChanged/lngChanged
    		photosView.updateTextInfos(doRedrawHere);
    	}
    	
    	if (latChanged || lngChanged) {
    		
    		doRedrawHere = forceRedraw;
    		
    		// update sizes of the photos
    		photosView.updateSizes(doRedrawHere);
    	}
    	
    	if (altChanged) {
    		
    		doRedrawHere = forceRedraw;
    		
    		// update y positions of the photos
    		photosView.updateYPositions(doRedrawHere);
    	}
    	
    	if (yawChanged) {
    		
    		doRedrawHere = forceRedraw;
    		
    		// update x positions of the photos
    		photosView.updateXPositions(currentYaw, doRedrawHere);
    	}
    }
    
    /**
     * Updates distance, direction, and altitude offset of the photos currently used by the photos view.
     */
    private void _updateCurrentPhotosProperties() {
    	Photo photo;
    	for (int id : photosView.getPhotos()) {
    		photo = _photosModel.getPhoto(id);
    		if (photo != null) photo.updateDistanceDirectionAndAltitudeOffset(currentLat, currentLng, currentAlt);
    	}
    }
}
