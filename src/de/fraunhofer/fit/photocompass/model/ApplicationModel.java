package de.fraunhofer.fit.photocompass.model;

import android.os.DeadObjectException;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

public class ApplicationModel {

    private static ApplicationModel _instance;

	private float _maxDistance; // in meters
	private int _minAge; // in ...
	private int _maxAge; // in ...

    private RemoteCallbackList<IApplicationModelCallback> _remoteCallbacks = new RemoteCallbackList<IApplicationModelCallback>();
	
	protected ApplicationModel() {
		super();
		
		// default values
		_maxDistance = 500;
		_minAge = 0;
		_maxAge = 100000;
	}

    public static ApplicationModel getInstance() {
        if (_instance == null) _instance = new ApplicationModel();
        return _instance;
    }
    
    public void registerCallback(IApplicationModelCallback cb) {
        if (cb != null) _remoteCallbacks.register(cb);
    }
    
    public void unregisterCallback(IApplicationModelCallback cb) {
        if (cb != null) _remoteCallbacks.unregister(cb);
    }

	public float getMaxDistance() {
		return _maxDistance;
	}

	public void setMaxDistance(float value) {
		_maxDistance = value;
		_broadcastChange();
	}

	public int getMinAge() {
		return _minAge;
	}

	public void setMinAge(int value) {
		_minAge = value;
		_broadcastChange();
	}

	public int getMaxAge() {
		return _maxAge;
	}

	public void setMaxAge(int value) {
		_maxAge = value;
		_broadcastChange();
	}
	
    // broadcasts the application model change to all registered callbacks
	private void _broadcastChange() {
	    final int numCallbacks = _remoteCallbacks.beginBroadcast();
	    for (int i = 0; i < numCallbacks; i++) {
	        try {
	            _remoteCallbacks.getBroadcastItem(i).onApplicationModelChange();
	        } catch (DeadObjectException e) {
	            // the RemoteCallbackList will take care of removing the dead object
	        } catch (RemoteException e) {
		    	Log.e(PhotoCompassApplication.LOG_TAG, "ApplicationModel: broadcast to callback failed");
	        }
	    }
	    _remoteCallbacks.finishBroadcast();
	}
}
