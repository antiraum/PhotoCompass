package de.fraunhofer.fit.photocompass.activities;

import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.Settings;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.services.IPhotosService;
import de.fraunhofer.fit.photocompass.util.ListArrayConversions;
import de.fraunhofer.fit.photocompass.views.ControlsView;
import de.fraunhofer.fit.photocompass.views.layouts.RotateView;
import de.fraunhofer.fit.photocompass.views.overlays.CurrentLocationOverlay;
import de.fraunhofer.fit.photocompass.views.overlays.PhotosOverlay;
import de.fraunhofer.fit.photocompass.views.overlays.ViewingDirectionOverlay;

/**
 * This class is the Activity component for the map view screen (phone held horizontally) of the application.
 */
public final class PhotoMapActivity extends MapActivity implements IServiceActivity {
    
    private static final String MAPS_API_KEY = "02LUNbs-0sTLfQE-JAZ78GXgqz8fRSthtLjrfBw";
    
    /**
     * Decorator that handles the connections to the service components.
     */
    private final ServiceConnections _serviceConnections = new ServiceConnections(this, this,
                                                                                  PhotoCompassApplication.MAP_ACTIVITY);
    
    private double _currentLat = 0;
    private double _currentLng = 0;
    private double _currentAlt = 0;
    private float _currentYaw = 0;
    
    // map view
    private RotateView _rotateView;
    private MapView _mapView;
    private MapController _mapController;
    
    // overlays
    private ViewingDirectionOverlay _viewDirOverlay;
    private CurrentLocationOverlay _customMyLocOverlay;
    private PhotosOverlay _photosOverlay;
    
    private ControlsView _controlsView = null;
    
    /**
     * Constructor.
     */
    public PhotoMapActivity() {

        super();
    }
    
    /**
     * Called when the activity is first created. Initializes the views and map components.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // map view
        _mapView = new MapView(this, MAPS_API_KEY);
        _mapView.setClickable(false); // disable panning
        _mapView.setEnabled(true);
//		_mapView.setBuiltInZoomControls(true); // disabled by purpose (comment this line for target 1 compatibility)
//		setContentView(_mapView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        _rotateView = new RotateView(this);
        _rotateView.addView(_mapView);
        setContentView(_rotateView);
        
        // controls view
        _controlsView = new ControlsView(this, this, PhotoCompassApplication.DISPLAY_WIDTH,
                                         PhotoCompassApplication.DISPLAY_HEIGHT -
                                                 PhotoCompassApplication.STATUSBAR_HEIGHT, false, true, true);
        addContentView(_controlsView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        
        // viewing direction overlay
        _viewDirOverlay = new ViewingDirectionOverlay();
        final List<Overlay> overlays = _mapView.getOverlays();
        overlays.add(_viewDirOverlay);
        
        // own current position overlay
        _customMyLocOverlay = new CurrentLocationOverlay();
        overlays.add(_customMyLocOverlay);
        
        // photos overlay
        _photosOverlay = new PhotosOverlay(this);
        overlays.add(_photosOverlay);
        
        // initialize map controller
        _mapController = _mapView.getController();
        _mapController.setZoom(15);
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
     * to tighten their belt. We are nice and clear the unneeded bitmaps in the photos overlay.
     */
    @Override
    public void onLowMemory() {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onLowMemory");
        _photosOverlay.clearUnneededBitmaps();
    }
    
    /**
     * Tell the Google server that we are not displaying any kind of route information.
     */
    @Override
    protected boolean isRouteDisplayed() {

        return false;
    }
    
