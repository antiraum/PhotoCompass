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
import android.view.Window;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.services.IOrientationService;
import de.fraunhofer.fit.photocompass.services.IOrientationServiceCallback;
import de.fraunhofer.fit.photocompass.services.OrientationService;

public class DummyMapActivity extends Activity {

	DummyMapActivity mapActivity;
    private IOrientationService _orientationService;
    private boolean _boundToOrientationService;

    private ServiceConnection _orientationServiceConn = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: connected to orientation service");
	    	
	    	// generate service object
	    	_orientationService = IOrientationService.Stub.asInterface(service);
	    	
	    	// register at the service
            try {
            	_orientationService.registerCallback(_orientationServiceCallback);
            } catch (DeadObjectException e) {
            	// service crashed
            } catch (RemoteException e) {
    			Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to register to orientation service");
            }
	    }
	
	    public void onServiceDisconnected(ComponentName name) {
	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: disconnected from orientation service");
	    	_orientationService = null;
	    }
    };
    
    private IOrientationServiceCallback _orientationServiceCallback = new IOrientationServiceCallback.Stub() {
		
		private float _roll;
    	
        public void onOrientationEvent(float yaw, float pitch, float roll) {
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: received event from orientation service");
        	
        	if (isFinishing()) return; // in the process of finishing, we don't need to do anything here
	    	
        	// currently we are only interested in the roll value
	    	if (roll == _roll) return; // value has not changed
	    	_roll = roll;
            
            // switch to activity based on orientation
        	int activity = PhotoCompassApplication.getActivityForRoll(_roll);
	    	if (activity == PhotoCompassApplication.FINDER_ACTIVITY) {
	    		Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: switching to finder activity");
	    		startActivity(new Intent(mapActivity, FinderActivity.class));
	    	}
        }
    };
    
    public DummyMapActivity() {
    	super();
    	mapActivity = this;
        _orientationService = null;
        _boundToOrientationService = false;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: onCreate");
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // setup view
	    setContentView(R.layout.dummymap_layout);
	}
    
    /**
     * Called before the activity becomes visible.
     */
    @Override
    public void onStart() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: onStart");
    	super.onStart();
    	
        // connect to orientation service
    	Intent orientationServiceIntent = new Intent(this, OrientationService.class);
    	_boundToOrientationService = bindService(orientationServiceIntent, _orientationServiceConn, Context.BIND_AUTO_CREATE);
    	if (! _boundToOrientationService) Log.e(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to connect to orientation service");
    }
    
    /**
     * Called when the activity is no longer visible.
     */
    @Override
    public void onStop() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: onStop");
    	
    	if (_boundToOrientationService) {
	    	
	    	// unregister from orientation service
	    	if (_orientationService != null) {
	    		try {
	    			_orientationService.unregisterCallback(_orientationServiceCallback);
	            } catch (DeadObjectException e) {
	            	// the service has crashed
	    		} catch (RemoteException e) {
	    			Log.w(PhotoCompassApplication.LOG_TAG, "DummyMapActivity: failed to unregister from orientation service");
	    		}
	    	}

	        // disconnect from orientation service
	        unbindService(_orientationServiceConn);
	        _boundToOrientationService = false;
    	}
        
        super.onStop();
    }
}
