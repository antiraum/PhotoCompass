package de.fraunhofer.fit.photocompass.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.Settings;

/**
 * This model stores the current values for distance and age limitations of the displayed photos. The values are set by
 * the controls in the {@link de.fraunhofer.fit.photocompass.views.ControlsView}. This is an active model, where
 * Activities can register as callbacks in order to get updates when the values change. This is a Singleton.
 */
public final class SettingsService extends Service {
    
    /**
     * Implementation of the interface to this service. Is provided to activities when they connect.
     */
    private final ISettingsService.Stub _binder = new ISettingsService.Stub() {
        
        private Settings _settings = new Settings();
        
        @Override
        public Settings getSettings() throws RemoteException {

            return _settings;
        }
        
        @Override
        public void updateSettings(final Settings settings) throws RemoteException {

            _settings = settings;
        }
    };
    
    /**
     * Constructor.
     */
    public SettingsService() {

    // nothing to do here
    }
    
    /**
     * Called by the system when the service is first created.
     */
    @Override
    public void onCreate() {

        Log.d(PhotoCompassApplication.LOG_TAG, "SettingsService: onCreate");
        super.onCreate();
    }
    
    /**
     * Called when an activity connects to the service.
     * 
     * @return The interface to the service.
     */
    @Override
    public IBinder onBind(final Intent intent) {

        Log.d(PhotoCompassApplication.LOG_TAG, "SettingsService: onBind");
        return _binder;
    }
    
    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. Shuts down the service.
     */
    @Override
    public void onDestroy() {

        Log.d(PhotoCompassApplication.LOG_TAG, "SettingsService: onDestroy");
        
        super.onDestroy();
    }
}