    /**
     * Updates the map view based on the current location. Centers the map to the location and updates the photo
     * overlays. Package scoped for faster access by inner classes.
     * 
     * @param latChanged If current latitude has changed.
     * @param lngChanged If current longitude has changed.
     * @param yawChanged If current yaw orientation has changed.
     * @param modelChanged If the application model has changed.
     */
    void updateMapView(final boolean latChanged, final boolean lngChanged, final boolean yawChanged,
                       final boolean modelChanged) {

//        Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: updateMapView");
        
        if (latChanged || lngChanged) {
            
            // center map view
            final GeoPoint currentLocation = new GeoPoint((int) (_currentLat * 1E6), (int) (_currentLng * 1E6));
            _mapController.animateTo(currentLocation);
            
            // update overlays
            _viewDirOverlay.updateLocation(currentLocation);
            _customMyLocOverlay.update(currentLocation);
        }
        
        if (modelChanged) {

            // TODO set zoom according to radius of displayed photos
//    		_mapController.zoomToSpan(latSpanE6, lngSpanE6);
        }
        
        if (latChanged || lngChanged || modelChanged) {
            
            final IPhotosService photosService = _serviceConnections.getPhotosService();
            
            if (photosService == null) {
                // photos service is not (yet) initialized, this may happen at the beginning of the life cycle
//                Log.w(PhotoCompassApplication.LOG_TAG,
//                      "PhotoMapActivity: updatePhotoView: photos service not initialized");
            } else {
                
                // update photos
                try {
                    if (latChanged || lngChanged) {
                        updateSettings(photosService.updatePhotoProperties(getSettings(), _currentLat, _currentLng,
                                                                           _currentAlt));
                    }
                    final int[] newPhotos = photosService.getNewlyVisiblePhotos(
                                                                                getSettings(),
                                                                                ListArrayConversions.intListToPrimitives(_photosOverlay.photos),
                                                                                false, true);
                    final int[] oldPhotos = photosService.getNoLongerVisiblePhotos(
                                                                                   getSettings(),
                                                                                   ListArrayConversions.intListToPrimitives(_photosOverlay.photos),
                                                                                   false, true);
                    _photosOverlay.addPhotos(ListArrayConversions.intArrayToList(newPhotos));
                    _photosOverlay.removePhotos(ListArrayConversions.intArrayToList(oldPhotos));
                } catch (final RemoteException e) {
                    Log.w(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to call photos service");
                }
            }
        }
        
        if (yawChanged) {
            
            // update overlays
            _photosOverlay.updateDirection(_currentYaw);
            _viewDirOverlay.updateDirection(_currentYaw);
        }
        
        // redraw map
        _mapView.postInvalidate();
        
        // rotate
        if (yawChanged) {
            _rotateView.setHeading(_currentYaw);
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
            Log.e(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: getPhoto: photos service not initialized");
            return null;
        }
        
        try {
            return photosService.getPhoto(id);
        } catch (final RemoteException e) {
            Log.w(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: failed to call photos service");
        }
        
        return null;
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onPhotosServicePhotoDistancesChange(float[])
     */
    @Override
    public void onPhotosServicePhotoDistancesChange(final float[] photoDistances) {

    // no distance slider on the controls view so we don't pass the values to the controls view
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
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMinDistanceChange(float, float)
     */
    @Override
    public void onSettingsServiceMinDistanceChange(final float minDistance, final float minDistanceRel) {

        // initiate update of the map view
        updateMapView(false, false, false, true);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMaxDistanceChange(float, float)
     */
    @Override
    public void onSettingsServiceMaxDistanceChange(final float maxDistance, final float maxDistanceRel) {

        // initiate update of the map view
        updateMapView(false, false, false, true);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMinAgeChange(long, float)
     */
    @Override
    public void onSettingsServiceMinAgeChange(final long minAge, final float minAgeRel) {

        // initiate update of the map view
        updateMapView(false, false, false, true);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMaxAgeChange(long, float)
     */
    @Override
    public void onSettingsServiceMaxAgeChange(final long maxAge, final float maxAgeRel) {

        // initiate update of the map view
        updateMapView(false, false, false, true);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onLocationServiceLocationEvent(double, double,
     *      boolean, double)
     */
    @Override
    public void onLocationServiceLocationEvent(final double lat, final double lng, final boolean hasAlt,
                                               final double alt) {

        // detect changes
        final boolean latChanged = (lat == _currentLat) ? false : true;
        final boolean lngChanged = (lng == _currentLng) ? false : true;
        
        // update variables
        _currentLat = lat;
        _currentLng = lng;
        if (hasAlt) {
            _currentAlt = alt;
        }
        
        // update map view
        updateMapView(latChanged, lngChanged, false, false);
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onOrientationServiceOrientationEvent(float,
     *      float, float)
     */
    @Override
    public void onOrientationServiceOrientationEvent(final float yaw, final float pitch, final float roll) {

        // detect changes
        final boolean yawChanged = (yaw == _currentYaw) ? false : true;
        
        // update variables
        _currentYaw = yaw;
        
        // update map view
        updateMapView(false, false, yawChanged, false);
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

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onActivityResult");
        if (requestCode == OptionsMenu.CAMERA_RETURN) {
            if (resultCode == RESULT_OK) {
                Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onActivityResult CAMERA_RETURN RESULT_OK");
                // FIXME at the moment the photo isn't saved - either we have to do this on our own, or
                // we can call the camera application in another way
//                Bitmap bitmap = (Bitmap) data.getParcelableExtra("data");
//                if (bitmap != null) {
//                	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoMapActivity: onActivityResult: we have a bmp");
//                }
//            	Photos.getInstance().updatePhotos(this);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
