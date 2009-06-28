package de.fraunhofer.fit.photocompass.activities;

import java.util.List;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

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
import de.fraunhofer.fit.photocompass.views.layouts.RotateView;
import de.fraunhofer.fit.photocompass.views.overlays.CustomMyLocationOverlay;
import de.fraunhofer.fit.photocompass.views.overlays.PhotosOverlay;
import de.fraunhofer.fit.photocompass.views.overlays.ViewingDirectionOverlay;

/**
 * This class is the Activity component for the map view screen (phone held horizontally) of the application.
 */
public final class PhotoMapActivity extends MapActivity {
	
	private static final String MAPS_API_KEY = "02LUNbs-0sTLfQE-JAZ78GXgqz8fRSthtLjrfBw";

	PhotoMapActivity mapActivity; // package scoped for faster access by inner classes
    
	double currentLat = 0; // package scoped for faster access by inner classes
	double currentLng = 0; // package scoped for faster access by inner classes
	double currentAlt = 0; // package scoped for faster access by inner classes
	float currentYaw = 0; // package scoped for faster access by inner classes

	private RotateView _rotateView;
	private MapView _mapView;
	private MapController _mapController;
	
	// overlays
	private ViewingDirectionOverlay _viewDirOverlay;
	private CustomMyLocationOverlay _customMyLocOverlay;
	private PhotosOverlay _photosOverlay;

    ILocationService locationService; // package scoped for faster access by inner classes
    private boolean _boundToLocationService;
    IOrientationService orientationService; // package scoped for faster access by inner classes
    private boolean _boundToOrientationService;

    private Photos _photosModel;
    private ApplicationModel _appModel;

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
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: connected to location service");
	    	
