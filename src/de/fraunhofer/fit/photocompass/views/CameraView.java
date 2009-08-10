package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

/**
 * This view is used by the {@link de.fraunhofer.fit.photocompass.activities.CameraActivity} and displays the image from
 * the camera finder.
 */
public final class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    
    private final SurfaceHolder _surfaceHolder;
    private Camera _camera;
    
    /**
     * Constructor. Initializes the surface holder.
     * 
     * @param context
     */
    public CameraView(final Context context) {

        super(context);
        
        // setup the surface holder
        _surfaceHolder = getHolder();
        _surfaceHolder.setKeepScreenOn(true);
        _surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        // register as callback so we get notified when the underlying surface is created, changed, or destroyed (activity gets paused)
        _surfaceHolder.addCallback(this);
    }
    
    /**
     * This is called after the surface is first created. Acquires the camera and tells it where to draw.
     */
    public void surfaceCreated(final SurfaceHolder holder) {

//    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderView: surfaceCreated");
        
        _camera = Camera.open();
        try {
            _camera.setPreviewDisplay(holder);
        } catch (final Exception e) {
            Log.e(PhotoCompassApplication.LOG_TAG, "FinderView: setting camera preview display failed");
        }
    }
    
    /**
     * This is called before a surface is being destroyed. Stops the preview and releases the camera.
     */
    public void surfaceDestroyed(final SurfaceHolder holder) {

//    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderView: surfaceDestroyed");
        
        // stop the preview
        _camera.stopPreview();
        
        // release the camera
        _camera.release();
        _camera = null;
    }
    
    /**
     * This is called after any structural changes (format or size) have been made to the surface. Sets up the camera to
     * the surface size and starts the preview.
     */
    public void surfaceChanged(final SurfaceHolder holder, final int format, final int w, final int h) {

//    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderView: surfaceChanged");
        
        // set up the camera parameters
        final Camera.Parameters parameters = _camera.getParameters();
        parameters.setPreviewSize(w, h);
        _camera.setParameters(parameters);
        
        // start the preview
        _camera.startPreview();
    }
}