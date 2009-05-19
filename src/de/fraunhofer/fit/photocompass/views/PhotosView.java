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
	 * @param photos List of photos
	 * @param yaw Current viewing direction
	 */
	public void setPhotos(List<Photo> photos, float yaw) {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: setPhotos");
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: photos.size = "+photos.size());
    	
    	// remove all views
    	removeAllViews();
    	
    	// remove all photo views we no longer need
//    	photoViews: for (int resourceId : _photoViews.keySet()) {
//    		for (Photo photo : photos) {
//    			if (photo.getResourceId() != resourceId) continue;
//    			continue photoViews; 
//    		}
//    		removeView(_photoViews.get(resourceId));
//    		_photoViews.remove(resourceId);
//    	}
    	
    	// remove all photo border views
    	// TODO better would be to also reuse existing photo border views
//    	for (PhotoBorderView photoBorderView : _photoBorderViews) removeView(photoBorderView);
//    	_photoBorderViews.clear();
        
        // calculate the photo sizes and positions
        Map<PhotoMetrics, Photo> photosMap = new HashMap<PhotoMetrics, Photo>();
        for (Photo photo : photos) {
	        int photoHeight = (int) Math.round(MIN_PHOTO_HEIGHT + (MAX_PHOTO_HEIGHT - MIN_PHOTO_HEIGHT) *
	        								   (1 - photo.getDistance() / ApplicationModel.getInstance().getMaxDistance()));
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: getDistance() = "+photo.getDistance()+", getMaxDistance() = "+ApplicationModel.getInstance().getMaxDistance());
	        int photoWidth = photoHeight / 4 * 3; // FIXME make this right (xScale = yScale)
	        int photoX = (int) Math.round(_viewMaxWidth * (photo.getDirection() - yaw + PHOTO_VIEW_DEGREES / 2) / PHOTO_VIEW_DEGREES);
	        int photoY = (_viewMaxHeight - photoHeight) / 2;
	        photosMap.put(new PhotoMetrics(photoX, photoY, photoWidth, photoHeight), photo);
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: photoX = "+photoX+", photoY = "+photoY+", photoWidth = "+photoWidth+", photoHeight = "+photoHeight);
        }
        
        // add photo views
        for (Map.Entry<PhotoMetrics, Photo> photoEntry : photosMap.entrySet()) {
        	int resourceId = photoEntry.getValue().getResourceId();
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: resouceId = "+resourceId);
        	
        	// create and add photo view
        	PhotoView photoView = new PhotoView(_context, resourceId);
        	photoView.setLayoutParams(photoEntry.getKey().getAbsoluteLayoutParams());
        	addView(photoView);
        	
        	// check if the view already exists
//        	boolean viewExists = false;
//        	for (int resId : Collections.unmodifiableMap(_photoViews).keySet()) {
//        		if (resId != resourceId) continue;
//        		viewExists = true;
//        		break;
//        	}
        	
        	// create if it does not exist
//        	if (! viewExists) {
//    			_photoViews.put(resourceId, new PhotoView(_context, resourceId));
//        		addView(_photoViews.get(resourceId));
//        	}

        	// set layout parameters
//        	_photoViews.get(resourceId).setLayoutParams(photoEntry.getKey().getAbsoluteLayoutParams());
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
