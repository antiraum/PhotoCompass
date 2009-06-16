package de.fraunhofer.fit.photocompass.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.model.data.PhotoMetrics;
import de.fraunhofer.fit.photocompass.views.layouts.SimpleAbsoluteLayout;

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
public final class PhotosView extends SimpleAbsoluteLayout {
	
	// photo height constants
	private static final float MIN_PHOTO_HEIGHT_PERCENT = .25F; // percent of the AVAILABLE_HEIGHT
	private static int MIN_PHOTO_HEIGHT;
	private static final float MAX_PHOTO_HEIGHT_PERCENT = .75F; // percent of the AVAILABLE_HEIGHT
	private static int MAX_PHOTO_HEIGHT;
	
	// size constrains
	private static int AVAILABLE_WIDTH;
	private static int AVAILABLE_HEIGHT;
	
	private static float DEGREE_WIDTH; // width of one degree direction  
	
	private Photos _photosModel;
	
	/**
	 * Layer containing the {@link #_photoViews}.
	 */
	private SimpleAbsoluteLayout _photoLayer;
	
	/**
	 * Layer containing the photo {@link #_borderViews}.
	 */
	private SimpleAbsoluteLayout _borderLayer;
	
	/**
	 * {@link PhotoView}s for photos (currently and previously used).
	 * Key is the resource/photo id.
	 */
	private final SparseArray<PhotoView> _photoViews = new SparseArray<PhotoView>();

	/**
	 * {@link PhotoBorderView}s for photos (currently and previously used).
	 * Key is the resource/photo id.
	 */
	private final SparseArray<PhotoBorderView> _borderViews = new SparseArray<PhotoBorderView>();
	
	// TODO maybe we can increase performance if we don't hide and show the views directly but rather put them into ViewStubs
	// which we then inflate

	/**
	 * Resource/photo ids of the currently used photos (sorted from farthest to nearest).
	 */
	private final ArrayList<Integer> _photos = new ArrayList<Integer>();

	/**
	 * {@link PhotoMetrics} of photos (currently and previously used).
	 * Key is the resource/photo id.
	 */
	private final SparseArray<PhotoMetrics> _photoMetrics = new SparseArray<PhotoMetrics>();
	
	/**
	 * Current viewing direction in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West)
	 */
	private float _direction;

	/**
	 * Constructor.
	 * Sets constants and creates the layers for photo and border views.
	 * 
	 * @param context
	 * @param availableWidth  Maximum width for this view (the display width).
	 * @param availableHeight Maximum height for this view (the display height minus status bar height and
	 * 						  minus the height of the controls on the bottom)
	 */
	public PhotosView(final Context context, final int availableWidth, final int availableHeight) {
        super(context);
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView");
        
        AVAILABLE_WIDTH = availableWidth;
        AVAILABLE_HEIGHT = availableHeight;
        DEGREE_WIDTH = AVAILABLE_WIDTH / PhotoCompassApplication.CAMERA_HDEGREES;

        // set height constants
        MAX_PHOTO_HEIGHT = (int) Math.round(MAX_PHOTO_HEIGHT_PERCENT * AVAILABLE_HEIGHT);
        MIN_PHOTO_HEIGHT = (int) Math.round(MIN_PHOTO_HEIGHT_PERCENT * AVAILABLE_HEIGHT);
    	
        _photosModel = Photos.getInstance();
        
        _photoLayer = new SimpleAbsoluteLayout(context);
        _photoLayer.setLayoutParams(new LayoutParams(AVAILABLE_WIDTH, AVAILABLE_HEIGHT, 0, 0));
        addView(_photoLayer);
    	
        _borderLayer = new SimpleAbsoluteLayout(context);
        _borderLayer.setLayoutParams(new LayoutParams(AVAILABLE_WIDTH, AVAILABLE_HEIGHT, 0, 0));
        addView(_borderLayer);
	}
	
