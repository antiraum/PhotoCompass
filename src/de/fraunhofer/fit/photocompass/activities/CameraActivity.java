package de.fraunhofer.fit.photocompass.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.Settings;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.services.IPhotosService;
import de.fraunhofer.fit.photocompass.services.SettingsService;
import de.fraunhofer.fit.photocompass.util.ListArrayConversions;
import de.fraunhofer.fit.photocompass.views.CameraView;
import de.fraunhofer.fit.photocompass.views.CompassView;
import de.fraunhofer.fit.photocompass.views.ControlsView;
import de.fraunhofer.fit.photocompass.views.PhotosView;

/**
 * This class is the Activity component for the camera view screen (phone held vertically) of the application.
 */
public final class CameraActivity extends Activity implements IServiceActivity {
    
    /**
     * Height of the controls on the bottom of the screen.
     */
    private static final int BOTTOM_CONTROLS_HEIGHT = 25 + ControlsView.CONTROL_SIDE_PADDING +
                                                      ControlsView.BOTTOM_EXTRA_PADDING;
    
    private static final int COMPASS_VIEW_UPDATE_IVAL = 300; // in milliseconds
    private static final int PHOTO_VIEW_UPDATE_IVAL = 300; // in milliseconds
    
    /**
     * Decorator that handles the connections to the service components.
     */
    private final ServiceConnections _serviceConnections = new ServiceConnections(
                                                                                  this,
                                                                                  this,
                                                                                  PhotoCompassApplication.CAMERA_ACTIVITY);
    
    private double _currentLat = 0;
    private double _currentLng = 0;
    private double _currentAlt = 0;
    private float _currentYaw = 0;
    
    private CompassView _compassView;
    private PhotosView _photosView;
    private long _lastCompassViewUpdate;
    private long _lastPhotoViewUpdate;
    private ControlsView _controlsView = null;
    
    /**
     * Constructor.
     */
    public CameraActivity() {

        super();
    }
    
    /**
     * Called when the activity is first created. Initializes the views.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {

        Log.d(PhotoCompassApplication.LOG_TAG, "CameraActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // initialize views
        final CameraView cameraView = new CameraView(this);
        final int availableHeight = PhotoCompassApplication.DISPLAY_HEIGHT - PhotoCompassApplication.STATUSBAR_HEIGHT;
        _compassView = new CompassView(this, PhotoCompassApplication.DISPLAY_WIDTH, availableHeight -
                                                                                    BOTTOM_CONTROLS_HEIGHT);
        _photosView = new PhotosView(this, PhotoCompassApplication.DISPLAY_WIDTH, availableHeight -
                                                                                  BOTTOM_CONTROLS_HEIGHT);
        _controlsView = new ControlsView(this, this, PhotoCompassApplication.DISPLAY_WIDTH, availableHeight, true,
                                         true, false);
        
        // setup views
        setContentView(cameraView);
        addContentView(_compassView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        addContentView(_photosView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        addContentView(_controlsView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }
    
    /**
     * Called before the activity becomes visible. Initiates connections to the services.
     */
    @Override
    public void onStart() {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onStart");
        super.onStart();
        
        _serviceConnections.connectToServices();
        
        if (_controlsView != null) {
            updateSettings(_controlsView.registerToSettings(getSettings()));
        }
    }
    
    /**
     * Called when the activity is no longer visible. Initiates disconnects from the services.
     */
    @Override
    public void onStop() {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onStop");
        
        if (_controlsView != null) {
            updateSettings(_controlsView.unregisterFromSettings(getSettings()));
        }
        
        _serviceConnections.disconnectFromServices();
        
        super.onStop();
    }
    
    /**
     * This is called when the overall system is running low on memory, and would like actively running process to try
     * to tighten their belt. We are nice and clear the unneeded photo and border views in the photos view.
     */
    @Override
    public void onLowMemory() {

        Log.d(PhotoCompassApplication.LOG_TAG, "CameraActivity: onLowMemory");
        _photosView.clearUnneededViews();
    }
    
