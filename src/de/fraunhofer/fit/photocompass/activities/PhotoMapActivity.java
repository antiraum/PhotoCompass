package de.fraunhofer.fit.photocompass.activities;

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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.services.ILocationService;
import de.fraunhofer.fit.photocompass.services.ILocationServiceCallback;
import de.fraunhofer.fit.photocompass.services.IOrientationService;
import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;
import de.fraunhofer.fit.photocompass.services.LocationService;
import de.fraunhofer.fit.photocompass.services.OrientationService;

/**
 * This class is the Activity component for the map view screen (phone held horizontally) of the application.
 */
public final class PhotoMapActivity extends MapActivity {
	
	private static final String MAPS_API_KEY = "02LUNbs-0sTLfQE-JAZ78GXgqz8fRSthtLjrfBw";

	PhotoMapActivity mapActivity; // package scoped for faster access by inner classes

	private MyLocationOverlay _myLocOverlay;
	private MapController _mapController;

    ILocationService locationService; // package scoped for faster access by inner classes
    private boolean _boundToLocationService;
    IOrientationService orientationService; // package scoped for faster access by inner classes
    private boolean _boundToOrientationService;

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
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onLocationEvent: lat = "+lat+", lng = "+lng+", alt = "+alt);
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
            
            // update map view
	    	updateMapView(lat, lng);
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
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
	    	
        	// we are only interested in the roll value
	    	if (roll == _roll) return; // value has not changed
	    	_roll = roll;
            
            // switch to activity based on orientation
        	int activity = PhotoCompassApplication.getActivityForRoll(_roll);
	    	if (activity == PhotoCompassApplication.FINDER_ACTIVITY) {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: switching to finder activity");
	    		startActivity(new Intent(mapActivity, FinderActivity.class));
		        finish(); // close this activity
	    	}
        }
    };
    
    /**
     * Constructor.
     * Initializes the state variables.
     */
    public PhotoMapActivity() {
    	super();
    	
    	mapActivity = this;
        locationService = null;
        _boundToLocationService = false;
        orientationService = null;
        _boundToOrientationService = false;
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
		MapView mapView = new MapView(this, MAPS_API_KEY);
		mapView.setClickable(true);
		mapView.setEnabled(true);
		mapView.setBuiltInZoomControls(true);
		setContentView(mapView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		// current position
		_myLocOverlay = new MyLocationOverlay(this, mapView);
		mapView.getOverlays().add(_myLocOverlay);

//      RelativeLayout mapLayout = new RelativeLayout(this);
//		mapLayout.addView(mapView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
//        setContentView(mapLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        // initialize map controller
		_mapController = mapView.getController();
		_mapController.setZoom(12);
	}
    
    /**
     * Called before the activity becomes visible.
     * Connects to the {@link LocationService} and the {@link OrientationService}.
     * Initiates a update of the {@link Photos} model.
     */
    @Override
    public void onStart() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onStart");
    	super.onStart();
    	
        // connect to location service
    	Intent locationServiceIntent = new Intent(this, LocationService.class);
    	_boundToLocationService = bindService(locationServiceIntent, _locationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToLocationService) Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to connect to location service");
    	
        // connect to orientation service
    	Intent orientationServiceIntent = new Intent(this, OrientationService.class);
    	_boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToOrientationService) Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to connect to orientation service");
    	
    	// let photos model check if the available photos have changed
    	Photos.getInstance().updatePhotos();
    	
    	// enable location and compass overlay
		_myLocOverlay.enableMyLocation();
		_myLocOverlay.enableCompass();
    }
    
    /**
     * Called when the activity is no longer visible.
     * Unregisters the callbacks from the services and then disconnects from the services.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onStop");

    	// disable location and compass overlay
		_myLocOverlay.disableMyLocation();
		_myLocOverlay.disableCompass();
		
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

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    /**
     * Updates the map view based on the current location.
     * 
     * @param Current latitude.
     * @param Current longitude.
     */
    private void updateMapView(double lat, double lng) { // package scoped for faster access by inner classes
		Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: updateMapView");
		
    	// center map view
		GeoPoint location = new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6));
		// _mapController.centerMapTo(m_curLocation, false);
		_mapController.animateTo(location);
		
		// TODO set zoom according to radius of displayed photos
//		_mapController.zoomToSpan(latSpanE6, lonSpanE6);
	}
}
