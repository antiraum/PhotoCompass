package de.fraunhofer.fit.photocompass.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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

	String locationProvider; // package scoped for faster access by inner classes

    /**
     * List of callbacks that have been registered with the service.
     * Package scoped for faster access by inner classes.
     */
    final RemoteCallbackList<ILocationServiceCallback> remoteCallbacks = new RemoteCallbackList<ILocationServiceCallback>();
    
    private final ILocationService.Stub _binder = new ILocationService.Stub() {
        public void registerCallback(ILocationServiceCallback cb) {
            if (cb != null) remoteCallbacks.register(cb);
        	
        	// send immediate initial broadcast
            locationListener.onLocationChanged((locationProvider != null) ? locationManager.getLastKnownLocation(locationProvider) : null);
        }
        public void unregisterCallback(ILocationServiceCallback cb) {
            if (cb != null) remoteCallbacks.unregister(cb);
        }
    };

	LocationManager locationManager; // package scoped for faster access by inner classes
	
	final LocationListener locationListener = new LocationListener() { // package scoped for faster access by inner classes
		
		public void onLocationChanged(Location location) {
			
			if (PhotoCompassApplication.USE_DUMMY_LOCATION) location = PhotoCompassApplication.dummyLocation;
			
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
	
    @Override
    public void onCreate() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onCreate");
        super.onCreate();
        
        // initialize location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
    	// let the location manager decide which provider to use (GPS or network)
    	Criteria criteria = new Criteria();
    	criteria.setAccuracy(Criteria.ACCURACY_COARSE); // Faster, no GPS fix.
//    	criteria.setAccuracy(Criteria.ACCURACY_FINE); // More accurate, GPS fix.
    	locationProvider = locationManager.getBestProvider(criteria, false);

    	if (locationProvider == null) {
        	// TODO notify the user when there is no provider and tell him that he cannot use the application without it
        	Log.e(PhotoCompassApplication.LOG_TAG, "LocationService: no location provider found");
    		return;
    	}
    	
    	// start getting updates
    	Log.e(PhotoCompassApplication.LOG_TAG, "LocationService: locationProvider = "+locationProvider);
    	locationManager.requestLocationUpdates(locationProvider, MIN_LOCATION_UPDATE_TIME, MIN_LOCATION_UPDATE_DISTANCE,
    											locationListener, getMainLooper());
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
    	locationManager.removeUpdates(locationListener);
    	locationManager = null;
    	
        super.onDestroy();
    }
}