	    	// generate service object
	    	locationService = ILocationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	locationService.registerCallback(locationServiceCallback);
            } catch (final DeadObjectException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: location service has crashed");
            } catch (final RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to register to location service");
            }
	    }

    	/**
    	 * Gets called when the service connection is closed down.
    	 * Frees {@link #locationService}.
    	 */
	    public void onServiceDisconnected(final ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: disconnected from location service");
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
        	
        	if (isFinishing()) return; // activity is finishing, we don't do anything anymore
        	
        	if (lat == currentLat && lng == currentLng && (! hasAlt || alt == currentAlt)) return; // no change
        	
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onLocationEvent");
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onLocationEvent: lat = "+lat+", lng = "+lng+", alt = "+alt);
            
        	final boolean latChanged = (lat == currentLat) ? false : true;
        	final boolean lngChanged = (lng == currentLng) ? false : true;
	    	
	    	// update variables
	    	currentLat = lat;
	    	currentLng = lng;
	    	if (hasAlt) currentAlt = alt;
            
            // update map view
	    	updateMapView(latChanged, lngChanged, false, false);
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
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: connected to orientation service");
	    	
	    	// generate service object
	    	orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	orientationService.registerCallback(orientationServiceCallback);
            } catch (final DeadObjectException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: orientation service has crashed");
            } catch (final RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to register to orientation service");
            }
	    }

    	/**
    	 * Gets called when the service connection is closed down.
    	 * Frees {@link #orientationService}.
    	 */
	    public void onServiceDisconnected(final ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: disconnected from orientation service");
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
		 * Initiates switch to {@link FinderActivity} when the phone is held vertically. 
		 */
        public void onOrientationEvent(final float yaw, final float pitch, final float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: received event from orientation service");
        	
        	if (isFinishing()) return; // activity is finishing, we don't do anything anymore
        	
	    	if (roll != _roll) {
		    	_roll = roll;
	            
	            // switch to activity based on orientation
	        	final int activity = PhotoCompassApplication.getActivityForRoll(_roll);
		    	if (activity == PhotoCompassApplication.FINDER_ACTIVITY) {
		    		Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: switching to finder activity");
		    		startActivity(new Intent(mapActivity, FinderActivity.class));
			        finish(); // close this activity
					System.gc(); // good point to run the GC
		    	}
	    	}
	    	
        	final boolean yawChanged = (yaw == currentYaw) ? false : true;
	    	
	    	// update variables
	    	currentYaw = yaw;

            // update map view
	    	updateMapView(false, false, yawChanged, false);
        }
    };

    /**
     * Callback object for the {@link ApplicationModel}.
     * Gets registered and unregistered at the {@link #_appModel} object.
     */
	private final IApplicationModelCallback _appModelCallback = new IApplicationModelCallback.Stub() {

		/**
		 * Gets called when the minimum distance in the {@link ApplicationModel} changes.
		 * Initiates a update of {@link #photosView}. 
		 */
		public void onMinDistanceChange(final float minDistance, final float minDistanceRel) {
        	
        	if (isFinishing()) return; // activity is finishing, we don't do anything anymore
        	
	    	updateMapView(false, false, false, true);
		}

		/**
		 * Gets called when the maximum distance in the {@link ApplicationModel} changes.
		 * Initiates a update of {@link #photosView}. 
		 */
		public void onMaxDistanceChange(final float maxDistance, final float maxDistanceRel) {
        	
        	if (isFinishing()) return; // activity is finishing, we don't do anything anymore
        	
	    	updateMapView(false, false, false, true);
		}

		/**
		 * Gets called when the minimum age in the {@link ApplicationModel} changes.
		 * Initiates a update of {@link #photosView}. 
		 */
		public void onMinAgeChange(final long minAge, final float minAgeRel) {
        	
        	if (isFinishing()) return; // activity is finishing, we don't do anything anymore
        	
	    	updateMapView(false, false, false, true);
		}

		/**
		 * Gets called when the maximum age in the {@link ApplicationModel} changes.
		 * Initiates a update of {@link #photosView}. 
		 */
		public void onMaxAgeChange(final long maxAge, final float maxAgeRel) {
        	
        	if (isFinishing()) return; // activity is finishing, we don't do anything anymore
        	
	    	updateMapView(false, false, false, true);
		}
	};
	
    /**
     * Constructor.
     * Initializes the state variables.
     */
    public PhotoMapActivity() {
    	super();
    	mapActivity = this;
    	
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
     * Called when the activity is first created.
     * Initializes the views and map components.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // map view
		_mapView = new MapView(this, MAPS_API_KEY);
		_mapView.setClickable(true);
		_mapView.setEnabled(true);
		_mapView.setBuiltInZoomControls(true); // XXX comment this line for target 1 compatibility
//		setContentView(_mapView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        _rotateView = new RotateView(this);
        _rotateView.addView(_mapView);
        setContentView(_rotateView);
		
		// viewing direction overlay
		_viewDirOverlay = new ViewingDirectionOverlay();
		List<Overlay> overlays = _mapView.getOverlays();
		overlays.add(_viewDirOverlay);
		
		// own current position overlay
		_customMyLocOverlay = new CustomMyLocationOverlay();
		overlays.add(_customMyLocOverlay);
		
		// photos overlay
		_photosOverlay = new PhotosOverlay();
		overlays.add(_photosOverlay);
		
        // initialize map controller
		_mapController = _mapView.getController();
		_mapController.setZoom(15);
	}
    
    /**
     * Called before the activity becomes visible.
     * Connects to the {@link LocationService} and the {@link OrientationService}.
     * Initiates a update of the {@link Photos} model.
     * Enables the current location and compass overlay.
     */
    @Override
    public void onStart() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onStart");
    	super.onStart();
    	
        // connect to location service
    	final Intent locationServiceIntent = new Intent(this, LocationService.class);
    	_boundToLocationService = bindService(locationServiceIntent, _locationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToLocationService) Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to connect to location service");
    	
        // connect to orientation service
    	final Intent orientationServiceIntent = new Intent(this, OrientationService.class);
    	_boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToOrientationService) Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to connect to orientation service");
    	
    	// let photos model check if the available photos have changed
    	Photos.getInstance().updatePhotos(this);
    }
    
    /**
     * Called when the activity is no longer visible.
     * Disables the current location and compass overlay.
     * Unregisters the callbacks from the services and then disconnects from the services.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onStop");
		
    	if (_boundToLocationService) {
	    	
	    	// unregister from location service
	    	if (locationService != null) {
	    		try {
	    			locationService.unregisterCallback(locationServiceCallback);
	            } catch (final DeadObjectException e) {
	    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: location service has crashed");
	    		} catch (final RemoteException e) {
	    			Log.w(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to unregister from location service");
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
	    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: orientation service has crashed");
	    		} catch (final RemoteException e) {
	    			Log.w(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to unregister from orientation service");
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
     * We are nice and clear the unneeded bitmaps in the {@link #_photosOverlay}. 
     */
    @Override
    public void onLowMemory() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onLowMemory");
    	_photosOverlay.clearUnneededBitmaps();
    }

    /**
     * Tell the Google server that we are not displaying any kind of route information.
     */
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    /**
     * Updates the map view based on the current location.
     * Centers the map to the location and updates the photo overlays.
     * Package scoped for faster access by inner classes.
     * 
     * @param latChanged If current latitude has changed.
     * @param lngChanged If current longitude has changed.
     * @param modelChanged If the application model has changed.
     */
    void updateMapView(final boolean latChanged, final boolean lngChanged, final boolean yawChanged, final boolean modelChanged) {
//		Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: updateMapView");
		
    	if (latChanged || lngChanged) {
    	
	    	// center map view
			final GeoPoint currentLocation = new GeoPoint((int)(currentLat * 1E6), (int)(currentLng * 1E6));
			_mapController.animateTo(currentLocation);
			
			// update viewing direction overlay
			_viewDirOverlay.updateLocation(currentLocation);
			
			// update current position overlay
			_customMyLocOverlay.update(currentLocation);
    	}
		
    	if (modelChanged) {

    		// TODO set zoom according to radius of displayed photos
//    		_mapController.zoomToSpan(latSpanE6, lngSpanE6);
    	}
		
    	if (latChanged || lngChanged || modelChanged) {
    		
    		// update photos
    		if (latChanged || lngChanged) _photosModel.updatePhotoProperties(currentLat, currentLng, currentAlt);
    		_photosOverlay.addPhotos(_photosModel.getNewlyVisiblePhotos(_photosOverlay.photos, false, true));
    		_photosOverlay.removePhotos(_photosModel.getNoLongerVisiblePhotos(_photosOverlay.photos, false, true));
    	}
		
    	if (yawChanged) {
    		
			// update viewing direction overlay
			_viewDirOverlay.updateDirection(currentYaw);
    	}
 
		// redraw map
		_mapView.postInvalidate();
		
		// rotate
		if (yawChanged) _rotateView.setHeading(currentYaw);
	}
    
    /**
     * Populate the options menu.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu = OptionsMenu.populateMenu(menu);
        return true;
    }

    /**
     * Handles the option menu item selections.
     */
    public boolean onOptionsItemSelected(final MenuItem item) {
    	return OptionsMenu.handleMenuItemSelection(item, this);
    }

    /**
     * Gets called when a started {@link Activity} returns a result.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onActivityResult");
        if (requestCode == OptionsMenu.CAMERA_RETURN) {
            if (resultCode == RESULT_OK) {
        		Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onActivityResult CAMERA_RETURN RESULT_OK");
            	// FIXME at the moment the photo isn't saved - either we have to do this on our own, or
            	// we can call the camera application in another way
//                Bitmap bitmap = (Bitmap) data.getParcelableExtra("data");
//                if (bitmap != null) {
//                	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onActivityResult: we have a bmp");
//                }
//            	Photos.getInstance().updatePhotos(this);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
