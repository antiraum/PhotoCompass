package de.fraunhofer.fit.photocompass.views;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.model.data.PhotoMetrics;
import de.fraunhofer.fit.photocompass.model.data.PhotoMetricsComparator;

// TODO as AbsoluteLayout is depreciated in 1.5, we should implement our own layout
public class PhotosView extends AbsoluteLayout {
	
	private static final float MIN_PHOTO_HEIGHT_PERCENT = .3f;
	private static int MIN_PHOTO_HEIGHT;
	private static final float MAX_PHOTO_HEIGHT_PERCENT = .9f;
	private static int MAX_PHOTO_HEIGHT;
	private static final float MINIMIZED_PHOTO_HEIGHT_PERCENT = .15f;
	private static int MINIMIZED_PHOTO_HEIGHT;
	
	private Context _context;
	private int _availableWidth; // display width
	private int _availableHeight; // display height minus status bar height and minus the height of the controls on the bottom
	
	private AbsoluteLayout _photoLayer; // layer with all the photo views
	private AbsoluteLayout _borderLayer; // layer with all the photo border views
	private Map<Integer, PhotoView> _photoViews; // map of photo views (key is resourceId of the photo) (sorted back to front)
	private Map<Integer, PhotoBorderView> _borderViews; // map of photo border views (key is resourceId of the photo) (sorted back to front)

	public PhotosView(Context context, int availableWidth, int availableHeight) {
        super(context);
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView");
        
        _context = context;
        _availableWidth = availableWidth;
        _availableHeight = availableHeight;

        // set height constants
        MAX_PHOTO_HEIGHT = (int) Math.round(MAX_PHOTO_HEIGHT_PERCENT * _availableHeight);
        MIN_PHOTO_HEIGHT = (int) Math.round(MIN_PHOTO_HEIGHT_PERCENT * _availableHeight);
        MINIMIZED_PHOTO_HEIGHT = (int) Math.round(MINIMIZED_PHOTO_HEIGHT_PERCENT * _availableHeight);
    	
        _photoLayer = new AbsoluteLayout(_context);
        _photoLayer.setLayoutParams(new LayoutParams(_availableWidth, _availableHeight, 0, 0));
        addView(_photoLayer);
    	
        _borderLayer = new AbsoluteLayout(_context);
        _borderLayer.setLayoutParams(new LayoutParams(_availableWidth, _availableHeight, 0, 0));
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
    	
    	/*
    	 * hide the photo views not needed right now
    	 */
    	photoViews: for (Map.Entry<Integer, PhotoView> photoView : _photoViews.entrySet()) {
    		for (Photo photo : photos) {
    			if (photo.getResourceId() != photoView.getKey()) continue;
    			continue photoViews; 
    		}
    		photoView.getValue().setVisibility(View.GONE);
    	}
	
		/*
		 * hide the photo border views not needed right now
		 */
		borderViews: for (Map.Entry<Integer, PhotoBorderView> borderView : _borderViews.entrySet()) {
			for (Photo photo : photos) {
				if (photo.getResourceId() != borderView.getKey()) continue;
				continue borderViews; 
			}
			borderView.getValue().setVisibility(View.GONE);
		}
        
        /*
         * calculate the photo sizes and positions
         */
    	// nearestDistance and furthestDistance are needed for calculating the relative distance
    	// (relative to the other currently visible photos) wich is used by the photo border views
    	float nearestDistance = ApplicationModel.getInstance().getMaxDistance();
    	float furthestDistance = 0;
        SortedMap<PhotoMetrics, Photo> photosMap = new TreeMap<PhotoMetrics, Photo>(new PhotoMetricsComparator());
        for (Photo photo : photos) {
        	if (nearestDistance > photo.getDistance()) nearestDistance = photo.getDistance();
        	if (furthestDistance < photo.getDistance()) furthestDistance = photo.getDistance();
        	
        	// the photo height is a linear mapping of the ratio between photo distance and maximum visible distance to the
        	// range between minimum photo height and maximum photo height
	        int photoHeight = (int) Math.round(MIN_PHOTO_HEIGHT + (MAX_PHOTO_HEIGHT - MIN_PHOTO_HEIGHT) *
	        								   (1 - photo.getDistance() / ApplicationModel.getInstance().getMaxDistance()));
	        
	        // to calculate the photo width the original aspect ratio of the photo is used
	        photo.determineOrigSize(getResources());
	        float scale = (float) photoHeight / (float) photo.getOrigHeight();
	        int photoWidth = (int) Math.round(photo.getOrigWidth() * scale);
	        int photoX = (int) Math.round(_availableWidth * (photo.getDirection() - yaw + PhotoCompassApplication.CAMERA_HDEGREES / 2) /
	        							  PhotoCompassApplication.CAMERA_HDEGREES);
	        
	        // the y position of the photo is determined by calculating the ratio between the altitude difference of the photo
	        // to the current altitude and the maximum visible height at the distance of the photo
	        // this ratio is then mapped to the available screen height
	        // TODO take the roll value of the orientation sensor into account, then the FinderActivity wouldn't need to subtract the
	        // BOTTOM_CONTROLS_HEIGHT from the available height any more -- also see the getPhotos method of the Photo model for this
	        int photoY = (_availableHeight - photoHeight) / 2;
	        if (photo.getAltOffset() != 0) {
		        double halfOfMaxVisibleMeters = Math.sin(Math.toRadians(PhotoCompassApplication.CAMERA_VDEGREES / 2)) * photo.getDistance() /
		        							    Math.cos(Math.toRadians(PhotoCompassApplication.CAMERA_VDEGREES / 2));
		        int pixelOffset = (int) Math.round(Math.abs(photo.getAltOffset()) / halfOfMaxVisibleMeters *
		        								   (_availableHeight - photoHeight) / 2);
		        if (photo.getAltOffset() > 0) pixelOffset *= -1;
		        photoY += pixelOffset;
	        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: altOffset = "+photo.getAltOffset()+
	        										   ", halfOfMaxVisibleMeters = "+halfOfMaxVisibleMeters+
	        										   ", pixelOffset = "+pixelOffset);
	        }
	        
        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: photoX = "+photoX+", photoY = "+photoY+", scale = "+scale+
        										   ", photoWidth = "+photoWidth+", photoHeight = "+photoHeight);
	        
