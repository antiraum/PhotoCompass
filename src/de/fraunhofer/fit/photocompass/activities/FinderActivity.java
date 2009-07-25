package de.fraunhofer.fit.photocompass.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
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
import de.fraunhofer.fit.photocompass.views.CompassView;
import de.fraunhofer.fit.photocompass.views.ControlsView;
import de.fraunhofer.fit.photocompass.views.FinderView;
import de.fraunhofer.fit.photocompass.views.PhotosView;

/**
 * This class is the Activity component for the camera view screen (phone held vertically) of the application.
 */
public final class FinderActivity extends Activity {
    
    private static final int BOTTOM_CONTROLS_HEIGHT = 25 + ControlsView.CONTROL_SIDE_PADDING +
                                                      ControlsView.BOTTOM_EXTRA_PADDING;
    
    FinderActivity finderActivity; // package scoped for faster access by inner classes
    
    double currentLat = 0; // package scoped for faster access by inner classes
    double currentLng = 0; // package scoped for faster access by inner classes
    double currentAlt = 0; // package scoped for faster access by inner classes
    float currentYaw = 0; // package scoped for faster access by inner classes
    
    private CompassView _compassView;
    private PhotosView _photosView;
    private long _lastCompassViewUpdate;
    private long _lastPhotoViewUpdate;
    private static final int COMPASS_VIEW_UPDATE_IVAL = 300; // in milliseconds
    private static final int PHOTO_VIEW_UPDATE_IVAL = 300; // in milliseconds
    
    ILocationService locationService = null; // package scoped for faster access by inner classes
    private boolean _boundToLocationService = false;
    IOrientationService orientationService = null; // package scoped for faster access by inner classes
    private boolean _boundToOrientationService = false;
    
    private final Photos _photosModel = Photos.getInstance();
    private final ApplicationModel _appModel = ApplicationModel.getInstance();
    

    /**
     * Connection object for the connection with the {@link LocationService}.
     */
    private final ServiceConnection _locationServiceConn = new ServiceConnection() {
        
        /**
         * Gets called when the service connection is established. Creates the {@link #locationService} object from the
         * service interface and registers the {@link #locationServiceCallback}.
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
         * Gets called when the service connection is closed down. Frees {@link #locationService}.
         */
        public void onServiceDisconnected(final ComponentName name) {

            Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: disconnected from location service");
            locationService = null;
        }
    };
    

    /**
     * Callback object for the {@link LocationService}. Gets registered and unregistered at the {@link #locationService}
     * object. Package scoped for faster access by inner classes.
     */
    final ILocationServiceCallback locationServiceCallback = new ILocationServiceCallback.Stub() {
        
        /**
         * Gets called when new data is provided by the {@link LocationService}. Stores the new location and initiates
         * an update of the map view.
         */
        public void onLocationEvent(final double lat, final double lng, final boolean hasAlt, final double alt) {

            if (isFinishing()) return; // activity is finishing, we don't do anything anymore
                
            // Check the distance between last and new location and only update if greater than the minimum
            // update distance. Otherwise we get too many updates because of invalid altitude values. 
            final float[] results = new float[1];
            Location.distanceBetween(currentLat, currentLng, lat, lng, results);
            if (results[0] < LocationService.MIN_LOCATION_UPDATE_DISTANCE) return;
            
            Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onLocationEvent: lat = " + lat + ", lng = " + lng +
                                                   ", alt = " + alt);
            Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onLocationEvent: changed by " + results[0] +
                                                   " meters");
            
            final boolean latChanged = (lat == currentLat) ? false : true;
            final boolean lngChanged = (lng == currentLng) ? false : true;
            final boolean altChanged = (!hasAlt || alt == currentAlt) ? false : true;
            
            // update variables
            currentLat = lat;
            currentLng = lng;
            if (hasAlt) currentAlt = alt;
            
            // update compass view
            updateCompassView();
            
