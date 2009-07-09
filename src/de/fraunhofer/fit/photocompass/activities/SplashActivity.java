package de.fraunhofer.fit.photocompass.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.services.IOrientationService;
import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;
import de.fraunhofer.fit.photocompass.services.OrientationService;

/**
 * This class is the Activity component for the splash screen of the application.
 * This is the Activity the application starts with.
 * It determines the initial orientation of the phone and switches to the right Activity for this orientation.
 * It also initializes the {@link Photos} model. 
 */
public final class SplashActivity extends Activity {

	SplashActivity splashActivity; // package scoped for faster access by inner classes
	
    private IOrientationService orientationService = null; // package scoped for faster access by inner classes
    private boolean _boundToOrientationService = false;

    /**
     * Connection object for the connection with the {@link OrientationService}.
     */
    private final ServiceConnection _orientationServiceConn = new ServiceConnection() {

	    public void onServiceConnected(final ComponentName name, final IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: connected to orientation service");
	    	
	    	// generate service object
	    	orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	orientationService.registerCallback(orientationServiceCallback);
            } catch (final DeadObjectException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "SplashActivity: orientation service has crashed");
            } catch (final RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "SplashActivity: failed to register to orientation service");
            }
	    }
	
	    public void onServiceDisconnected(final ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: disconnected from orientation service");
	    	orientationService = null;
	    }
    };

    /**
     * Callback object for the {@link OrientationService}.
     * Gets registered and unregistered at the {@link #orientationService} object.
     * Package scoped for faster access by inner classes.
     */
    final IOrientationServiceCallback orientationServiceCallback = new IOrientationServiceCallback.Stub() {
    	
        public void onOrientationEvent(final float yaw, final float pitch, final float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: received event from orientation service");
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
            
            // switch to activity based on orientation
        	final int activity = PhotoCompassApplication.getActivityForRoll(roll, PhotoCompassApplication.SPLASH_ACTIVITY);
	    	if (activity == PhotoCompassApplication.FINDER_ACTIVITY) {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: switching to finder activity");
	    		ProgressDialog.show(splashActivity, "",  "Loading camera view. Please wait...", true);
	    		startActivity(new Intent(splashActivity, FinderActivity.class));
	    	} else {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: switching to map activity");
	    		ProgressDialog.show(splashActivity, "",  "Loading map view. Please wait...", true);
	    		if (PhotoCompassApplication.TARGET_PLATFORM == 3) {
	    			startActivity(new Intent(splashActivity, PhotoMapActivity.class));
	    		} else {
	    			startActivity(new Intent(splashActivity, DummyMapActivity.class));
	    		}
	    	}
	        
	        // close splash activity
	        finish();
        }
    };

    /**
     * Constructor.
     */
    public SplashActivity() {
    	super();
    	splashActivity = this;
    }

    /**
     * Called when the activity is first created.
     * Connects to the {@link OrientationService}.
     * Initializes the photo model.
     * Initializes the view.
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        // set application variables
        final Display display = getWindowManager().getDefaultDisplay();
        PhotoCompassApplication.DISPLAY_WIDTH = display.getWidth();
        PhotoCompassApplication.DISPLAY_HEIGHT = display.getHeight();
        
        // initialize photos model
        Photos.getInstance().initialize(this);
    	
        // connect to orientation service
    	final Intent orientationServiceIntent = new Intent(this, OrientationService.class);
    	_boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToOrientationService) Log.e(PhotoCompassApplication.LOG_TAG, "SplashActivity: failed to connect to orientation service");
        
        // setup view
	    setContentView(R.layout.splash_layout);
	}
    
    /**
     * Called when the activity is no longer visible.
     * Unregisters the {@link #orientationServiceCallback} from the {@link OrientationService} and then 
     * disconnects from the {@link OrientationService}.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: onStop");
    	
    	if (_boundToOrientationService) {
    	
	    	// unregister from orientation service
	    	if (orientationService != null) {
	    		try {
	    			orientationService.unregisterCallback(orientationServiceCallback);
	            } catch (final DeadObjectException e) {
	    			Log.e(PhotoCompassApplication.LOG_TAG, "SplashActivity: orientation service has crashed");
	    		} catch (final RemoteException e) {
	    			Log.e(PhotoCompassApplication.LOG_TAG, "SplashActivity: failed to unregister from orientation service");
	    		}
	    	}
	    	
	    	// disconnect from orientation service
	        unbindService(_orientationServiceConn);
	        _boundToOrientationService = false;
    	}
    	
        super.onStop();
    }
    
    /**
     * Populate the options menu.
     */
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu = OptionsMenu.populateMenu(menu);
        return true;
    }

    /**
     * Handles the option menu item selections.
     */
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
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
