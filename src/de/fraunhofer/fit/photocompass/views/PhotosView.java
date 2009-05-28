package de.fraunhofer.fit.photocompass.views;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.model.data.PhotoMetrics;

// TODO as AbsoluteLayout is depreciated in 1.5, we should implement our own layout
public class PhotosView extends AbsoluteLayout {
	
	// FIXME set this to a correct value determined by the camera capacities
	public static final int PHOTO_VIEW_HDEGREES = 48; // horizontal degrees out of 360 that are visible from one point
	public static final int PHOTO_VIEW_VDEGREES = 32; // vertical degrees out of 360 that are visible from one point
	
	private static final int MIN_PHOTO_HEIGHT = 50;
	private static int MAX_PHOTO_HEIGHT;
	private static final int MINIMIZED_PHOTO_HEIGHT = 30;
	private Context _context;
	private int _viewMaxWidth; // display width
	private int _viewMaxHeight; // display height minus statusbar height
	
	private AbsoluteLayout _photoLayer; // layer with all the photo views
	private AbsoluteLayout _borderLayer; // layer with all the photo border views
	private Map<Integer, PhotoView> _photoViews; // map of photo views (key is resourceId of the photo) (sorted back to front)
	private Map<Integer, PhotoBorderView> _borderViews; // map of photo border views (key is resourceId of the photo) (sorted back to front)

	public PhotosView(Context context, int viewMaxWidth, int viewMaxHeight) {
        super(context);
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView");
        
        _context = context;
        _viewMaxWidth = viewMaxWidth;
        _viewMaxHeight = viewMaxHeight;

        MAX_PHOTO_HEIGHT = (int) Math.round(0.65 * _viewMaxHeight); // 80 percent of view height
    	
        _photoLayer = new AbsoluteLayout(_context);
        _photoLayer.setLayoutParams(new LayoutParams(_viewMaxWidth, _viewMaxHeight, 0, 0));
        addView(_photoLayer);
    	
        _borderLayer = new AbsoluteLayout(_context);
        _borderLayer.setLayoutParams(new LayoutParams(_viewMaxWidth, _viewMaxHeight, 0, 0));
        addView(_borderLayer);
        
    	_photoViews = new LinkedHashMap<Integer, PhotoView>();
    	_borderViews = new LinkedHashMap<Integer, PhotoBorderView>();
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
    	
    	// hide the photo views not needed right now
    	photoViews: for (Map.Entry<Integer, PhotoView> photoView : _photoViews.entrySet()) {
    		for (Photo photo : photos) {
    			if (photo.getResourceId() != photoView.getKey()) continue;
    			continue photoViews; 
    		}
    		photoView.getValue().setVisibility(View.GONE);
    	}
	
		// hide the photo border views not needed right now
		borderViews: for (Map.Entry<Integer, PhotoBorderView> borderView : _borderViews.entrySet()) {
			for (Photo photo : photos) {
				if (photo.getResourceId() != borderView.getKey()) continue;
				continue borderViews; 
			}
			borderView.getValue().setVisibility(View.GONE);
		}
        
        // calculate the photo sizes and positions
        Map<PhotoMetrics, Photo> photosMap = new LinkedHashMap<PhotoMetrics, Photo>();
        for (Photo photo : photos) {
	        int photoHeight = (int) Math.round(MIN_PHOTO_HEIGHT + (MAX_PHOTO_HEIGHT - MIN_PHOTO_HEIGHT) *
	        								   (1 - photo.getDistance() / ApplicationModel.getInstance().getMaxDistance()));
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: getDistance() = "+photo.getDistance()+", getMaxDistance() = "+ApplicationModel.getInstance().getMaxDistance());
	        int photoWidth = photoHeight / 4 * 3; // FIXME make this right (xScale = yScale)
	        int photoX = (int) Math.round(_viewMaxWidth * (photo.getDirection() - yaw + PHOTO_VIEW_HDEGREES / 2) / PHOTO_VIEW_HDEGREES);
	        int photoY = (_viewMaxHeight - photoHeight) / 2;
	        photosMap.put(new PhotoMetrics(photoX, photoY, photoWidth, photoHeight), photo);
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: photoX = "+photoX+", photoY = "+photoY+", photoWidth = "+photoWidth+", photoHeight = "+photoHeight);
        }
        
        // setup the views
        for (Map.Entry<PhotoMetrics, Photo> photoEntry : photosMap.entrySet()) {
        	int resourceId = photoEntry.getValue().getResourceId();
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: resourceId = "+resourceId+", getDistance() = "+photoEntry.getValue().getDistance());
        	
        	// check if the photo view already exists
        	boolean photoViewExists = false;
            for (Map.Entry<Integer, PhotoView> photoView : _photoViews.entrySet()) {
        		if (photoView.getKey() != resourceId) continue;
        		photoViewExists = true;
        		
        		// show view
        		photoView.getValue().setVisibility(View.VISIBLE);
        		bringChildToFront(photoView.getValue());
        		
        		// put to front in the map
        		_photoViews.remove(photoView.getKey());
        		_photoViews.put(photoView.getKey(), photoView.getValue());
        		
        		break;
        	}
        	
        	// create photo view if it does not exist
        	if (! photoViewExists) {
    			_photoViews.put(resourceId, new PhotoView(_context, resourceId, photoEntry.getValue().getDistance()));
        		_photoLayer.addView(_photoViews.get(resourceId));
        	}

        	// set photo view layout parameters
        	LayoutParams layoutParams = photoEntry.getKey().getAbsoluteLayoutParams();
        	if (_photoViews.get(resourceId).isMinimized()) {
        		layoutParams.y = layoutParams.y + layoutParams.height - MINIMIZED_PHOTO_HEIGHT;
        		layoutParams.height = MINIMIZED_PHOTO_HEIGHT;
        	}
        	_photoViews.get(resourceId).setLayoutParams(layoutParams);
        	
        	// check if the photo border view already exists
        	boolean borderViewExists = false;
            for (Map.Entry<Integer, PhotoBorderView> borderView : _borderViews.entrySet()) {
        		if (borderView.getKey() != resourceId) continue;
        		borderViewExists = true;
        		
        		// show view
        		borderView.getValue().setVisibility(View.VISIBLE);
        		bringChildToFront(borderView.getValue());
        		
        		// put to front in the map
        		_borderViews.remove(borderView.getKey());
        		_borderViews.put(borderView.getKey(), borderView.getValue());
        		
        		break;
        	}
        	
        	// create photo border view if it does not exist
        	if (! borderViewExists) {
        		_borderViews.put(resourceId, new PhotoBorderView(_context));
        		_borderLayer.addView(_borderViews.get(resourceId));
        	}

        	// set photo border view layout parameters
        	_borderViews.get(resourceId).setLayoutParams(layoutParams);
        }
	}