            // update photo view
            updatePhotoView(latChanged, lngChanged, altChanged, false, false, false);
        }
    };
    

    /**
     * Connection object for the connection with the {@link OrientationService}.
     */
    private final ServiceConnection _orientationServiceConn = new ServiceConnection() {
        
        /**
         * Gets called when the service connection is established. Creates the {@link #orientationService} object from
         * the service interface and registers the {@link #orientationServiceCallback}.
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
         * Gets called when the service connection is closed down. Frees {@link #orientationService}.
         */
        public void onServiceDisconnected(final ComponentName name) {

            Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: disconnected from orientation service");
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
         * Gets called when new data is provided by the {@link OrientationService}. Initiates switch to
         * {@link PhotoMapActivity} when the phone is held horizontally. Also updates the photos and compass views when
         * the yaw value changed.
         */
        public void onOrientationEvent(final float yaw, final float pitch, final float roll) {

//	    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: received event from orientation service");
            
            if (isFinishing()) return; // activity is finishing, we don't do anything anymore
                
            // pitch or roll value has changed
            if (pitch != _pitch || roll != _roll) {
                _pitch = pitch;
                _roll = roll;
                
                // switch to activity based on orientation
                final int activity = PhotoCompassApplication.getActivityForOrientation(
                                                                                       _pitch,
                                                                                       _roll,
                                                                                       PhotoCompassApplication.FINDER_ACTIVITY);
                if (activity == PhotoCompassApplication.MAP_ACTIVITY) {
                    Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: switching to map activity");
                    ProgressDialog.show(finderActivity, "", "Switching to map view. Please wait...", true);
                    if (PhotoCompassApplication.TARGET_PLATFORM == 3)
                        startActivity(new Intent(finderActivity, PhotoMapActivity.class));
                    else
                        startActivity(new Intent(finderActivity, DummyMapActivity.class));
                    finish(); // close this activity
                    System.gc(); // good point to run the GC
                }
            }
            
            // yaw value has changed
            if (currentYaw != yaw) {
                
                // update variable
                currentYaw = yaw;
                
                // update compass view
                updateCompassView();
                
                // update photo view
                updatePhotoView(false, false, false, true, false, false);
            }
        }
    };
    

    /**
     * Callback object for the {@link ApplicationModel}. Gets registered and unregistered at the {@link #_appModel}
     * object.
     */
    private final IApplicationModelCallback _appModelCallback = new IApplicationModelCallback.Stub() {
        
        /**
         * Gets called when the minimum distance in the {@link ApplicationModel} changes. Initiates a update of
         * {@link #_photosView}.
         */
        public void onMinDistanceChange(final float minDistance, final float minDistanceRel) {

            if (isFinishing()) return; // activity is finishing, we don't do anything anymore
                
            updatePhotoView(false, false, false, false, true, true);
        }
        

        /**
         * Gets called when the maximum distance in the {@link ApplicationModel} changes. Initiates a update of
         * {@link #_photosView}.
         */
        public void onMaxDistanceChange(final float maxDistance, final float maxDistanceRel) {

            if (isFinishing()) return; // activity is finishing, we don't do anything anymore
                
            updatePhotoView(false, false, false, false, true, true);
        }
        

        /**
         * Gets called when the minimum age in the {@link ApplicationModel} changes. Initiates a update of
         * {@link #_photosView}.
         */
        public void onMinAgeChange(final long minAge, final float minAgeRel) {

            if (isFinishing()) return; // activity is finishing, we don't do anything anymore
                
            updatePhotoView(false, false, false, false, true, true);
        }
        

        /**
         * Gets called when the maximum age in the {@link ApplicationModel} changes. Initiates a update of
         * {@link #_photosView}.
         */
        public void onMaxAgeChange(final long maxAge, final float maxAgeRel) {

            if (isFinishing()) return; // activity is finishing, we don't do anything anymore
                
            updatePhotoView(false, false, false, false, true, true);
        }
    };
    
    
    /**
     * Constructor.
     */
    public FinderActivity() {

        super();
        finderActivity = this;
        
        // register as application model callback
        _appModel.registerCallback(_appModelCallback);
    }
    

    /**
     * Called when the activity is first created. Initializes the views.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {

        Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        _photosModel.initialize(this);
        
        // initialize views
        final FinderView finderView = new FinderView(this);
        final int availableHeight = PhotoCompassApplication.DISPLAY_HEIGHT - PhotoCompassApplication.STATUSBAR_HEIGHT;
        _compassView = new CompassView(this, PhotoCompassApplication.DISPLAY_WIDTH, availableHeight -
                                                                                    BOTTOM_CONTROLS_HEIGHT);
        _photosView = new PhotosView(this, PhotoCompassApplication.DISPLAY_WIDTH, availableHeight -
                                                                                  BOTTOM_CONTROLS_HEIGHT);
        final ControlsView controlsView = new ControlsView(this, PhotoCompassApplication.DISPLAY_WIDTH,
                                                           availableHeight, true, true, false);
        
        // setup views
        setContentView(finderView);
        addContentView(_compassView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        addContentView(_photosView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        addContentView(controlsView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }
    

    /**
     * Called before the activity becomes visible. Connects to the {@link LocationService} and the
     * {@link OrientationService}. Initiates a update of the {@link Photos} model.
     */
    @Override
    public void onStart() {

        Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onStart");
        super.onStart();
        
        // connect to location service
        final Intent locationServiceIntent = new Intent(this, LocationService.class);
        _boundToLocationService = bindService(locationServiceIntent, _locationServiceConn, Context.BIND_AUTO_CREATE);
        if (!_boundToLocationService) Log.e(PhotoCompassApplication.LOG_TAG, "failed to connect to location service");
        
        // connect to orientation service
        final Intent orientationServiceIntent = new Intent(this, OrientationService.class);
        _boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn,
                                                 Context.BIND_AUTO_CREATE);
        if (!_boundToOrientationService)
            Log.e(PhotoCompassApplication.LOG_TAG, "failed to connect to orientation service");
        
        // let photos model check if the available photos have changed
        _photosModel.updatePhotos(this);
    }
    

    /**
     * Called when the activity is no longer visible. Unregisters the callbacks from the services and then disconnects
     * from the services.
     */
    @Override
    public void onStop() {

        Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onStop");
        
        if (_boundToLocationService) {
            
            // unregister from location service
            if (locationService != null) try {
                locationService.unregisterCallback(locationServiceCallback);
            } catch (final DeadObjectException e) {
                Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: location service has crashed");
            } catch (final RemoteException e) {
                Log.w(PhotoCompassApplication.LOG_TAG, "FinderActivity: failed to unregister from location service");
            }
            
            // disconnect from location service
            unbindService(_locationServiceConn);
            _boundToLocationService = false;
        }
        
        if (_boundToOrientationService) {
            
            // unregister from orientation service
            if (orientationService != null)
                try {
                    orientationService.unregisterCallback(orientationServiceCallback);
                } catch (final DeadObjectException e) {
                    Log.e(PhotoCompassApplication.LOG_TAG, "FinderActivity: orientation service has crashed");
                } catch (final RemoteException e) {
                    Log.w(PhotoCompassApplication.LOG_TAG,
                          "FinderActivity: failed to unregister from orientation service");
                }
            
            // disconnect from orientation service
            unbindService(_orientationServiceConn);
            _boundToOrientationService = false;
        }
        
        super.onStop();
    }
    

    /**
     * This is called when the overall system is running low on memory, and would like actively running process to try
     * to tighten their belt. We are nice and clear the unneeded photo and border views in the photos view.
     */
    @Override
    public void onLowMemory() {

        Log.d(PhotoCompassApplication.LOG_TAG, "FinderActivity: onLowMemory");
        _photosView.clearUnneededViews();
    }
    

    /**
     * Updates the compass view based on the current viewing direction.
     */
    void updateCompassView() {

        // redraw the view only if last redraw older than COMPASS_VIEW_UPDATE_IVAL
        if (SystemClock.uptimeMillis() - _lastCompassViewUpdate < COMPASS_VIEW_UPDATE_IVAL) return;
        _lastCompassViewUpdate = SystemClock.uptimeMillis();
        
        _compassView.update(currentYaw);
    }
    

    /**
     * Updates the photo view based on the current location and phone orientation as well as the settings in the
     * {@link ApplicationModel}. Package scoped for faster access by inner classes.
     * 
     * @param latChanged If current latitude has changed.
     * @param lngChanged If current longitude has changed.
     * @param altChanged If current altitude has changed.
     * @param yawChanged If current yaw has changed.
     * @param modelChanged If the application model has changed.
     * @param forceRedraw Force redraw of the view. Otherwise the photos view update interval is respected.
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
            
            doRedrawHere = false; // we do other updates first 
            
            // update photos
            if (latChanged || lngChanged) _photosModel.updatePhotoProperties(currentLat, currentLng, currentAlt);
            _photosView.addPhotos(_photosModel.getNewlyVisiblePhotos(_photosView.photos, true, true), doRedrawHere);
            _photosView.removePhotos(_photosModel.getNoLongerVisiblePhotos(_photosView.photos, true, true));
        }
        
        if (latChanged || lngChanged || altChanged) {
            
            doRedrawHere = false; // we do other updates first 
            
            // update photo text informations
            if (altChanged && !latChanged && !lngChanged) _updateCurrentPhotosProperties(); // already did update on latChanged/lngChanged
            _photosView.updateTextInfos(doRedrawHere);
        }
        
        if (latChanged || lngChanged || modelChanged) {
            
            doRedrawHere = forceRedraw;
            
            // update sizes of the photos
            _photosView.updateSizes(doRedrawHere);
        }
        
        if (altChanged) {
            
            doRedrawHere = forceRedraw;
            
            // update y positions of the photos
            _photosView.updateYPositions(doRedrawHere);
        }
        
        if (yawChanged) {
            
            doRedrawHere = forceRedraw;
            
            // update x positions of the photos
            _photosView.updateXPositions(currentYaw, doRedrawHere);
        }
    }
    

    /**
     * Updates distance, direction, and altitude offset of the photos currently used by the photos view.
     */
    private void _updateCurrentPhotosProperties() {

        Photo photo;
        for (final int id : _photosView.photos) {
            photo = _photosModel.getPhoto(id);
            if (photo != null) photo.updateDistanceDirectionAndAltitudeOffset(currentLat, currentLng, currentAlt);
        }
        _photosModel.updateAppModelMaxValues();
    }
    

    /**
     * Populate the options menu.
     */
//    public boolean onCreateOptionsMenu(Menu menu) {
//    	menu = OptionsMenu.populateMenu(menu);
//        return true;
//    }
    
    /**
     * Handles the option menu item selections.
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        return OptionsMenu.handleMenuItemSelection(item, this);
    }
    

    /**
     * Gets called when a started {@link Activity} returns a result.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        if (requestCode == OptionsMenu.CAMERA_RETURN) {
            if (resultCode == RESULT_OK) {
                // FIXME at the moment the photo isn't saved - either we have to do this on our own, or
                // we can call the camera application in another way
//            	Photos.getInstance().updatePhotos(this);
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
