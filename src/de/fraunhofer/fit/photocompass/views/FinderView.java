package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

public class FinderView extends SurfaceView implements SurfaceHolder.Callback {
	
    private SurfaceHolder _holder;
    private Camera _camera;

    public FinderView(Context context) {
        super(context);

        // install a SurfaceHolder callback so we get notified when the
        // underlying surface is created and destroyed (activity gets paused)
        _holder = getHolder();
        _holder.addCallback(this);
        _holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderView: surfaceCreated");
    	
        // the Surface has been created, acquire the camera and tell it where to draw
        _camera = Camera.open();
        try {
        	_camera.setPreviewDisplay(holder);
        } catch (Exception e) {
        	Log.e(PhotoCompassApplication.LOG_TAG, "FinderView: setting camera preview display failed");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderView: surfaceDestroyed");
    	
        // surface will be destroyed, so stop the preview
        _camera.stopPreview();
        
        // release the camera
        _camera.release();
//        Thread.sleep(1000L) 
        _camera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "FinderView: surfaceChanged");
    	
        // size is known, set up the camera parameters and begin the preview
        Camera.Parameters parameters = _camera.getParameters();
        parameters.setPreviewSize(w, h);
        _camera.setParameters(parameters);
        _camera.startPreview();
    }
}