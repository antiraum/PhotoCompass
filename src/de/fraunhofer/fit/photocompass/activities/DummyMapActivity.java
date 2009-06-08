package de.fraunhofer.fit.photocompass.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.services.IOrientationService;
import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;
import de.fraunhofer.fit.photocompass.services.OrientationService;

/**
 * This class is a dummy replacement of the {@link PhotoMapActivity} for running the application on a platform without Google libraries.
 * If this class or {@link PhotoMapActivity} is used is controlled by the {@link PhotoCompassApplication#TARGET_PLATFORM} constant.
 */
public final class DummyMapActivity extends Activity {

	DummyMapActivity mapActivity; // package scoped for faster access by inner classes
    IOrientationService orientationService; // package scoped for faster access by inner classes
    private boolean _boundToOrientationService;

    /**
     * Connection object for the connection with the {@link OrientationService}.
     */
    private final ServiceConnection _orientationServiceConn = new ServiceConnection() {

    	/**
    	 * Gets called when the service connection is established.
    	 * Creates the {@link #orientationService} object from the service interface and
    	 * registers the {@link #orientationServiceCallback}.
    	 */
	    public void onServiceConnected(final ComponentName className, final IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: connected to orientation service");
	    	
	    	// generate service object
	    	orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	orientationService.registerCallback(orientationServiceCallback);
            } catch (final DeadObjectException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: orientation service has crashed");
            } catch (final RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to register to orientation service");
            }
	    }

    	/**
    	 * Gets called when the service connection is closed down.
    	 * Frees {@link #orientationService}.
    	 */
	    public void onServiceDisconnected(final ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: disconnected from orientation service");
	    	orientationService = null;
	    }
    };
    
    /**
     * Callback object for the {@link OrientationService}.
     * Gets registered and unregistered at the {@link #orientationService} object.
     * Package scoped for faster access by inner classes.
     */
    final IOrientationServiceCallback orientationServiceCallback = new IOrientationServiceCallback.Stub() {
		
		private float _roll;
    	
		/**
		 * Gets called when new data is provided by the {@link OrientationService}.
		 * Initiates switch to {@link FinderActivity} when the phone is held vertically. 
		 */
        public void onOrientationEvent(final float yaw, final float pitch, final float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: received event from orientation service");
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
	    	
        	// we are only interested in the roll value
	    	if (roll == _roll) return; // value has not changed
	    	_roll = roll;
            
            // switch to activity based on orientation
        	final int activity = PhotoCompassApplication.getActivityForRoll(_roll);
	    	if (activity == PhotoCompassApplication.FINDER_ACTIVITY) {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: switching to finder activity");
	    		startActivity(new Intent(mapActivity, FinderActivity.class));
		        finish(); // close this activity
	    	}
        }
    };
    
    /**
     * Constructor.
     * Initializes the state variables.
     */
    public DummyMapActivity() {
    	super();
    	mapActivity = this;
        orientationService = null;
        _boundToOrientationService = false;
    }

    /**
     * Called when the activity is first created.
     * Initializes the views.
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
     * Called before the activity becomes visible.
     * Connects to the {@link OrientationService}.
     */
    @Override
    public void onStart() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: onStart");
    	super.onStart();
    	
        // connect to orientation service
    	final Intent orientationServiceIntent = new Intent(this, OrientationService.class);
    	_boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToOrientationService) Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to connect to orientation service");
    }
    
    /**
     * Called when the activity is no longer visible.
     * Unregisters the {@link #orientationServiceCallback} from the {@link OrientationService} and then 
     * disconnects from the {@link OrientationService}.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: onStop");
    	
    	if (_boundToOrientationService) {
	    	
	    	// unregister from orientation service
	    	if (orientationService != null) {
	    		try {
	    			orientationService.unregisterCallback(orientationServiceCallback);
	            } catch (final DeadObjectException e) {
	    			Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: orientation service has crashed");
	    		} catch (final RemoteException e) {
	    			Log.w(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to unregister from orientation service");
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