    /**
     * Updates the compass view based on the current viewing direction.
     */
    void updateCompassView() {

        // redraw the view only if last redraw older than COMPASS_VIEW_UPDATE_IVAL
        if (SystemClock.uptimeMillis() - _lastCompassViewUpdate < COMPASS_VIEW_UPDATE_IVAL) return;
        _lastCompassViewUpdate = SystemClock.uptimeMillis();
        
        _compassView.update(_currentYaw);
    }
    
    /**
     * Updates the photo view based on the current location and phone orientation as well as the settings in the
     * {@link SettingsService}. Package scoped for faster access by inner classes.
     * 
     * @param latChanged If current latitude has changed.
     * @param lngChanged If current longitude has changed.
     * @param altChanged If current altitude has changed.
     * @param yawChanged If current yaw has changed.
     * @param modelChanged If the application model has changed.
     * @param forceRedraw Force redraw of the view. Otherwise the photos view update interval is respected.
     */
    void updatePhotosView(final boolean latChanged, final boolean lngChanged, final boolean altChanged,
                          final boolean yawChanged, final boolean modelChanged, boolean forceRedraw) {

//        Log.d(PhotoCompassApplication.LOG_TAG, "CameraActivity: updatePhotosView: forceRedraw = " + forceRedraw);
        
        // redraw the view only if either forced or last redraw older than PHOTO_VIEW_UPDATE_IVAL
        if (forceRedraw || SystemClock.uptimeMillis() - _lastPhotoViewUpdate > PHOTO_VIEW_UPDATE_IVAL) {
            forceRedraw = true;
            _lastPhotoViewUpdate = SystemClock.uptimeMillis();
        }
        
        boolean doRedrawHere;
        if (latChanged || lngChanged || modelChanged) {
            
            final IPhotosService photosService = _serviceConnections.getPhotosService();
            
            if (photosService == null) {
                // photos service is not (yet) initialized, this may happen at the beginning of the life cycle
//                Log.w(PhotoCompassApplication.LOG_TAG,
//                      "CameraActivity: updatePhotoView: photos service not initialized");
            } else {
                
                doRedrawHere = false; // we do other updates first
                
                // update photos
                try {
                    if (latChanged || lngChanged) {
                        updateSettings(photosService.updatePhotoProperties(getSettings(), _currentLat, _currentLng,
                                                                           _currentAlt));
                    }
                    final int[] newPhotos = photosService.getNewlyVisiblePhotos(
                                                                                getSettings(),
                                                                                ListArrayConversions.intListToPrimitives(_photosView.photos),
                                                                                true, true);
                    final int[] oldPhotos = photosService.getNoLongerVisiblePhotos(
                                                                                   getSettings(),
                                                                                   ListArrayConversions.intListToPrimitives(_photosView.photos),
                                                                                   true, true);
                    _photosView.addPhotos(ListArrayConversions.intArrayToList(newPhotos), doRedrawHere);
                    _photosView.removePhotos(ListArrayConversions.intArrayToList(oldPhotos));
                } catch (final RemoteException e) {
                    Log.w(PhotoCompassApplication.LOG_TAG, "CameraActivity: failed to call photos service");
                }
            }
        }
        
        if (latChanged || lngChanged || altChanged) {
            
            doRedrawHere = false; // we do other updates first
            
            // update photo text informations
            if (altChanged && !latChanged && !lngChanged) {
                _updateCurrentPhotosProperties(); // already did update on latChanged/lngChanged
            }
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
            _photosView.updateXPositions(_currentYaw, doRedrawHere);
        }
    }
    
    /**
     * Updates distance, direction, and altitude offset of the photos currently used by the photos view.
     */
    private void _updateCurrentPhotosProperties() {

        Log.d(PhotoCompassApplication.LOG_TAG, "CameraActivity: _updateCurrentPhotosProperties");
        
        final IPhotosService photosService = _serviceConnections.getPhotosService();
        
        if (photosService == null) {
            Log.e(PhotoCompassApplication.LOG_TAG,
                  "CameraActivity: _updateCurrentPhotosProperties: photos service not initialized");
            return;
        }
        
        try {
            Photo photo;
            for (final int id : _photosView.photos) {
                photo = photosService.getPhoto(id);
                if (photo == null) {
                    continue;
                }
                photo.updateDistanceDirectionAndAltitudeOffset(_currentLat, _currentLng, _currentAlt);
            }
            updateSettings(photosService.updateAppModelMaxValues(getSettings()));
        } catch (final RemoteException e) {
            Log.w(PhotoCompassApplication.LOG_TAG, "CameraActivity: failed to call photos service");
        }
    }
    
