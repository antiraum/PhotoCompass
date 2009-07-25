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
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

/**
 * This class is a Service component that reads data from a location provider.
 * Activities should bind to this Service when they become visible and disconnect when they are no longer visible, so that this
 * Service only runs when needed. After the connection to the service is established activities can register as callbacks to get
 * notified when the location changes.
 */
public final class LocationService extends Service {
	
	private static long MIN_LOCATION_UPDATE_TIME = 2 * 60 * 1000L; // in milliseconds
	public static float MIN_LOCATION_UPDATE_DISTANCE = 10F; // in meters
	
	private static final long CHECK_FOR_BETTER_PROVIDER_IVAL = 5 * 60 * 1000L; // in milliseconds
    private final Handler _providerCheckHandler = new Handler();
    private final Runnable _providerCheckCaller = new Runnable() {
        public void run() {
        	checkForBetterProvider();
        }
    };

	private final Criteria _criteria = new Criteria();
	private final Criteria _fallbackCriteria = new Criteria();
	
	/**
	 * Location provider that is currently used. Gets chosen by the {@link LocationManager} based on {@link #_criteria}.
     * Package scoped for faster access by inner classes.
	 */
	String locationProvider;

    /**
     * List of callbacks that have been registered with the service.
     * Package scoped for faster access by inner classes.
     */
    final RemoteCallbackList<ILocationServiceCallback> remoteCallbacks = new RemoteCallbackList<ILocationServiceCallback>();
    
    /**
     * Implementation of the interface to this service.
     * Is provided to activities when they connect ({@see #onBind(Intent)}).
     */
    private final ILocationService.Stub _binder = new ILocationService.Stub() {
        public void registerCallback(final ILocationServiceCallback cb) {
            if (cb != null) remoteCallbacks.register(cb);
        	
        	// send immediate initial broadcast
            locationListener.onLocationChanged((locationProvider != null) ? locationManager.getLastKnownLocation(locationProvider) : null);
        }
        public void unregisterCallback(final ILocationServiceCallback cb) {
            if (cb != null) remoteCallbacks.unregister(cb);
        }
    };

    /**
     * {@link LocationManager}.
     * Package scoped for faster access by inner classes.
     */
	LocationManager locationManager;
	
	/**
	 * {@link LocationListener} for the {@link #locationManager}.
     * Package scoped for faster access by inner classes.
	 */
	final LocationListener locationListener = new LocationListener() {
		
		/**
		 * Called when the location has changed.
		 * Broadcasts the new location to all registered callbacks.
		 */
		public void onLocationChanged(Location location) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onLocationChanged");
	    	
	    	// check if there are callbacks registered
		    final int numCallbacks = remoteCallbacks.beginBroadcast();
	    	if (numCallbacks == 0) {
	    		remoteCallbacks.finishBroadcast();
	    		return;
	    	}
			
			if (PhotoCompassApplication.USE_DUMMY_LOCATION) location = PhotoCompassApplication.dummyLocation;
			
			if (location == null) return;
			
	        // broadcast the new location to all registered callbacks
			final double lat = location.getLatitude();
			final double lng = location.getLongitude();
			final boolean hasAlt = location.hasAltitude();
			final double alt = location.getAltitude(); 
	        for (int i = 0; i < numCallbacks; i++) {
	            try {
	                remoteCallbacks.getBroadcastItem(i).onLocationEvent(lat, lng, hasAlt, alt);
	            } catch (final DeadObjectException e) {
	                // the RemoteCallbackList will take care of removing the dead object
	            } catch (final RemoteException e) {
	    	    	Log.w(PhotoCompassApplication.LOG_TAG, "broadcast to callback failed");
                }
	        }
	        remoteCallbacks.finishBroadcast();
		}
		
		/**
		 * Called when the provider is disabled by the user.
		 * Re-choose the location provider. 
		 */
		public void onProviderDisabled(final String provider) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onProviderDisabled: provider = "+provider);
	    	chooseLocationProvider(null); // choose new provider
		}

		/**
		 * Called when the provider is enabled by the user.
		 */
		public void onProviderEnabled(final String provider) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onProviderEnabled: provider = "+provider);
		}
		
		/**
		 * Called when the provider status changes.
		 * If the status changes to {@link #LocationProvider.OUT_OF_SERVICE} a new location provider gets chosen.
		 * If the status changes to {@link #LocationProvider.TEMPORARILY_UNAVAILABLE} we check for an available better provider,
		 * otherwise we keep checking the current one.
		 */
		public void onStatusChanged(final String provider, final int status, final Bundle extras) {
	    	
	    	if (status == LocationProvider.OUT_OF_SERVICE) {
	    		// provider is out of service, and this is not expected to change in the near future
	    		
		    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onStatusChanged: provider = "+provider+
		    										   ", status = OUT_OF_SERVICE");
	    		chooseLocationProvider(null); // choose new provider
	    		
	    	} else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
	    		// provider is temporarily unavailable

		    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onStatusChanged: provider = "+provider+
		    										   ", status = TEMPORARILY_UNAVAILABLE");
	    		chooseLocationProvider(locationProvider); // look for better provider 
	    		// TODO make a time limit for TEMPORARILY_UNAVAILABLE / if exceeded switch to any other available provider
	    	}
		}
    };
    
    public LocationService() {
    	// the emulator is always getting new locations with a great variety
    	if (PhotoCompassApplication.RUNNING_ON_EMULATOR) {
	    	MIN_LOCATION_UPDATE_TIME = 5 * 60 * 1000; // in milliseconds
	    	MIN_LOCATION_UPDATE_DISTANCE = 1000; // in meters
    	}
    }
	
    /**
     * Called by the system when the service is first created.
     * Initializes the {@link #locationManager}, sets up the {@link #locationProvider} criteria, chooses and starts listening
     * to the initial location provider, and starts the regular checks for a better location provider.
     */
    @Override
    public void onCreate() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onCreate");
        super.onCreate();
        
        // initialize location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        	
    	// setup criteria for choosing the location provider
