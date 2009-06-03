package de.fraunhofer.fit.photocompass.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ListIterator;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AbsoluteLayout;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.model.data.PhotoMetrics;

/**
 * <p>This view is used by the {@link de.fraunhofer.fit.photocompass.activities.FinderActivity} and displays the currently visible photos.</p>
 * <p>The view should always know about all photos that are visible in any direction from the current position with the current settings.
 * These photos can be added and removed to/from the view with the methods {@link #addPhotos(ArrayList, boolean)} and 
 * {@link #removePhotos(ArrayList)}. {@link #getPhotos()} returns the photos the view currently uses.</p>
 * <p>For every photo the view creates an instance of {@link PhotoMetrics}, {@link PhotoView}, and {@link PhotoBorderView}. When a photo
 * is removed, these instances are not dismissed, but kept for later reuse (for better performance).</p>
 * <p>To initiate updates of the position and dimension of the photos use these methods: {@link #updateXPositions(float, boolean)}, 
 * {@link #updateYPositions(boolean)}, {@link #updateSizes(boolean)}, and {@link #updateTextInfos(boolean)}.</p>
 * <p>As photos can be interacted with, the view provides the methods {@link #onFling(float, float, float, float)} and 
 * {@link #onSingleTapUp(float, float)} to pass touch events to it.</p>
 */
// TODO as AbsoluteLayout is depreciated in 1.5, we should implement our own layout
public class PhotosView extends AbsoluteLayout {
	
	// photo height constants
	private static final float MIN_PHOTO_HEIGHT_PERCENT = .25f; // percent of the AVAILABLE_HEIGHT
	private static int MIN_PHOTO_HEIGHT;
	private static final float MAX_PHOTO_HEIGHT_PERCENT = .75f; // percent of the AVAILABLE_HEIGHT
	private static int MAX_PHOTO_HEIGHT;
	
	// size constrains
	private static int AVAILABLE_WIDTH;
	private static int AVAILABLE_HEIGHT;
	
	private static float DEGREE_WIDTH; // width of one degree direction  
	
	private AbsoluteLayout _photoLayer; // layer with all the photo views
	private AbsoluteLayout _borderLayer; // layer with all the photo border views
	private HashMap<Integer, PhotoView> _photoViews; // map of photo views (key is resourceId of the photo) (sorted back to front)
	private HashMap<Integer, PhotoBorderView> _borderViews; // map of photo border views (key is resourceId of the photo) (sorted back to front)

	private ArrayList<Integer> _photos; // resourceIds of the currently used photos sorted from farthest to nearest
	private HashMap<Integer, PhotoMetrics> _photoMetrics; // metrics of photos (currently and previously used)
	private float _direction; // current viewing direction in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West)
	
	/**
	 * Constructor.
	 * Sets constants, creates the layers for photo and border views, and initializes state variables.
	 * @param context
	 * @param availableWidth  Maximum width for this view (the display width).
	 * @param availableHeight Maximum height for this view (the display height minus status bar height and
	 * 						  minus the height of the controls on the bottom)
	 */
	public PhotosView(Context context, int availableWidth, int availableHeight) {
        super(context);
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView");
        
        AVAILABLE_WIDTH = availableWidth;
        AVAILABLE_HEIGHT = availableHeight;
        DEGREE_WIDTH = AVAILABLE_WIDTH / PhotoCompassApplication.CAMERA_HDEGREES;

        // set height constants
        MAX_PHOTO_HEIGHT = (int) Math.round(MAX_PHOTO_HEIGHT_PERCENT * AVAILABLE_HEIGHT);
        MIN_PHOTO_HEIGHT = (int) Math.round(MIN_PHOTO_HEIGHT_PERCENT * AVAILABLE_HEIGHT);
    	
        _photoLayer = new AbsoluteLayout(context);
        _photoLayer.setLayoutParams(new LayoutParams(AVAILABLE_WIDTH, AVAILABLE_HEIGHT, 0, 0));
        addView(_photoLayer);
    	
        _borderLayer = new AbsoluteLayout(context);
        _borderLayer.setLayoutParams(new LayoutParams(AVAILABLE_WIDTH, AVAILABLE_HEIGHT, 0, 0));
        addView(_borderLayer);
        
    	_photoViews = new LinkedHashMap<Integer, PhotoView>();
    	_borderViews = new LinkedHashMap<Integer, PhotoBorderView>();
        
        _photos = new ArrayList<Integer>();
        _photoMetrics = new HashMap<Integer, PhotoMetrics>();
	}
	