	/**
	 * gets called by the activity when a fling gesture is detected
	 */
    public boolean onFling(float startX, float startY, float endX, float endY) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onFling: startX = "+startX+", startY = "+startY+", endX = "+endX+", endY = "+endY);
    	
    	// detect which photo was "flinged"
    	// TODO change this to a reverse iteration through the map
    	int flingedPhoto = 0;
        for (Map.Entry<Integer, PhotoView> photoView : _photoViews.entrySet()) {
        	PhotoView view = photoView.getValue();
        	if (view.isMinimized()) continue;
    		if (view.getLeft() < startX && view.getRight() > startX &&
    			view.getTop() < startY && view.getBottom() > startY &&
    			endY - startY > view.getHeight() / 2) flingedPhoto = photoView.getKey();
    	}
        if (flingedPhoto == 0) return false;
    	
    	// minimize photo and photo border view
        PhotoView photoView = _photoViews.get(flingedPhoto);
        PhotoBorderView borderView = _borderViews.get(flingedPhoto);
        photoView.setMinimized(true);
        borderView.setMinimized(true);
    	LayoutParams layoutParams = new LayoutParams(photoView.getWidth(), MINIMIZED_PHOTO_HEIGHT, photoView.getLeft(),
    												 photoView.getTop() + photoView.getHeight() - MINIMIZED_PHOTO_HEIGHT);
    	photoView.setLayoutParams(layoutParams);
    	borderView.setLayoutParams(layoutParams);
        
        return true;
    }

	/**
	 * gets called by the activity when a single tap gesture is detected
	 */
    public boolean onSingleTapUp(float x, float y) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onSingleTapUp: x = "+x+", y = "+y);

    	// detect which minimized photo was tapped
    	// TODO change this to a reverse iteration through the map
    	int Y_TAP_TOLERANCE = 5;
    	int tappedPhoto = 0;
        for (Map.Entry<Integer, PhotoView> photoView : _photoViews.entrySet()) {
        	PhotoView view = photoView.getValue();
        	if (! view.isMinimized()) continue;
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onSingleTapUp: photo = "+photoView.getKey()+", left = "+view.getLeft()+", top = "+view.getTop()+", right = "+view.getRight()+", bottom = "+view.getBottom());
    		if (view.getLeft() < x && view.getRight() > x &&
    			view.getTop() - Y_TAP_TOLERANCE < y && view.getBottom() + Y_TAP_TOLERANCE > y) tappedPhoto = photoView.getKey();
    	}
        if (tappedPhoto == 0) return false;
    	
    	// restore photo and photo border view
        PhotoView photoView = _photoViews.get(tappedPhoto);
        PhotoBorderView borderView = _borderViews.get(tappedPhoto);
        photoView.setMinimized(false);
        borderView.setMinimized(false);
        int photoHeight = photoView.getWidth() / 3 * 4; // FIXME make this right (xScale = yScale)
    	LayoutParams layoutParams = new LayoutParams(photoView.getWidth(), photoHeight, photoView.getLeft(),
    												 (_viewMaxHeight - photoHeight) / 2);
    	photoView.setLayoutParams(layoutParams);
    	borderView.setLayoutParams(layoutParams);
        
        return true;
    }
}
