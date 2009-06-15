package de.fraunhofer.fit.photocompass;

import android.app.Application;
import android.graphics.Color;
import android.location.Location;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.Photos;

/**
 * This is the Application class of the PhotoCompass application. It gets initialized at application start.
 * We use it to store some global information and to initialize the model singletons.
 */
public final class PhotoCompassApplication extends Application {
	
	// tag for logging
    public static final String LOG_TAG = "PhotoCompass";
    
    // running environment constants - change these if you switch between testing on the emulator and the G1
    public static final boolean RUNNING_ON_EMULATOR = false;
    public static final int TARGET_PLATFORM = 3; // 1 for 1.1, 2 for 1.5, 3 for 1.5 with Google libraries 
    
    // activity constants
    public static final int FINDER_ACTIVITY = 1;
    public static final int MAP_ACTIVITY = 2;

	// camera angle constants
	// FIXME set this to a correct value determined by the camera capacities
	public static final int CAMERA_HDEGREES = 48; // horizontal degrees out of 360 that are visible on the screen
	public static final int CAMERA_VDEGREES = 32; // vertical degrees out of 360 that are visible on the screen
    
    // colors
	public static final int GREY = Color.parseColor("#9c9c9c");
	public static final int DARK_GREY = Color.parseColor("#5a5a5a");
    public static final int ORANGE = Color.parseColor("#ffd300");
    public static final int DARK_ORANGE = Color.parseColor("#ffb600");
    
    // tap interaction constants
    public static final int MIN_TAP_SIZE = 40; // minimum size of an area that can be tapped on
	
    // dummy location settings (enable for development when a fixed location is needed)
    public static final boolean USE_DUMMY_LOCATION = false;
    public static Location dummyLocation;
    
    // dummy photo settings (enable for development when a fixed set of photos is needed)
    public static final boolean USE_DUMMY_PHOTOS = false;
    
    /**
     * Constructor.
     * Initializes the models {@link ApplicationModel} and {@link Photos}.
     */
    public PhotoCompassApplication() {
    	super();

    	// setup dummy location
    	if (USE_DUMMY_LOCATION) {
	    	dummyLocation = new Location("");
//	    	dummyLocation.setLatitude(Location.convert("50:43:12.59")); // B-IT
//	    	dummyLocation.setLongitude(Location.convert("7:7:16.2")); // B-IT
//	    	dummyLocation.setAltitude(103); // B-IT
	    	dummyLocation.setLatitude(Location.convert("50:44:58.43")); // FIT
	    	dummyLocation.setLongitude(Location.convert("7:12:14.54")); // FIT
	    	dummyLocation.setAltitude(125); // FIT
    	}
    	
    	// initialize models
    	ApplicationModel.getInstance();
    	Photos.getInstance();
    }
    
    /**
     * Returns the activity constant for a roll value from the orientation sensor.
     * Is used by the activities to determine when they have to switch to another activity.
     * 
     * @param roll Roll value of the orientation sensor (values from -180 to 180).
     * @return Activity constant of the correct activity at this roll value.
     * 		   {@link #FINDER_ACTIVITY} when the phone is held vertically, or
     * 		   {@link #MAP_ACTIVITY} when the phone is held horizontally.
     */
    public static int getActivityForRoll(final float roll) {
    	if ((roll > -135 && roll < -45) || (roll > 45 && roll < 135)) {
    		return FINDER_ACTIVITY;
    	} else {
    		return MAP_ACTIVITY;
    	}
    }
}