	/**
	 * Adds photos to the list of currently used photos.
	 * If a photo has been used before its views are set to {@link View#VISIBLE} and updated.
	 * If a photo has not been used before, metrics and views are created and updated. 
	 * The photos are added to {@link #_photos} and the z orders of the photo and border views are updated.
	 * 
	 * @param newPhotos ArrayList of photo/resource ids of the photos to add.
	 * @param doRedraw Redraw after changes.
	 */
	public void addPhotos(final ArrayList<Integer> newPhotos, final boolean doRedraw) {
    	if (newPhotos.size() == 0) return; // nothing to do

    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: addPhotos: newPhotos.size() = "+newPhotos.size());
    	
		for (int id : newPhotos) {
			if (_photosModel.getPhoto(id) == null) continue;
	    	
	    	if (_photoMetrics.get(id) != null) { // has been used before
	    		
	    		// show views
	    		_photoViews.get(id).setVisibility(View.VISIBLE);
	    		_borderViews.get(id).setVisibility(View.VISIBLE);
	    		
	    	} else {
	    		
	    		// create metrics
	    		_photoMetrics.append(id, new PhotoMetrics());
	    		
		    	// create views
	    		final Context context = getContext();
	    		final PhotoView photoView = new PhotoView(context, id);
	    		_photoViews.append(id, photoView);
	    		_photoLayer.addView(photoView);
	    		final PhotoBorderView borderView = new PhotoBorderView(context);
	    		_borderViews.append(id, borderView);
	    		_borderLayer.addView(borderView);
	    	}
	    	
	    	// add to list of currently used photos
			_photos.add(id);
	    	
    		// update size and position and redraw if changed and wanted
			if (doRedraw && (_updatePhotoSize(id) || _updatePhotoXPosition(id) || _updatePhotoYPosition(id)))
				_redrawPhoto(id);
		}
		
		// sort photo order
		_sortPhotos();
		
		// update views z orders
		for (int id : _photos) {
			_photoViews.get(id).bringToFront();
			_borderViews.get(id).bringToFront();
		}
		
		// set number of occlusions for border alpha value
		_setBorderOcclusions();
	}

	/**
	 * Removes photos from the list of currently used photos.
	 * The views of the photos are set to {@link View#GONE} and their minimized state is reset.
	 * The photos are removed {@link #_photos}.
	 * 
	 * @param oldPhotos ArrayList of photo/resource ids of the photos to remove.
	 */
	public void removePhotos(final ArrayList<Integer> oldPhotos) {
    	if (oldPhotos.size() == 0) return; // nothing to do

    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: removePhotos: oldPhotos.size() = "+oldPhotos.size());
    	
		for (int id : oldPhotos) {
		
			// hide views
			_photoViews.get(id).setVisibility(View.GONE);
			_borderViews.get(id).setVisibility(View.GONE);
			
			// reset minimized state
			_photoViews.get(id).setMinimized(false);
		}
		
		// remove from list of currently used photos
		_photos.removeAll(oldPhotos);

		// update photo order
		_sortPhotos();
		
		// set number of occlusions for border alpha value
		_setBorderOcclusions();
	}
	