	/**
	 * Adds photos to the list of currently used photos.
	 * If a photo has been used before its views are set to {@link View#VISIBLE} and updated.
	 * If a photo has not been used before, metrics and views are created and updated. 
	 * The photos are added to {@link #_photos} and the z orders of the photo and border views are updated.
	 * @param newPhotos ArrayList of resource ids of the photos to add.
	 * @param doRedraw Redraw after changes.
	 */
	public void addPhotos(ArrayList<Integer> newPhotos, boolean doRedraw) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: addPhotos: newPhotos.size() = "+newPhotos.size());
    	
    	if (newPhotos.size() == 0) return; // nothing to do
		
		for (int resourceId : newPhotos) {
	    	
			// check if photo has been used before
			boolean usedBefore = _photoMetrics.containsKey(resourceId);
			
	    	if (usedBefore) {
	    		
	    		// show views
	    		_photoViews.get(resourceId).setVisibility(View.VISIBLE);
	    		_borderViews.get(resourceId).setVisibility(View.VISIBLE);
	    		
	    	} else {
	    		
	    		// create metrics
	    		_photoMetrics.put(resourceId, new PhotoMetrics());
	    		
		    	// create views
	    		Context context = getContext();
	    		PhotoView photoView = new PhotoView(context, resourceId);
				_photoViews.put(resourceId, photoView);
	    		_photoLayer.addView(photoView);
	    		PhotoBorderView borderView = new PhotoBorderView(context);
	    		_borderViews.put(resourceId, borderView);
	    		_borderLayer.addView(borderView);
	    	}
	    	
	    	// add to list of currently used photos
			_photos.add(resourceId);
	    	
    		// update size and position and redraw if changed and wanted
			if (doRedraw && 
				(_updatePhotoSize(resourceId) || _updatePhotoXPosition(resourceId) || _updatePhotoYPosition(resourceId)))
				_redrawPhoto(resourceId);
		}
		
		// sort photo order
		_sortPhotos();
		
		// update views z orders
		for (int resourceId : _photos) {
			_photoViews.get(resourceId).bringToFront();
			_borderViews.get(resourceId).bringToFront();
		}
		