	        photosMap.put(new PhotoMetrics(photoX, photoY, photoWidth, photoHeight), photo);
        }
//        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: nearestDistance = "+nearestDistance+", furthestDistance = "+furthestDistance);
        
        /*
         * setup the views
         */
        for (Map.Entry<PhotoMetrics, Photo> photoEntry : photosMap.entrySet()) {
        	int resourceId = photoEntry.getValue().getResourceId();
//        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: resourceId = "+resourceId+
//        										   ", getDistance() = "+photoEntry.getValue().getDistance());
        	
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
    			_photoViews.put(resourceId, new PhotoView(_context, photoEntry.getValue()));
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

        	// set photo border view parameters
        	float relativeDistance = 1;
        	if (furthestDistance != nearestDistance) // this happens if currently only one photo is visible
        		relativeDistance = 1 - (photoEntry.getValue().getDistance() - nearestDistance) / (furthestDistance - nearestDistance);
        	_borderViews.get(resourceId).setDistance(relativeDistance);
        	_borderViews.get(resourceId).setLayoutParams(layoutParams);
        }
	}

	/**
	 * gets called by the activity when a fling gesture is detected
	 */
    public boolean onFling(float startX, float startY, float endX, float endY) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onFling: startX = "+startX+", startY = "+startY+
    										   ", endX = "+endX+", endY = "+endY);
    	
    	// detect which photo was "flinged"
    	// TODO change this to a reverse iteration through the map
    	int flingedPhoto = 0;
        for (Map.Entry<Integer, PhotoView> photoView : _photoViews.entrySet()) {
        	PhotoView view = photoView.getValue();
        	if (view.isMinimized()) continue;
    		if (view.getLeft() < startX && view.getRight() > startX &&
    			view.getTop() < startY && view.getBottom() > startY &&
    			endY - startY > view.getHeight() / 3) flingedPhoto = photoView.getKey();
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
    		if (view.getLeft() < x && view.getRight() > x &&
    			view.getTop() - Y_TAP_TOLERANCE < y && view.getBottom() + Y_TAP_TOLERANCE > y) tappedPhoto = photoView.getKey();
    	}
        if (tappedPhoto == 0) return false;
    	
    	// restore photo and photo border view
        PhotoView photoView = _photoViews.get(tappedPhoto);
        PhotoBorderView borderView = _borderViews.get(tappedPhoto);
        photoView.setMinimized(false);
        borderView.setMinimized(false);
        photoView.getPhoto().determineOrigSize(getResources());
        float scale = (float) photoView.getWidth() / (float) photoView.getPhoto().getOrigWidth();
        int photoHeight = (int) Math.round(photoView.getPhoto().getOrigHeight() * scale);
    	LayoutParams layoutParams = new LayoutParams(photoView.getWidth(), photoHeight, photoView.getLeft(),
    												 (_availableHeight - photoHeight) / 2);
    	photoView.setLayoutParams(layoutParams);
    	borderView.setLayoutParams(layoutParams);
        
        return true;
    }
}
