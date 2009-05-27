package de.fraunhofer.fit.photocompass.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.widget.AbsoluteLayout;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.model.data.PhotoMetrics;

// TODO as AbsoluteLayout is depreciated in 1.5, we should implement our own layout
public class PhotosView extends AbsoluteLayout {
	
	// FIXME create a proper model for this (see also Photos class)
	public static final int PHOTO_VIEW_DEGREES = 40; // degrees out of 360 that are visible from one point
	
	
	private static final int MIN_PHOTO_HEIGHT = 50;
	private static int MAX_PHOTO_HEIGHT;
	
	private Context _context;
	
	private int _viewMaxWidth;
	private int _viewMaxHeight;
	
//	private Map<Integer, PhotoView> _photoViews; // map of currently displayed photo views (key is resourceId of the photo)
//	private List<PhotoBorderView> _photoBorderViews; // list of currently displayed photo border views

	public PhotosView(Context context, int viewMaxWidth, int viewMaxHeight) {
        super(context);
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView");
        
        _context = context;
        _viewMaxWidth = viewMaxWidth;
        _viewMaxHeight = viewMaxHeight;

        MAX_PHOTO_HEIGHT = (int) Math.round(0.65 * _viewMaxHeight); // 80 percent of view height
    	
//    	_photoViews = new HashMap<Integer, PhotoView>();
//    	_photoBorderViews = new ArrayList<PhotoBorderView>();
	}
	
	/**
	 * Change the displayed photos
	 * 
	 * 	Angles for the viewfinder are vertical 32º horizontal 48º
	 * 
	 * @param photos List of photos
	 * @param yaw Current viewing direction
	 */
	public void setPhotos(List<Photo> photos, float yaw) {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: setPhotos");
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: photos.size = "+photos.size());
    	
    	// remove all views
    	removeAllViews();
        
   // calculate the photo sizes - positions - altitude related with the screen.
    	
        Map<PhotoMetrics, Photo> photosMap = new HashMap<PhotoMetrics, Photo>();
        for (Photo photo : photos) {
	        int photoHeight = (int) Math.round(MIN_PHOTO_HEIGHT + (MAX_PHOTO_HEIGHT - MIN_PHOTO_HEIGHT) *
	        								   (1 - photo.getDistance() / ApplicationModel.getInstance().getMaxDistance()));
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: getDistance() = "+photo.getDistance()+", getMaxDistance() = "+ApplicationModel.getInstance().getMaxDistance());
	        int photoWidth = photoHeight / 4 * 3; // FIXME make this right (xScale = yScale)
	        
	        
	        int photoX = (int) Math.round(_viewMaxWidth * (photo.getDirection() - yaw + PHOTO_VIEW_DEGREES / 2) / PHOTO_VIEW_DEGREES);
	        
	     //calculating the percent of the y position of the photo on the screen
	     // percent is from 0.0 to 1.0
	        double h = photo.getDistance()/Math.cos(Math.toRadians(16));
	        double limit_ymeter = (Math.sin( Math.toRadians(16) )) * h ; 
	        double ypercent = limit_ymeter / photo.getAltOffset();
	        int photoY = (int) Math.round ( (_viewMaxHeight/2)* ypercent );
	        
	        
	        
	        photosMap.put(new PhotoMetrics(photoX, photoY, photoWidth, photoHeight), photo);
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: photoX = "+photoX+", photoY = "+photoY+", photoWidth = "+photoWidth+", photoHeight = "+photoHeight);
        }
        
        // add photo views
        for (Map.Entry<PhotoMetrics, Photo> photoEntry : photosMap.entrySet()) {
        	int resourceId = photoEntry.getValue().getResourceId();
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: resouceId = "+resourceId);
        	
        	// create and add photo view
        	PhotoView photoView = new PhotoView(_context, resourceId, photoEntry.getValue().getDistance());
        	photoView.setLayoutParams(photoEntry.getKey().getAbsoluteLayoutParams());
        	addView(photoView);
        }
        
        // add photo border views
        for (PhotoMetrics photoMetrics : photosMap.keySet()) {
	        PhotoBorderView photoBorderView = new PhotoBorderView(_context, photoMetrics.getWidth(), photoMetrics.getHeight());
	        photoBorderView.setLayoutParams(photoMetrics.getAbsoluteLayoutParams());
	        addView(photoBorderView);
//        	_photoBorderViews.add(photoBorderView);
        }
	}
}
