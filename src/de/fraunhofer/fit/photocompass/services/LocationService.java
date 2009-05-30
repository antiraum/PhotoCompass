package de.fraunhofer.fit.photocompass.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

public class LocationService extends Service {
	
	private static final int MIN_LOCATION_UPDATE_TIME = 3 * 1000; // in milliseconds
	private static final int MIN_LOCATION_UPDATE_DISTANCE = 1; // in meters
	
	private String _locationProvider;
	
	private Location _dummyLocation; // dummy location for development
	private boolean USE_DUMMY_LOCATION = true;

    /**
     * List of callbacks that have been registered with the service.
     * This is package scoped (instead of private) so that it can be accessed more efficiently from inner classes.
     */
    final RemoteCallbackList<ILocationServiceCallback> remoteCallbacks = new RemoteCallbackList<ILocationServiceCallback>();
    
    private final ILocationService.Stub _binder = new ILocationService.Stub() {
        public void registerCallback(ILocationServiceCallback cb) {
            if (cb != null) remoteCallbacks.register(cb);
        	
        	// send immediate initial broadcast
            _locationListener.onLocationChanged((_locationProvider != null) ? _locationManager.getLastKnownLocation(_locationProvider) : null);
        }
        public void unregisterCallback(ILocationServiceCallback cb) {
            if (cb != null) remoteCallbacks.unregister(cb);
        }
    };
	
	private LocationManager _locationManager;
	
	private LocationListener _locationListener = new LocationListener() {
		
		public void onLocationChanged(Location location) {
			
			if (USE_DUMMY_LOCATION) location = _dummyLocation;
			
			if (location == null) return;
	    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onLocationChanged");
			
	        // broadcast the new location to all registered callbacks
	        final int numCallbacks = remoteCallbacks.beginBroadcast();
	    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: numCallbacks = "+numCallbacks);
	        for (int i = 0; i < numCallbacks; i++) {
	            try {
	                remoteCallbacks.getBroadcastItem(i).onLocationEvent(location.getLatitude(), location.getLongitude(),
	                													location.getAltitude());
	            } catch (DeadObjectException e) {
	                // the RemoteCallbackList will take care of removing the dead object
	            } catch (RemoteException e) {
	    	    	Log.w(PhotoCompassApplication.LOG_TAG, "broadcast to callback failed");
                }
	        }
	        remoteCallbacks.finishBroadcast();
		}
		
		public void onProviderDisabled(String provider) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onProviderDisabled: provider = "+provider);
		}

		public void onProviderEnabled(String provider) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onProviderEnabled: provider = "+provider);
		}
		
		public void onStatusChanged(String provider, int status, Bundle extras) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onStatusChanged: provider = "+provider+", status = "+status);
	    	// removed code from Humberto -- I cannot see what's the point in broadcasting an illegal dummy location when the provider
	    	// becomes unavailable. Either we do something useful here or do nothing.
		}
    };
    
    public LocationService() {
    	super();

    	_dummyLocation = new Location("");
//    	_dummyLocation.setLatitude(Location.convert("50:43:12.59")); // B-IT
//    	_dummyLocation.setLongitude(Location.convert("7:7:16.2")); // B-IT
//    	_dummyLocation.setAltitude(103); // B-IT
    	_dummyLocation.setLatitude(Location.convert("50:44:58.43")); // FIT
    	_dummyLocation.setLongitude(Location.convert("7:12:14.54")); // FIT
    	_dummyLocation.setAltitude(125); // FIT
    }
	
    @Override
    public void onCreate() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onCreate");
        super.onCreate();
        
        // initialize location manager
        _locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
    	// let the location manager decide which provider to use (GPS or network)
    	Criteria criteria = new Criteria();
    	criteria.setAccuracy(Criteria.ACCURACY_COARSE); // Faster, no GPS fix.
//    	criteria.setAccuracy(Criteria.ACCURACY_FINE); // More accurate, GPS fix.
    	_locationProvider = _locationManager.getBestProvider(criteria, false);

    	if (_locationProvider == null) {
        	// TODO notify the user when there is no provider and tell him that he cannot use the application without it
        	Log.e(PhotoCompassApplication.LOG_TAG, "LocationService: no location provider found");
    		return;
    	}
    	
    	// start getting updates
    	Log.e(PhotoCompassApplication.LOG_TAG, "LocationService: locationProvider = "+_locationProvider);
    	_locationManager.requestLocationUpdates(_locationProvider, MIN_LOCATION_UPDATE_TIME, MIN_LOCATION_UPDATE_DISTANCE,
    											_locationListener, getMainLooper());
    }

	@Override
	public IBinder onBind(Intent intent) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onBind");
		return _binder;
	}

    @Override
    public void onDestroy() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onDestroy");

        // unregister all callbacks
    	remoteCallbacks.kill();
    	
    	// stop getting updates
    	_locationManager.removeUpdates(_locationListener);
    	_locationManager = null;
    	
        super.onDestroy();
    }
}