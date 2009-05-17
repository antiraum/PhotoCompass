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
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.services.IOrientationService;
import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;
import de.fraunhofer.fit.photocompass.services.OrientationService;

public class SplashActivity extends Activity {

	SplashActivity splashActivity;
	
    private IOrientationService _orientationService;
    private boolean _boundToOrientationService;

    private ServiceConnection _orientationServiceConn = new ServiceConnection() {

	    public void onServiceConnected(ComponentName name, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: connected to orientation service");
	    	
	    	// generate service object
	    	_orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	_orientationService.registerCallback(_orientationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "SplashActivity: failed to register to orientation service");
            }
	    }
	
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: disconnected from orientation service");
	    	_orientationService = null;
	    }
    };
    
    private IOrientationServiceCallback _orientationServiceCallback = new IOrientationServiceCallback.Stub() {
    	
        public void onOrientationEvent(float azimuth, float pitch, float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: received event from orientation service");
        	
        	if (isFinishing()) return; // splash activity is in the process of finishing, we don't need to do anything here
            
            // switch to activity based on orientation
        	int activity = PhotoCompassApplication.getActivityForRoll(roll);
	    	if (activity == PhotoCompassApplication.FINDER_ACTIVITY) {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: switching to finder activity");
	    		startActivity(new Intent(splashActivity, FinderActivity.class));
	    	} else {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: switching to map activity");
//	    		startActivity(new Intent(splashActivity, PhotoMapActivity.class));
	    		startActivity(new Intent(splashActivity, DummyMapActivity.class));
	    	}
	        
	        // close splash activity
	        finish();
        }
    };
    
    public SplashActivity() {
    	super();
    	splashActivity = this;
        _orientationService = null;
        _boundToOrientationService = false;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: onCreate");
        super.onCreate(savedInstanceState);
    	
        // connect to orientation service
    	Intent orientationServiceIntent = new Intent(this, OrientationService.class);
    	_boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToOrientationService) Log.e(PhotoCompassApplication.LOG_TAG, "SplashActivity: failed to connect to orientation service");
        
        // setup view
	    setContentView(R.layout.splash_layout);
	}
    
    /**
     * Called when the activity is no longer visible.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "SplashActivity: onStop");
    	
    	if (_boundToOrientationService) {
    	
	    	// unregister from orientation service
	    	if (_orientationService != null) {
	    		try {
	    			_orientationService.unregisterCallback(_orientationServiceCallback);
	            } catch (DeadObjectException e) {
	            	// the service has crashed
	    		} catch (RemoteException e) {
	    			Log.e(PhotoCompassApplication.LOG_TAG, "SplashActivity: failed to unregister from orientation service");
	    		}
	    	}
	    	
	    	// disconnect from orientation service
	        unbindService(_orientationServiceConn);
	        _boundToOrientationService = false;
    	}
    	
        super.onStop();
    }
}
