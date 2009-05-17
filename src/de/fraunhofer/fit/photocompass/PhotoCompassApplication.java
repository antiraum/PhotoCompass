package de.fraunhofer.fit.photocompass;

import android.app.Application;

public class PhotoCompassApplication extends Application {
	
	// tag for logging
    public static final String LOG_TAG = "PhotoCompass";
    
    // activity constants
    public static final int FINDER_ACTIVITY = 1;
    public static final int MAP_ACTIVITY = 2;
    
    // returns the activity constant for a roll value from the orientation sensor
    public static int getActivityForRoll(float roll) {
    	if ((roll > -135 && roll < -45) || (roll > 45 && roll < 135)) {
    		return FINDER_ACTIVITY;
    	} else {
    		return MAP_ACTIVITY;
    	}
    }
}
