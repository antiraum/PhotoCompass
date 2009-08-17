package de.fraunhofer.fit.photocompass.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.Settings;

/**
 * This class is a dummy replacement of the {@link PhotoMapActivity} for running the application on a platform without
 * Google libraries. If this class or {@link PhotoMapActivity} is used is controlled by the
 * {@link PhotoCompassApplication#TARGET_PLATFORM} constant.
 */
public final class DummyMapActivity extends Activity implements IServiceActivity {
    
    /**
     * Decorator that handles the connections to the service components.
     */
    private final ServiceConnections _serviceConnections = new ServiceConnections(this, this,
                                                                                  PhotoCompassApplication.MAP_ACTIVITY);
    
    /**
     * Constructor.
     */
    public DummyMapActivity() {

        super();
    }
    
    /**
     * Called when the activity is first created. Initializes the views.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {

        Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        // setup view
        setContentView(R.layout.dummymap_layout);
    }
    
    /**
     * Called before the activity becomes visible. Initiates connections to the services.
     */
    @Override
    public void onStart() {

        Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: onStart");
        super.onStart();
        
        _serviceConnections.connectToServices();
    }
    
    /**
     * Called when the activity is no longer visible. Initiates disconnects from the services.
     */
    @Override
    public void onStop() {

        Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: onStop");
        
        _serviceConnections.disconnectFromServices();
        
        super.onStop();
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onLocationServiceLocationEvent(double, double,
     *      boolean, double)
     */
    @Override
    public void onLocationServiceLocationEvent(final double lat, final double lng, final boolean hasAlt,
                                               final double alt) {

    // nothing to do here
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onOrientationServiceOrientationEvent(float,
     *      float, float)
     */
    @Override
    public void onOrientationServiceOrientationEvent(final float yaw, final float pitch, final float roll) {

    // nothing to do here
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onPhotosServicePhotoAgesChange(float[])
     */
    @Override
    public void onPhotosServicePhotoAgesChange(final float[] photoAges) {

    // nothing to do here
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onPhotosServicePhotoDistancesChange(float[])
     */
    @Override
    public void onPhotosServicePhotoDistancesChange(final float[] photoDistances) {

    // nothing to do here
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMaxAgeChange(long, float)
     */
    @Override
    public void onSettingsServiceMaxAgeChange(final long maxAge, final float maxAgeRel) {

    // nothing to do here
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMaxDistanceChange(float, float)
     */
    @Override
    public void onSettingsServiceMaxDistanceChange(final float maxDistance, final float maxDistanceRel) {

    // nothing to do here
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMinAgeChange(long, float)
     */
    @Override
    public void onSettingsServiceMinAgeChange(final long minAge, final float minAgeRel) {

    // nothing to do here
    }
    
    /**
     * @see de.fraunhofer.fit.photocompass.activities.IServiceActivity#onSettingsServiceMinDistanceChange(float, float)
     */
    @Override
    public void onSettingsServiceMinDistanceChange(final float minDistance, final float minDistanceRel) {

    // nothing to do here
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu = OptionsMenu.populateMenu(menu);
        return true;
    }
    
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