    /**
     * Wrapper method to access the getPhoto method of the photo service from the views.
     * 
     * @param id Id of the requested photo (photo id for MediaStore photos; resource id for dummy photos).
     * @return <code>{@link Photo}</code> if the photo is known, or <code>null</code> if the photo is not known.
     */
    public Photo getPhoto(final int id) {

        final IPhotosService photosService = _serviceConnections.getPhotosService();
        
        if (photosService == null) {
            Log.e(PhotoCompassApplication.LOG_TAG, "CameraActivity: getPhoto: photos service not initialized");
            return null;
        }
        
        try {
            return photosService.getPhoto(id);
        } catch (final RemoteException e) {
            Log.w(PhotoCompassApplication.LOG_TAG, "CameraActivity: failed to call photos service");
        }
        
        return null;
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onLocationServiceLocationEvent(double, double,
     *      boolean, double)
     */
    @Override
    public void onLocationServiceLocationEvent(final double lat, final double lng, final boolean hasAlt,
                                               final double alt) {

        // detect what has changed
        final boolean latChanged = (lat == _currentLat) ? false : true;
        final boolean lngChanged = (lng == _currentLng) ? false : true;
        final boolean altChanged = (!hasAlt || alt == _currentAlt) ? false : true;
        
        // update location variables
        _currentLat = lat;
        _currentLng = lng;
        if (hasAlt) {
            _currentAlt = alt;
        }
        
        // update compass view
        updateCompassView();
        
        // update photo view
        updatePhotosView(latChanged, lngChanged, altChanged, false, false, false);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onOrientationServiceOrientationEvent(float,
     *      float, float)
     */
    @Override
    public void onOrientationServiceOrientationEvent(final float yaw, final float pitch, final float roll) {

        if (_currentYaw == yaw) return;
        
        // update variable
        _currentYaw = yaw;
        
        // update compass view
        updateCompassView();
        
        // update photo view
        updatePhotosView(false, false, false, true, false, false);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onPhotosServicePhotoAgesChange(float[])
     */
    @Override
    public void onPhotosServicePhotoAgesChange(final float[] photoAges) {

        if (_controlsView == null) return;
        
        // pass the values to the controls view
        _controlsView.setPhotoAges(photoAges);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onPhotosServicePhotoDistancesChange(float[])
     */
    @Override
    public void onPhotosServicePhotoDistancesChange(final float[] photoDistances) {

        if (_controlsView == null) return;
        
        // pass the values to the controls view
        _controlsView.setPhotoDistances(photoDistances);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMaxAgeChange(long, float)
     */
    @Override
    public void onSettingsServiceMaxAgeChange(final long maxAge, final float maxAgeRel) {

        // initiates an update of the photos view
        updatePhotosView(false, false, false, false, true, true);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMaxDistanceChange(float, float)
     */
    @Override
    public void onSettingsServiceMaxDistanceChange(final float maxDistance, final float maxDistanceRel) {

        // initiates an update of the photos view
        updatePhotosView(false, false, false, false, true, true);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMinAgeChange(long, float)
     */
    @Override
    public void onSettingsServiceMinAgeChange(final long minAge, final float minAgeRel) {

        // initiates an update of the photos view
        updatePhotosView(false, false, false, false, true, true);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMinDistanceChange(float, float)
     */
    @Override
    public void onSettingsServiceMinDistanceChange(final float minDistance, final float minDistanceRel) {

        // initiates an update of the photos view
        updatePhotosView(false, false, false, false, true, true);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#getSettings()
     */
    @Override
    public Settings getSettings() {

        return _serviceConnections.getSettings();
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#updateSettings(de.fraunhofer.fit.photocompass.model.Settings)
     */
    @Override
    public void updateSettings(final Settings settings) {

        _serviceConnections.updateSettings(settings);
    }
    
    /**
     * Populate the options menu.
     */
//    public boolean onCreateOptionsMenu(Menu menu) {
//      menu = OptionsMenu.populateMenu(menu);
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
//              Photos.getInstance().updatePhotos(this);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