//    	_criteria.setAccuracy(Criteria.ACCURACY_FINE);
    	_criteria.setAccuracy(Criteria.ACCURACY_COARSE); // changed to enable switch to network provider at FIT
    													 // TODO re-check the strategy
//    	_criteria.setAltitudeRequired(true);
    	_criteria.setAltitudeRequired(false); // disabled altitude support in this release
    	_criteria.setBearingRequired(false);
    	_criteria.setCostAllowed(false);
    	_criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
    	_criteria.setSpeedRequired(false);
    	_fallbackCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
    	_fallbackCriteria.setAltitudeRequired(false);
    	_fallbackCriteria.setBearingRequired(false);
    	_fallbackCriteria.setCostAllowed(false);
    	_fallbackCriteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
    	_fallbackCriteria.setSpeedRequired(false);
    	
    	// choose initial provider
    	chooseLocationProvider(null);

        // start the regular checks for a better provider
        checkForBetterProvider();
    }
    
    /**
     * Regularly (every {@link #CHECK_FOR_BETTER_PROVIDER_IVAL} milliseconds) called method that checks if a better location
     * provider than the current one is available.
     * The method schedules its next call by itself.
     * Package scoped for faster access by inner classes.
     */
    void checkForBetterProvider() {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: checkForBetterProvider");

    	// check for better provider
    	chooseLocationProvider(locationProvider);
    	
        // schedule next call
        _providerCheckHandler.postDelayed(_providerCheckCaller, CHECK_FOR_BETTER_PROVIDER_IVAL);
    }
    
    /**
     * Chooses a location provider and starts getting updates from it.
     * First stops listening to the current provider, then tries to find the best possible provider, and starts listening to it. 
     * Package scoped for faster access by inner classes.
     * 
     * @param currentProvider Current provider. Pass this argument when the provider should only be switched if a better provider
     * 						  is available. 
     */
    void chooseLocationProvider(final String currentProvider) {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: chooseLocationProvider: current location provider is "+currentProvider);
    	
    	// first check for good and enabled provider
    	String newProvider = locationManager.getBestProvider(_criteria, true);
    	
    	if (currentProvider != null && (newProvider == null || newProvider.equals(currentProvider))) { // no better provider found
//        	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: chooseLocationProvider: no better provider found");
    		return;
    	}

    	// second check for any enabled provider
    	if (newProvider == null) newProvider = locationManager.getBestProvider(_fallbackCriteria, true);

    	// third check for good not enabled provider
    	if (newProvider == null) newProvider = locationManager.getBestProvider(_criteria, true);

    	// forth check for any not enabled provider
    	if (newProvider == null) newProvider = locationManager.getBestProvider(_fallbackCriteria, true);
    	
    	// stop listening to the current provider
    	if (locationProvider != null) locationManager.removeUpdates(locationListener);
    	
    	// upate location provider
    	locationProvider = newProvider;
    	
    	// no provider found
    	if (locationProvider == null) {
        	Log.e(PhotoCompassApplication.LOG_TAG, "LocationService: chooseLocationProvider: no location provider found");
        	// TODO notify the user when there is no provider and tell him that he cannot use the application without it
    		return;
    	}
    	
    	// start getting updates
    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: chooseLocationProvider: location provider changed to "+locationProvider);
    	locationManager.requestLocationUpdates(locationProvider, MIN_LOCATION_UPDATE_TIME, MIN_LOCATION_UPDATE_DISTANCE,
    										   locationListener, getMainLooper());
    }

    /**
     * Called when an activity connects to the service.
     * 
     * @return The {@link #_binder} interface to the service.
     */
	@Override
	public IBinder onBind(final Intent intent) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onBind");
		return _binder;
	}

	/**
	 * Called by the system to notify a Service that it is no longer used and is being removed.
	 * Shuts down the service.
	 */
    @Override
    public void onDestroy() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "LocationService: onDestroy");
    	
    	// stop looking for a better location provider
    	_providerCheckHandler.removeCallbacks(_providerCheckCaller);

        // unregister all callbacks
    	remoteCallbacks.kill();
    	
    	// stop getting updates
    	locationManager.removeUpdates(locationListener);
    	locationManager = null;
    	
        super.onDestroy();
    }
}