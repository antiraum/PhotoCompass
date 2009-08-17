package de.fraunhofer.fit.photocompass.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.Settings;
import de.fraunhofer.fit.photocompass.services.ILocationService;
import de.fraunhofer.fit.photocompass.services.ILocationServiceCallback;
import de.fraunhofer.fit.photocompass.services.IOrientationService;
import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;
import de.fraunhofer.fit.photocompass.services.IPhotosService;
import de.fraunhofer.fit.photocompass.services.IPhotosServiceCallback;
import de.fraunhofer.fit.photocompass.services.ISettingsService;
import de.fraunhofer.fit.photocompass.services.LocationService;
import de.fraunhofer.fit.photocompass.services.OrientationService;
import de.fraunhofer.fit.photocompass.services.PhotosService;
import de.fraunhofer.fit.photocompass.services.SettingsService;

/**
 * This decorator class is to be used by all activity components of the applications in order to use the service
 * components. Activities must implement the {@link IServiceActivity} interface. This class handles the service
 * connections.
 */
public final class ServiceConnections {
    
    /**
     * Instance of the using activity. Package scoped for faster access by inner classes.
     */
    final Activity activity;
    /**
     * Instance of the using activity. Package scoped for faster access by inner classes.
     */
    final IServiceActivity serviceActivity;
    /**
     * Type of using activity. See {@link PhotoCompassApplication} for the defined types. Package scoped for faster
     * access by inner classes.
     */
    final int activityType;
    
    /**
     * Instance of the {@link Settings} model. Is read from the {@SettingsService} upon connecting and
     * saved at the service before disconnection. Package scoped for faster access by inner classes.
     */
    Settings settings;
    
    /**
     * Settings service interface object. Package scoped for faster access by inner classes.
     */
    ISettingsService settingsService = null;
    /**
     * Binding status to the settings service.
     */
    private boolean _boundToSettingsService = false;
    
    /**
     * Photos service interface object. Package scoped for faster access by inner classes.
     */
    IPhotosService photosService = null;
    /**
     * Binding status to the photos service.
     */
    private boolean _boundToPhotosService = false;
    
    /**
     * Location service interface object. Package scoped for faster access by inner classes.
     */
    ILocationService locationService = null;
    /**
     * Binding status to the location service.
     */
    private boolean _boundToLocationService = false;
    
    /**
     * Orientation service interface object. Package scoped for faster access by inner classes.
     */
    IOrientationService orientationService = null;
    /**
     * Binding status to the orientation service.
     */
    private boolean _boundToOrientationService = false;
    
    /**
     * Connection object for the connection with the {@link SettingsService}.
     */
    private final ServiceConnection _settingsServiceConn = new ServiceConnection() {
        
        /**
         * Called when the service connection is established. Creates the {@link #settingsService} object from the
         * service interface and reads the {@link Settings}.
         */
        public void onServiceConnected(final ComponentName name, final IBinder service) {

            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: connected to settings service");
            
            // generate service object
            settingsService = ISettingsService.Stub.asInterface(service);
            
            // get the settings
            try {
                settings = settingsService.getSettings();
                
                if (settings == null) {
                    Log.e(PhotoCompassApplication.LOG_TAG,
                          "ServiceConnections: settings received from the settings service is null");
                }
                
                // now we can connect to the photos service
                connectToPhotosService();
            } catch (final RemoteException e) {
                Log.e(PhotoCompassApplication.LOG_TAG, "ServiceConnections: failed to call settings service");
            }
        }
        
        /**
         * Called when the service connection is closed down. Frees {@link #settingsService}.
         */
        public void onServiceDisconnected(final ComponentName name) {

            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: disconnected from settings service");
            settingsService = null;
        }
    };
    