		// set number of occlusions for border alpha value
		_setBorderOcclusions();
	}

	/**
	 * Removes photos from the list of currently used photos.
	 * The views of the photos are set to {@link View#GONE} and their minimized state is reset.
	 * The photos are removed {@link #_photos}.
	 * @param oldPhotos ArrayList of resource ids of the photos to remove.
	 */
	public void removePhotos(ArrayList<Integer> oldPhotos) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: addPhotos: oldPhotos.size() = "+oldPhotos.size());
    	
    	if (oldPhotos.size() == 0) return; // nothing to do
    	
		for (int resourceId : oldPhotos) {
		
			// hide views
			PhotoView photoView = _photoViews.get(resourceId);
			PhotoBorderView borderView = _borderViews.get(resourceId);
    		photoView.setVisibility(View.GONE);
			borderView.setVisibility(View.GONE);
			
			// reset minimized state
    		photoView.setMinimized(false);
			
			// remove from map of currently used photos
    		_photos.remove(resourceId);
		}

		// update photo order
		_sortPhotos();
		
		// set number of occlusions for border alpha value
		_setBorderOcclusions();
	}
	
	/**
	 * Sorts the currently used photos ({@link #_photos}) based on their distance.
	 */
	private void _sortPhotos() {
		Collections.sort(_photos, new Comparator() {
	    	public int compare(Object o1, Object o2) {
	    		if (Photos.getInstance().getPhoto((Integer) o1).getDistance() >
	    			Photos.getInstance().getPhoto((Integer) o2).getDistance()) return -1;
	    		return 1;
	        }
	    });
	}
	
	/**
	 * Sets the number of occluding photos for every border view.
	 * The border view decreases it's alpha value for every photo that occludes the photo it belongs to.
	 */
	private void _setBorderOcclusions() {

		for (int resId1 : _photos) {
			PhotoMetrics met1 = _photoMetrics.get(resId1);
			int numOccludingPhotos = 0;
	        for (ListIterator<Integer> lit = _photos.listIterator(_photos.size()); lit.hasPrevious();) { // iterate front to back
	        	int resId2 = lit.previous();
				if (resId1 == resId2) break; 
				PhotoMetrics met2 = _photoMetrics.get(resId2);
				if (((met2.getTop() >= met1.getTop() && met2.getTop() <= met1.getBottom()) ||
					 (met2.getBottom() >= met1.getTop() && met2.getBottom() <= met1.getBottom()) ||
					 (met2.getTop() < met1.getTop() && met2.getBottom() > met1.getBottom())) &&
					((met2.getLeft() >= met1.getLeft() && met2.getLeft() <= met1.getRight()) ||
					 (met2.getRight() >= met1.getLeft() && met2.getRight() <= met1.getRight()) ||
					 (met2.getLeft() < met1.getLeft() && met2.getRight() > met1.getRight()))) numOccludingPhotos++;
			}
	        _borderViews.get(resId1).setNumberOfOcclusions(numOccludingPhotos);
		}
	}
	
	/**
	 * @return The photos currently used by the view.
	 */
	public ArrayList<Integer> getPhotos() {
		return _photos;
	}
	
	/**
	 * Updates the text overlay on the photo views.
	 * @param doRedraw Redraw after changes.
	 */
	public void updateTextInfos(boolean doRedraw) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: updateTextInfos");

		for (int resourceId : _photos) _photoViews.get(resourceId).updateText();
	}

	/**
	 * Updates the x position of all photos and redraws the ones that changed.
	 * @param direction Current viewing direction in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West).
	 * @param doRedraw Redraw after changes.
	 */
	public void updateXPositions(float direction, boolean doRedraw) {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: updateXPositions: direction = "+direction);
		
		_direction = direction;

		for (int resourceId : _photos) {
			if (_updatePhotoXPosition(resourceId) && doRedraw) _redrawPhoto(resourceId);
		}
	}
	
	/**
	 * Updates the x position of a photo.
	 * @param resourceId Resource id of the photo to be updated.
	 * @return 			 <code>true</code> if the x position has changed, or
	 * 					 <code>false</code> if the x position has not changed.
	 */
	private boolean _updatePhotoXPosition(int resourceId) {
			
		PhotoMetrics metrics = _photoMetrics.get(resourceId);
        
        // calculate the x position of the photo
		double directionOffset = Photos.getInstance().getPhoto(resourceId).getDirection() - _direction;
        int photoX = (int) Math.round(AVAILABLE_WIDTH / 2 + directionOffset * DEGREE_WIDTH - metrics.getWidth() / 2);
        
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: _updatePhotoXPosition: directionOffset = "+directionOffset+", photoX = "+photoX);

        if (metrics.getLeft() == photoX) return false;
        
        // update metrics
        metrics.setLeft(photoX);
        return true;
	}

	/**
	 * Updates the y position of all photos and redraws the ones that changed.
	 * @param doRedraw Redraw after changes.
	 */
	public void updateYPositions(boolean doRedraw) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: updateYPositions");
    	
		for (int resourceId : _photos) {
			if (_updatePhotoYPosition(resourceId) && doRedraw) _redrawPhoto(resourceId);
		}
	}
	
	/**
	 * Updates the y position of a photo.
     * The y position of the photo is determined by calculating the ratio between the altitude offset of the photo and the maximum
     * visible height at the distance of the photo. This ratio is then mapped to the available screen height.
	 * @param resourceId Resource id of the photo to be updated.
	 * @return 			 <code>true</code> if the y position has changed, or
	 * 					 <code>false</code> if the y position has not changed.
	 */
	private boolean _updatePhotoYPosition(int resourceId) {
			
		Photo photo = Photos.getInstance().getPhoto(resourceId);
		PhotoMetrics metrics = _photoMetrics.get(resourceId);
		
		// calculate y position
	    // TODO take the roll value of the orientation sensor into account, then the FinderActivity wouldn't need to subtract the
	    // BOTTOM_CONTROLS_HEIGHT from the available height anymore -- also see the getPhotos method of the Photo model for this
		int photoHeight = metrics.getHeight();
        int photoY = (AVAILABLE_HEIGHT - photoHeight) / 2;
		double photoAltOffset = photo.getAltOffset();
        if (photoAltOffset != 0) {
	        double halfOfMaxVisibleMeters = Math.sin(Math.toRadians(PhotoCompassApplication.CAMERA_VDEGREES / 2)) * photo.getDistance() /
	        							    Math.cos(Math.toRadians(PhotoCompassApplication.CAMERA_VDEGREES / 2));
	        int pixelOffset = (int) Math.round(Math.abs(photoAltOffset) / halfOfMaxVisibleMeters *
	        								   (AVAILABLE_HEIGHT - photoHeight) / 2);
	        if (photoAltOffset > 0) pixelOffset *= -1;
	        photoY += pixelOffset;
//	        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: altOffset = "+photoAltOffset+
//	        										   ", halfOfMaxVisibleMeters = "+halfOfMaxVisibleMeters+
//	        										   ", pixelOffset = "+pixelOffset);
        }
        
        if (metrics.getTop() == photoY) return false;
        
        // update metrics
        metrics.setTop(photoY);
        return true;
	}

	/**
	 * Updates the sizes of all photos and redraws the ones that changed.
	 * @param doRedraw Redraw after changes.
	 */
	public void updateSizes(boolean doRedraw) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: updateSizes");
    	
		for (int resourceId : _photos) {
			if (_updatePhotoSize(resourceId)) {
				_updatePhotoXPosition(resourceId);
				_updatePhotoYPosition(resourceId);
				if (doRedraw) _redrawPhoto(resourceId);
			}
		}
	}
	
	/**
	 * Updates the size of a photo.
     * The photo height is a linear mapping of the ratio between photo distance and maximum visible distance to the
     * range between minimum photo height and maximum photo height.
     * To calculate the photo width the original aspect ratio of the photo is used.
	 * @param resourceId Resource id of the photo to be updated.
	 * @return 			 <code>true</code> if the height has changed, or
	 * 					 <code>false</code> if the height has not changed.
	 */
	private boolean _updatePhotoSize(int resourceId) {
			
		Photo photo = Photos.getInstance().getPhoto(resourceId);
		PhotoMetrics metrics = _photoMetrics.get(resourceId);

    	// calculate the photo height
        int photoHeight = (int) Math.round(MIN_PHOTO_HEIGHT + (MAX_PHOTO_HEIGHT - MIN_PHOTO_HEIGHT) *
        								   (1 - photo.getDistance() / ApplicationModel.getInstance().getMaxDistance()));
        
        if (metrics.getHeight() == photoHeight) return false;

        // calculate the photo width
        photo.determineOrigSize(getResources());
        float scale = (float) photoHeight / (float) photo.getOrigHeight();
        int photoWidth = (int) Math.round(photo.getOrigWidth() * scale);
        
        // update metrics
        metrics.setWidth(photoWidth);
        metrics.setHeight(photoHeight);
        return true;
	}
	
	/** 
	 * Redraws the photo and border view for a photo by updating its {@link LayoutParams}.
	 * @param resourceId Resource id of the photo to be redrawn.
	 */
	private void _redrawPhoto(int resourceId) {
		
    	PhotoView photoView = _photoViews.get(resourceId);
    	LayoutParams layoutParams = photoView.isMinimized() ? _photoMetrics.get(resourceId).getMinimizedLayoutParams()
    														: _photoMetrics.get(resourceId).getLayoutParams();
    	
    	// skip if photo has layout parameters, and is not and will not be visible on screen
    	if (photoView.getLayoutParams() != null &&
    		(photoView.getRight() < 0 && layoutParams.x + layoutParams.width < 0) || // left of screen
    		(photoView.getLeft() > AVAILABLE_WIDTH && layoutParams.x > AVAILABLE_WIDTH)) // right of screen
    		return;
    	
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: _redrawPhoto: resourceId = "+resourceId+", x = "+layoutParams.x+", y = "+layoutParams.y+", width = "+layoutParams.width+", height = "+layoutParams.height);
    	
    	photoView.setLayoutParams(layoutParams);
    	_borderViews.get(resourceId).setLayoutParams(layoutParams);
    }

	/**
	 * Gets called by the activity when a fling gesture is detected.
	 * Determines if the gesture was performed on an unminimized photo in vertical direction and minimizes it.
	 * @param startX X-Position at the start of the gesture. 
	 * @param startY Y-Position at the start of the gesture. 
	 * @param endX   X-Position at the end of the gesture. 
	 * @param endY   Y-Position at the end of the gesture.
	 * @return       <code>true</code> if a photo is minimized, or
	 * 				 <code>false</code> if no action is performed.
	 */
    public boolean onFling(float startX, float startY, float endX, float endY) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onFling: startX = "+startX+", startY = "+startY+
    										   ", endX = "+endX+", endY = "+endY);
    	
    	/*
    	 *  Detect which photo is flinged.
    	 */
    	int flingedPhoto = 0; // resourceId of the flinged photo
        for (ListIterator<Integer> lit = _photos.listIterator(_photos.size()); lit.hasPrevious();) { // iterate front to back
        	int resourceId = lit.previous();
        	PhotoView view = _photoViews.get(resourceId);
        	if (view.isMinimized()) continue; // ignore minimized photos
    		if (view.getLeft() < startX && view.getRight() > startX && // on the view in horizontal direction
    			view.getTop() < startY && view.getBottom() > startY && // on the view in vertical direction
    			endY - startY > view.getHeight() / 3) { // fling movement should run for at least one third of the photo height
    			flingedPhoto = resourceId;
    			break;
    		}
    	}
        if (flingedPhoto == 0) return false; // no photo matched
    	
    	// set photo view minimized
        _photoViews.get(flingedPhoto).setMinimized(true);
        
        // redraw photo
        _redrawPhoto(flingedPhoto);
        
        return true;
    }

	/**
	 * Gets called by the activity when a single tap gesture is detected.
	 * Determines if the gesture was performed on a minimized photo and restores it.
	 * @param x X-Position of the gesture. 
	 * @param y Y-Position of the gesture. 
	 * @return  <code>true</code> if a photo is restored, or
	 * 		    <code>false</code> if no action is performed.
	 */
    public boolean onSingleTapUp(float x, float y) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onSingleTapUp: x = "+x+", y = "+y);

    	// tap tolerance
    	int y_tap_tolerance = 0;
    	if (PhotoMetrics.MINIMIZED_PHOTO_HEIGHT < PhotoCompassApplication.MIN_TAP_SIZE)
    		y_tap_tolerance = (PhotoCompassApplication.MIN_TAP_SIZE - PhotoMetrics.MINIMIZED_PHOTO_HEIGHT) / 2;
    	
    	/*
    	 *  Detect which photo is tapped on.
    	 */
    	int tappedPhoto = 0; // resourceId of the flinged photo
        for (ListIterator<Integer> lit = _photos.listIterator(_photos.size()); lit.hasPrevious();) { // iterate front to back
        	int resourceId = lit.previous();
        	PhotoView view = _photoViews.get(resourceId);
        	if (! view.isMinimized()) continue; // ignore not minimized photos
    		if (view.getLeft() < x && view.getRight() > x && // on the view in horizontal direction
    			view.getTop() - y_tap_tolerance < y && view.getBottom() + y_tap_tolerance > y) { // on the view in vertical direction
    			tappedPhoto = resourceId;
    			break;
    		}
    	}
        if (tappedPhoto == 0) return false; // no photo matched
    	
    	// set photo view restored
        _photoViews.get(tappedPhoto).setMinimized(false);
        
        // redraw photo
        _redrawPhoto(tappedPhoto);
        
        return true;
    }
}
