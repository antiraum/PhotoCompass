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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

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
public class PhotoMapActivity extends MapActivity {
	
	private static final String MAPS_API_KEY = "02LUNbs-0sTLfQE-JAZ78GXgqz8fRSthtLjrfBw";

	PhotoMapActivity mapActivity;

//	private MapView _mapView;
	private MapController _mapController;
//	private LinearLayout _zoomControlsView;
//	private ZoomControls _zoomControls;
    
	double currentLat; // package scoped for faster access by inner classes
	double currentLng; // package scoped for faster access by inner classes

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
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: connected to location service");
	    	
	    	// generate service object
	    	locationService = ILocationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	locationService.registerCallback(locationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to register to location service");
            }
	    }

    	/**
    	 * Gets called when the service connection is closed down.
    	 * Frees {@link #locationService}.
    	 */
	    public void onServiceDisconnected(ComponentName name) {
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
        public void onLocationEvent(double lat, double lng, boolean hasAlt, double alt) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: received event from location service");
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
            
	    	// update variables
	    	currentLat = lat;
	    	currentLng = lng;
            
            // update map view
	    	updateMapView();
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
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: connected to orientation service");
	    	
	    	// generate service object
	    	orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	orientationService.registerCallback(orientationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to register to orientation service");
            }
	    }

    	/**
    	 * Gets called when the service connection is closed down.
    	 * Frees {@link #orientationService}.
    	 */
	    public void onServiceDisconnected(ComponentName name) {
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
        public void onOrientationEvent(float yaw, float pitch, float roll) {
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
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // initialize views
        RelativeLayout mapLayout = new RelativeLayout(this);
		MapView mapView = new MapView(this, MAPS_API_KEY);
		mapView.setClickable(true);
//		mapView.displayZoomControls(true);
		LinearLayout zoomControlsView = new LinearLayout(this);
		ZoomControls zoomControls = (ZoomControls) mapView.getZoomControls();
        zoomControlsView.addView(zoomControls);
        
        // setup views
		mapLayout.addView(mapView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		mapLayout.addView(zoomControlsView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setContentView(mapLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        // initialize map controller
		_mapController = mapView.getController();
		_mapController.setZoom(12);
		
		updateMapView();
		
		// TODO display photos
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
    }
    
    /**
     * Called when the activity is no longer visible.
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
	            } catch (DeadObjectException e) {
	            	// the service has crashed
	    		} catch (RemoteException e) {
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
	            } catch (DeadObjectException e) {
	            	// the service has crashed
	    		} catch (RemoteException e) {
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
     */
    private void updateMapView() { // package scoped for faster access by inner classes
		Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: updateMapView");
		
    	// center map view
		GeoPoint location = new GeoPoint((int)(currentLat * 1E6), (int)(currentLng * 1E6));
		// _mapController.centerMapTo(m_curLocation, false);
		_mapController.animateTo(location);
		
		// TODO set zoom according to radius of displayed photos
//		_mapController.zoomToSpan(latSpanE6, lonSpanE6);
	}
}
