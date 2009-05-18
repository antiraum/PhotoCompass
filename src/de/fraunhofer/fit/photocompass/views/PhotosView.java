package de.fraunhofer.fit.photocompass.views;

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
	
	private static final int MIN_PHOTO_HEIGHT = 50;
	private static int MAX_PHOTO_HEIGHT;
	private Context _context;

	public PhotosView(Context context) {
        super(context);
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView");

        MAX_PHOTO_HEIGHT = (int) Math.round(0.8 * getHeight()); // 80 percent of view height
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: MAX_PHOTO_HEIGHT = "+MAX_PHOTO_HEIGHT);
        
        _context = context;
	}
	
	public void setPhotos(List<Photo> photos) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: setPhotos");
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: photos.size = "+photos.size());
        
        // calculate the photo sizes and positions
        Map<PhotoMetrics, Photo> photosMap = new HashMap<PhotoMetrics, Photo>();
        for (Photo photo : photos) {
	        int photoHeight = (int) (MIN_PHOTO_HEIGHT + (MAX_PHOTO_HEIGHT - MIN_PHOTO_HEIGHT) *
	        											(1 - photo.getDistance() / ApplicationModel.getInstance().getMaxDistance()));
	        int photoWidth = photoHeight / 4 * 3; // TODO make this right (xScale = yScale)
	        int photoX = (int) Math.round(((AbsoluteLayout) getParent()).getWidth() * 360 / photo.getDirection());
	        int photoY = (getWidth() - photoHeight) / 2;
	        photosMap.put(new PhotoMetrics(photoX, photoY, photoWidth, photoHeight), photo);
        }
        
        // add photo views
        for (Map.Entry<PhotoMetrics, Photo> photoEntry : photosMap.entrySet()) {
	        PhotoView photoView = new PhotoView(_context, photoEntry.getValue().getResourceId());
	        photoView.setLayoutParams(photoEntry.getKey().getAbsoluteLayoutParams());
	        addView(photoView);
        }
        
        // add photo border views
        for (PhotoMetrics photoMetrics : photosMap.keySet()) {
	        PhotoBorderView photoBorderView = new PhotoBorderView(_context, photoMetrics.getWidth(), photoMetrics.getHeight());
	        photoBorderView.setLayoutParams(photoMetrics.getAbsoluteLayoutParams());
	        addView(photoBorderView);
        }
	}
}