    /**
     * Connection object for the connection with the {@link PhotosService}.
     */
    private final ServiceConnection _photosServiceConn = new ServiceConnection() {
        
        /**
         * Called when the service connection is established. Creates the {@link #photosService} object from the service
         * interface and registers the {@link #photosServiceCallback}.
         */
        public void onServiceConnected(final ComponentName name, final IBinder service) {

            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: connected to photos service");
            
            // generate service object
            photosService = IPhotosService.Stub.asInterface(service);
            
            try {
                photosService.registerCallback(photosServiceCallback); // register at the service
                photosService.initialize(settings); // let photos service check for new photos
            } catch (final RemoteException e) {
                Log.e(PhotoCompassApplication.LOG_TAG, "ServiceConnections: failed to call photos service");
            }
        }
        
        /**
         * Called when the service connection is closed down. Frees {@link #photosService}.
         */
        public void onServiceDisconnected(final ComponentName name) {

            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: disconnected from photos service");
            photosService = null;
        }
    };
    
    /**
     * Callback object for the {@link PhotosService}. Gets registered and unregistered at the {@link #photosService}
     * object. Package scoped for faster access by inner classes.
     */
    final IPhotosServiceCallback photosServiceCallback = new IPhotosServiceCallback.Stub() {
        
        /**
         * Called when the list of photo distances has changed.
         * 
         * @param photoDistances Distances of the photos in relative values (0..1).
         */
        public void onPhotoDistancesChange(final float[] photoDistances) {

            if (activity.isFinishing()) return; // activity is finishing, we don't do anything anymore
                
            serviceActivity.onPhotosServicePhotoDistancesChange(photoDistances); // run class specific code
        }
        
        /**
         * Called when the list of photo ages has changed.
         * 
         * @param photoAges Ages of the photos in relative values (0..1).
         */
        public void onPhotoAgesChange(final float[] photoAges) {

            if (activity.isFinishing()) return; // activity is finishing, we don't do anything anymore
                
            serviceActivity.onPhotosServicePhotoAgesChange(photoAges); // run class specific code
        }
    };
    
    /**
     * Connection object for the connection with the {@link LocationService}.
     */
    private final ServiceConnection _locationServiceConn = new ServiceConnection() {
        
        /**
         * Called when the service connection is established. Creates the {@link #locationService} object from the
         * service interface and registers the {@link #locationServiceCallback}.
         */
        public void onServiceConnected(final ComponentName className, final IBinder service) {

            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: connected to location service");
            
            // generate service object
            locationService = ILocationService.Stub.asInterface(service);
            
            // register at the service
            try {
                locationService.registerCallback(locationServiceCallback);
            } catch (final RemoteException e) {
                Log.e(PhotoCompassApplication.LOG_TAG, "ServiceConnections: failed to register to location service");
            }
        }
        
        /**
         * Called when the service connection is closed down. Frees {@link #locationService}.
         */
        public void onServiceDisconnected(final ComponentName name) {

            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: disconnected from location service");
            locationService = null;
        }
    };
    
    /**
     * Callback object for the {@link LocationService}. Gets registered and unregistered at the {@link #locationService}
     * object. Package scoped for faster access by inner classes.
     */
    final ILocationServiceCallback locationServiceCallback = new ILocationServiceCallback.Stub() {
        
        private double _lastLat = 0;
        private double _lastLng = 0;
        
        /**
         * Called when new location data is available.
         */
        public void onLocationEvent(final double lat, final double lng, final boolean hasAlt, final double alt) {

            if (activity.isFinishing()) return; // activity is finishing, we don't do anything anymore
                
            // Check the distance between last and new location and abort if smaller than the minimum
            // update distance. Otherwise we get too many updates because of invalid altitude values.
            final float[] results = new float[1];
            Location.distanceBetween(_lastLat, _lastLng, lat, lng, results);
            if (results[0] < LocationService.MIN_LOCATION_UPDATE_DISTANCE) return;
            
            _lastLat = lat;
            _lastLng = lng;
            
            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: onLocationEvent: lat = " + lat + ", lng = " +
                                                   lng + ", alt = " + alt);
            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: onLocationEvent: changed by " + results[0] +
                                                   " meters");
            