	/**
	 * Sorts ({@link #_photos}) based on their distance. Farthest to nearest.
	 */
	private void _sortPhotos() {
		Collections.sort(_photos, new Comparator<Integer>() {
	    	public int compare(final Integer id1, final Integer id2) {
	    		Photo photo1 = _photosModel.getPhoto(id1);
	    		Photo photo2 = _photosModel.getPhoto(id2);
	    		if (photo1 == null || photo2 == null) return 0;
	    		if (photo1.getDistance() > photo2.getDistance()) return -1;
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
			final PhotoMetrics met1 = _photoMetrics.get(resId1);
			int numOccludingPhotos = 0;
			int resId2;
			PhotoMetrics met2;
			ListIterator<Integer> lit = _photos.listIterator(_photos.size());
	        while (lit.hasPrevious()) { // iterate front to back
	        	resId2 = lit.previous();
				if (resId1 == resId2) break; 
				met2 = _photoMetrics.get(resId2);
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
	 * 
	 * @param doRedraw Redraw after changes.
	 */
	public void updateTextInfos(final boolean doRedraw) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: updateTextInfos");

		for (int id : _photos) _photoViews.get(id).updateText();
	}

	/**
	 * Updates the x position of all photos and redraws the ones that changed.
	 * 
	 * @param direction Current viewing direction in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West).
	 * @param doRedraw Redraw after changes.
	 */
	public void updateXPositions(final float direction, final boolean doRedraw) {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: updateXPositions: direction = "+direction);
		
		_direction = direction;

		for (int id : _photos) {
			if (_updatePhotoXPosition(id) && doRedraw) _redrawPhoto(id);
		}
	}
	
	/**
	 * Updates the x position of a photo.
	 * 
	 * @param id Photo/resource id of the photo to be updated.
	 * @return 	 <code>true</code> if the x position has changed, or
	 * 			 <code>false</code> if the x position has not changed.
	 */
	private boolean _updatePhotoXPosition(final int id) {
			
		final Photo photo = _photosModel.getPhoto(id);
		final PhotoMetrics metrics = _photoMetrics.get(id);
		if (metrics == null || photo == null) return false;
        
        // calculate the x position of the photo
		final double directionOffset = photo.getDirection() - _direction;
        final int photoX = (int) Math.round(AVAILABLE_WIDTH / 2 + directionOffset * DEGREE_WIDTH - metrics.getWidth() / 2);
        
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: _updatePhotoXPosition: directionOffset = "+directionOffset+", photoX = "+photoX);

        if (metrics.getLeft() == photoX) return false;
        
        // update metrics
        metrics.setLeft(photoX);
        return true;
	}

	/**
	 * Updates the y position of all photos and redraws the ones that changed.
	 * 
	 * @param doRedraw Redraw after changes.
	 */
	public void updateYPositions(final boolean doRedraw) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: updateYPositions");
    	
		for (int id : _photos) {
			if (_updatePhotoYPosition(id) && doRedraw) _redrawPhoto(id);
		}
	}
	
	/**
	 * Updates the y position of a photo.
     * The y position of the photo is determined by calculating the ratio between the altitude offset of the photo and the maximum
     * visible height at the distance of the photo. This ratio is then mapped to the available screen height.
     * 
	 * @param id Photo/resource id of the photo to be updated.
	 * @return 	 <code>true</code> if the y position has changed, or
	 * 			 <code>false</code> if the y position has not changed.
	 */
	private boolean _updatePhotoYPosition(final int id) {

		final Photo photo = _photosModel.getPhoto(id);
		final PhotoMetrics metrics = _photoMetrics.get(id);
		if (metrics == null || photo == null) return false;
		
		// calculate y position
	    // TODO take the roll value of the orientation sensor into account, then the FinderActivity wouldn't need to subtract the
	    // BOTTOM_CONTROLS_HEIGHT from the available height anymore -- also see the getPhotos method of the Photo model for this
		final int photoHeight = metrics.getHeight();
        int photoY = (AVAILABLE_HEIGHT - photoHeight) / 2;
		final double photoAltOffset = photo.getAltOffset();
        if (photoAltOffset != 0) {
	        final double halfOfMaxVisibleMeters = Math.sin(Math.toRadians(PhotoCompassApplication.CAMERA_VDEGREES / 2)) * photo.getDistance() /
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
	 * 
	 * @param doRedraw Redraw after changes.
	 */
	public void updateSizes(final boolean doRedraw) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: updateSizes");
    	
		for (int id : _photos) {
			if (_updatePhotoSize(id)) {
				_updatePhotoXPosition(id);
				_updatePhotoYPosition(id);
				if (doRedraw) _redrawPhoto(id);
			}
		}
	}
	
	/**
	 * Updates the size of a photo.
     * The photo height is a linear mapping of the ratio between photo distance and maximum visible distance to the
     * range between minimum photo height and maximum photo height.
     * To calculate the photo width the original aspect ratio of the photo is used.
     * 
	 * @param id Photo/resource id of the photo to be updated.
	 * @return 	 <code>true</code> if the height has changed, or
	 * 			 <code>false</code> if the height has not changed.
	 */
	private boolean _updatePhotoSize(final int id) {

		final Photo photo = _photosModel.getPhoto(id);
		final PhotoMetrics metrics = _photoMetrics.get(id);
		if (metrics == null || photo == null) return false;

    	// calculate the photo height
        final int photoHeight = (int) Math.round(MIN_PHOTO_HEIGHT + (MAX_PHOTO_HEIGHT - MIN_PHOTO_HEIGHT) *
        								   		 (1 - photo.getDistance() / (ApplicationModel.getInstance().getMaxDistance() - ApplicationModel.getInstance().getMinDistance())));
        
        if (metrics.getHeight() == photoHeight) return false;

        // calculate the photo width
        photo.determineOrigSize(getResources());
        final float scale = (float) photoHeight / (float) photo.getOrigHeight();
        final int photoWidth = (int) Math.round(photo.getOrigWidth() * scale);
        
        // update metrics
        metrics.setWidth(photoWidth);
        metrics.setHeight(photoHeight);
        return true;
	}
	
	/** 
	 * Redraws the photo and border view for a photo by updating its {@link LayoutParams}.
	 * 
	 * @param id Photo/Resource id of the photo to be redrawn.
	 */
	private void _redrawPhoto(final int id) {
		
    	final LayoutParams layoutParams = _photoViews.get(id).isMinimized() ? _photoMetrics.get(id).getMinimizedLayoutParams(AVAILABLE_HEIGHT - 21) // available height minus space for the labels and padding
    															  			: _photoMetrics.get(id).getLayoutParams();
    	
    	// skip if photo has layout parameters, and is not and will not be visible on screen
    	if (_photoViews.get(id).getLayoutParams() != null &&
    		(_photoViews.get(id).getRight() < 0 && layoutParams.x + layoutParams.width < 0) || // left of screen
    		(_photoViews.get(id).getLeft() > AVAILABLE_WIDTH && layoutParams.x > AVAILABLE_WIDTH)) // right of screen
    		return;
    	
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: _redrawPhoto: id = "+id+", x = "+layoutParams.x+", y = "+layoutParams.y+", width = "+layoutParams.width+", height = "+layoutParams.height);
    	
    	_photoViews.get(id).setLayoutParams(layoutParams);
    	_borderViews.get(id).setLayoutParams(layoutParams);
    }

	/**
	 * Gets called by the activity when a fling gesture is detected.
	 * Determines if the gesture was performed on an unminimized photo in vertical direction and minimizes it.
	 * 
	 * @param startX X-Position at the start of the gesture. 
	 * @param startY Y-Position at the start of the gesture. 
	 * @param endX   X-Position at the end of the gesture. 
	 * @param endY   Y-Position at the end of the gesture.
	 * @return       <code>true</code> if a photo is minimized, or
	 * 				 <code>false</code> if no action is performed.
	 */
    public boolean onFling(final float startX, final float startY, final float endX, final float endY) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onFling");
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onFling: startX = "+startX+", startY = "+startY+
//    										   ", endX = "+endX+", endY = "+endY);
    	
    	/*
    	 *  Detect which photo is flinged.
    	 */
    	int flingedPhoto = 0; // id of the flinged photo
    	int id;
		ListIterator<Integer> lit = _photos.listIterator(_photos.size());
        while (lit.hasPrevious()) { // iterate front to back
        	id = lit.previous();
        	if (_photoViews.get(id).isMinimized()) continue; // ignore minimized photos
    		if (_photoViews.get(id).getLeft() < startX && _photoViews.get(id).getRight() > startX && // on the view in horizontal direction
    			_photoViews.get(id).getTop() < startY && _photoViews.get(id).getBottom() > startY && // on the view in vertical direction
    			endY - startY > _photoViews.get(id).getHeight() / 3) { // fling movement should run for at least one third of the photo height
    			flingedPhoto = id;
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
	 * 
	 * @param x X-Position of the gesture. 
	 * @param y Y-Position of the gesture. 
	 * @return  <code>true</code> if a photo is restored, or
	 * 		    <code>false</code> if no action is performed.
	 */
    public boolean onSingleTapUp(final float x, final float y) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onSingleTapUp");
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onSingleTapUp: x = "+x+", y = "+y);

    	// tap tolerance
    	int y_tap_tolerance = 0;
    	if (PhotoMetrics.MINIMIZED_PHOTO_HEIGHT < PhotoCompassApplication.MIN_TAP_SIZE)
    		y_tap_tolerance = (PhotoCompassApplication.MIN_TAP_SIZE - PhotoMetrics.MINIMIZED_PHOTO_HEIGHT) / 2;
    	
    	/*
    	 * Detect which photo is tapped on.
    	 */
    	int tappedPhoto = 0; // id of the tapped photo
    	int id;
		ListIterator<Integer> lit = _photos.listIterator(_photos.size());
        while (lit.hasPrevious()) { // iterate front to back
        	id = lit.previous();
        	if (! _photoViews.get(id).isMinimized()) continue; // ignore not minimized photos
    		if (_photoViews.get(id).getLeft() < x && _photoViews.get(id).getRight() > x && // on the view in horizontal direction
    			_photoViews.get(id).getTop() - y_tap_tolerance < y && _photoViews.get(id).getBottom() + y_tap_tolerance > y) { // on the view in vertical direction
    			tappedPhoto = id;
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
    
    /**
     * Clears the currently not needed photo and border views to save memory.
     */
    public void clearUnneededViews() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: clearUnneededViews");
    	View view;
    	for (ViewGroup layer : new ViewGroup[] {_photoLayer, _borderLayer}) {
    		for (int i = 0; i < layer.getChildCount(); i++) {
    			view = layer.getChildAt(i);
    			if (_photos.contains(view.getId())) continue; // is currently needed
    			layer.removeView(view);
    		}
    	}
    }
}
