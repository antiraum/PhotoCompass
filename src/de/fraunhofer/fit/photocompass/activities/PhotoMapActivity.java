package de.fraunhofer.fit.photocompass.activities;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ZoomControls;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.services.ILocationService;
import de.fraunhofer.fit.photocompass.services.ILocationServiceCallback;
import de.fraunhofer.fit.photocompass.services.LocationService;
import de.fraunhofer.fit.photocompass.views.PhotoMapOverlay;

public class PhotoMapActivity extends MapActivity {

	private MapView _mapView;
	private LinearLayout _zoomControlsView;
	private ZoomControls _zoomControls;
	private List<Overlay> _mapOverlays;
	private Drawable _drawable;
	private PhotoMapOverlay _itemizedOverlay;
	
    private ILocationService _locationService;
    private boolean _boundToLocationService;

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
    			Log.w(PhotoCompassApplication.LOG_TAG, "failed to register to location service");
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
            
            // get location
            
            // update map view
        }
    };
    
    public PhotoMapActivity() {
    	super();
        _locationService = null;
        _boundToLocationService = false;
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
        // initialize views
        _mapView = (MapView) findViewById(R.id.mapview);
        _zoomControlsView = (LinearLayout) findViewById(R.id.zoomview);
        _zoomControls = (ZoomControls) _mapView.getZoomControls();
        
        // setup views
		setContentView(findViewById(R.id.maplayout));
        _zoomControlsView.addView(_zoomControls);
        
//        _mapOverlays = _mapView.getOverlays();
//        _drawable = this.getResources().getDrawable(R.drawable.icon);
//        _itemizedoverlay = new PhotoMapOverlay(_drawable);
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
    	if (! _boundToLocationService) Log.e(PhotoCompassApplication.LOG_TAG, "failed to connect to location service");
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
	    			Log.w(PhotoCompassApplication.LOG_TAG, "failed to unregister from location service");
	    		}
	    	}

	        // disconnect from location service
	        unbindService(_locationServiceConn);
	        _boundToLocationService = false;
    	}
        
        super.onStop();
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