            serviceActivity.onLocationServiceLocationEvent(lat, lng, hasAlt, alt); // run class specific code
        }
    };
    
    /**
     * Connection object for the connection with the {@link OrientationService}.
     */
    private final ServiceConnection _orientationServiceConn = new ServiceConnection() {
        
        /**
         * Called when the service connection is established. Creates the {@link #orientationService} object from the
         * service interface and registers the {@link #orientationServiceCallback}.
         */
        public void onServiceConnected(final ComponentName className, final IBinder service) {

            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: connected to orientation service");
            
            // generate service object
            orientationService = IOrientationService.Stub.asInterface(service);
            
            // register at the service
            try {
                orientationService.registerCallback(orientationServiceCallback);
            } catch (final RemoteException e) {
                Log.e(PhotoCompassApplication.LOG_TAG, "ServiceConnections: failed to register to orientation service");
            }
        }
        
        /**
         * Called when the service connection is closed down. Frees {@link #orientationService}.
         */
        public void onServiceDisconnected(final ComponentName name) {

            Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: disconnected from orientation service");
            orientationService = null;
        }
    };
    
    /**
     * Callback object for the {@link OrientationService}. Gets registered and unregistered at the
     * {@link #orientationService} object. Package scoped for faster access by inner classes.
     */
    final IOrientationServiceCallback orientationServiceCallback = new IOrientationServiceCallback.Stub() {
        
        private float _pitch;
        private float _roll;
        
        /**
         * Called when new orientation data is available. Initiates activity switches based on phone orientation.
         */
        public void onOrientationEvent(final float yaw, final float pitch, final float roll) {

            if (activity.isFinishing()) return; // activity is finishing, we don't do anything anymore
                
//          Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: received event from orientation service");
            
            // pitch or roll value has changed
            if (pitch != _pitch || roll != _roll) {
                
                _pitch = pitch;
                _roll = roll;
                
                // switch to activity based on orientation
                final int type = PhotoCompassApplication.getActivityForOrientation(_pitch, _roll, activityType);
                if (type != activityType) {
                    final String newActivityName = (type == PhotoCompassApplication.CAMERA_ACTIVITY) ? "camera" : "map";
                    Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: switching to " + newActivityName +
                                                           " activity");
                    ProgressDialog.show(activity, "", "Switching to " + newActivityName + " view. Please wait...", true);
                    if (type == PhotoCompassApplication.CAMERA_ACTIVITY) {
                        activity.startActivity(new Intent(activity, CameraActivity.class));
                    } else {
                        if (PhotoCompassApplication.TARGET_PLATFORM == 3) {
                            activity.startActivity(new Intent(activity, PhotoMapActivity.class));
                        } else {
                            activity.startActivity(new Intent(activity, DummyMapActivity.class));
                        }
                    }
                    activity.finish(); // close this activity
                    System.gc(); // good moment to run the GC
                }
            }
            
            serviceActivity.onOrientationServiceOrientationEvent(yaw, pitch, roll); // run class specific code
        }
    };
    
    /**
     * Constructor.
     * 
     * @param activity Instance of the activity using this decorator.
     * @param serviceActivity Instance of the activity using this decorator.
     * @param activityType Type of activity. See {@link PhotoCompassApplication} for the defined types.
     */
    public ServiceConnections(final Activity activity, final IServiceActivity serviceActivity, final int activityType) {

        super();
        this.activity = activity;
        this.serviceActivity = serviceActivity;
        this.activityType = activityType;
    }
    
    /**
     * Connects to the services. Must be called in the onStart method of the using activity.
     */
    public void connectToServices() {

        // connect to settings service
        final Intent settingsServiceIntent = new Intent(activity, SettingsService.class);
        _boundToSettingsService = activity.bindService(settingsServiceIntent, _settingsServiceConn,
                                                       Context.BIND_AUTO_CREATE);
        if (!_boundToSettingsService) {
            Log.e(PhotoCompassApplication.LOG_TAG, "ServiceConnections: failed to connect to settings service");
        }
        
        // connect to location service
        final Intent locationServiceIntent = new Intent(activity, LocationService.class);
        _boundToLocationService = activity.bindService(locationServiceIntent, _locationServiceConn,
                                                       Context.BIND_AUTO_CREATE);
        if (!_boundToLocationService) {
            Log.e(PhotoCompassApplication.LOG_TAG, "ServiceConnections: failed to connect to location service");
        }
        
        // connect to orientation service
        final Intent orientationServiceIntent = new Intent(activity, OrientationService.class);
        _boundToOrientationService = activity.bindService(orientationServiceIntent, _orientationServiceConn,
                                                          Context.BIND_AUTO_CREATE);
        if (!_boundToOrientationService) {
            Log.e(PhotoCompassApplication.LOG_TAG, "ServiceConnections: failed to connect to orientation service");
        }
    }
    
    /**
     * Connects to the photos services. Is called when the settings service is connected and the {@link Settings}
     * instance is available. Package scoped for faster access by inner classes.
     */
    void connectToPhotosService() {

        // connect to photos service
        final Intent photosServiceIntent = new Intent(activity, PhotosService.class);
        _boundToPhotosService = activity.bindService(photosServiceIntent, _photosServiceConn, Context.BIND_AUTO_CREATE);
        if (!_boundToPhotosService) {
            Log.e(PhotoCompassApplication.LOG_TAG, "ServiceConnections: failed to connect to photos service");
        }
    }
    
    /**
     * Unregisters the callbacks from the services and then disconnects from the services. Must be called in the onStop
     * method of the using activity.
     */
    public void disconnectFromServices() {

        if (_boundToOrientationService) {
            
            // unregister from orientation service
            if (orientationService != null) {
                try {
                    orientationService.unregisterCallback(orientationServiceCallback);
                } catch (final RemoteException e) {
                    Log.w(PhotoCompassApplication.LOG_TAG,
                          "ServiceConnections: failed to unregister from orientation service");
                }
            }
            
            // disconnect from orientation service
            activity.unbindService(_orientationServiceConn);
            _boundToOrientationService = false;
        }
        
        if (_boundToLocationService) {
            
            // unregister from location service
            if (locationService != null) {
                try {
                    locationService.unregisterCallback(locationServiceCallback);
                } catch (final RemoteException e) {
                    Log.w(PhotoCompassApplication.LOG_TAG,
                          "ServiceConnections: failed to unregister from location service");
                }
            }
            
            // disconnect from location service
            activity.unbindService(_locationServiceConn);
            _boundToLocationService = false;
        }
        
        if (_boundToPhotosService) {
            
            // unregister from photos service
            if (photosService != null) {
                try {
                    photosService.unregisterCallback(photosServiceCallback);
                } catch (final RemoteException e) {
                    Log.w(PhotoCompassApplication.LOG_TAG,
                          "ServiceConnections: failed to unregister from photos service");
                }
            }
            
            // disconnect from photos service
            activity.unbindService(_photosServiceConn);
            _boundToPhotosService = false;
        }
        
        if (_boundToSettingsService) {
            
            // disconnect from settings service
            activity.unbindService(_settingsServiceConn);
            _boundToSettingsService = false;
        }
    }
    
    /**
     * Provides direct access to the instance of the photos service interface object.
     * 
     * @return {@link #photosService} instance.
     */
    IPhotosService getPhotosService() {

        return photosService;
    }
    
    /**
     * @return {@link Settings} instance from the settings service.
     */
    public Settings getSettings() {

        return settings;
    }
    
    /**
     * @param settings {@link Settings} instance to save to the settings service.
     */
    public void updateSettings(final Settings settings) {

        Log.d(PhotoCompassApplication.LOG_TAG, "ServiceConnections: updateSettings");
        
        if (settings == null) {
            Log.w(PhotoCompassApplication.LOG_TAG, "ServiceConnections: updateSettings: passed settings are null");
            return;
        }
        
        if (!_boundToSettingsService || settingsService == null) return;
        
        // safe settings
        try {
            settingsService.updateSettings(settings);
        } catch (final RemoteException e) {
            Log.w(PhotoCompassApplication.LOG_TAG, "ServiceConnections: failed to save settings to settings service");
        }
    }
}
