package de.fraunhofer.fit.photocompass.activities;

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
import de.fraunhofer.fit.photocompass.services.ILocationService;
import de.fraunhofer.fit.photocompass.services.ILocationServiceCallback;
import de.fraunhofer.fit.photocompass.services.IOrientationService;
import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;
import de.fraunhofer.fit.photocompass.services.LocationService;
import de.fraunhofer.fit.photocompass.services.OrientationService;

/* begin 1.1 code */
//public class PhotoMapActivity extends Activity {
/* end 1.1 code */
/* begin 1.5 code */
public class PhotoMapActivity extends MapActivity {
/* end 1.5 code */
	
	private static final String MAPS_API_KEY = "02LUNbs-0sTLfQE-JAZ78GXgqz8fRSthtLjrfBw";

	PhotoMapActivity mapActivity;

//	private MapView _mapView;
	private MapController _mapController;
//	private LinearLayout _zoomControlsView;
//	private ZoomControls _zoomControls;
    
	private double _currentLat;
	private double _currentLng;

    private ILocationService _locationService;
    private boolean _boundToLocationService;
    private IOrientationService _orientationService;
    private boolean _boundToOrientationService;

    private ServiceConnection _locationServiceConn = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: connected to location service");
	    	
	    	// generate service object
	    	_locationService = ILocationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	_locationService.registerCallback(_locationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to register to location service");
            }
	    }
	
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: disconnected from location service");
	    	_locationService = null;
	    }
    };
    
    private ILocationServiceCallback _locationServiceCallback = new ILocationServiceCallback.Stub() {
    	
        public void onLocationEvent(double latitude, double longitude, double altitude) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: received event from location service");
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
            
	    	// update variables
	    	_currentLat = latitude;
	    	_currentLng = longitude;
            
            // update map view
	    	_updateMapView();
        }
    };

    private ServiceConnection _orientationServiceConn = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: connected to orientation service");
	    	
	    	// generate service object
	    	_orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	_orientationService.registerCallback(_orientationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to register to orientation service");
            }
	    }
	
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: disconnected from orientation service");
	    	_orientationService = null;
	    }
    };
    
    private IOrientationServiceCallback _orientationServiceCallback = new IOrientationServiceCallback.Stub() {
		
		private float _roll;
    	
        public void onOrientationEvent(float yaw, float pitch, float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: received event from orientation service");
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
	    	
        	// currently we are only interested in the roll value
	    	if (roll == _roll) return; // value has not changed
	    	_roll = roll;
            
            // switch to activity based on orientation
        	int activity = PhotoCompassApplication.getActivityForRoll(_roll);
	    	if (activity == PhotoCompassApplication.FINDER_ACTIVITY) {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: switching to finder activity");
	    		startActivity(new Intent(mapActivity, FinderActivity.class));
	    	}
            
            // update map view
	    	// TODO
        }
    };
    
    public PhotoMapActivity() {
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
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /* begin 1.1 code */
//        // setup view
//	    setContentView(R.layout.dummymap_layout);
        /* end 1.1 code */
        
        /* begin 1.5 code */
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
		
		_updateMapView();
		
		// TODO display photos
        /* end 1.5 code */
	}
    
    /**
     * Called before the activity becomes visible.
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
    }
    
    /**
     * Called when the activity is no longer visible.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onStop");
    	
    	if (_boundToLocationService) {
	    	
	    	// unregister from location service
	    	if (_locationService != null) {
	    		try {
	    			_locationService.unregisterCallback(_locationServiceCallback);
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
	    	if (_orientationService != null) {
	    		try {
	    			_orientationService.unregisterCallback(_orientationServiceCallback);
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

    /* begin 1.5 code */
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    /* end 1.5 code */
    
    /**
     * Updates the map view based on the current location
     */
    private void _updateMapView() {
		Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: _updateMapView");
    	
    	// dummy values
    	// TODO make this proper
    	_currentLat = Location.convert("50:43:12.59"); // B-IT
    	_currentLng = Location.convert("7:7:16.2"); // B-IT
		
    	// center map view
        /* begin 1.5 code */
		GeoPoint location = new GeoPoint((int)(_currentLat * 1E6), (int)(_currentLng * 1E6));
		// _mapController.centerMapTo(m_curLocation, false);
		_mapController.animateTo(location);
		
		// TODO set zoom according to radius of displayed photos
//		_mapController.zoomToSpan(latSpanE6, lonSpanE6);
	    /* end 1.5 code */
	}
}
