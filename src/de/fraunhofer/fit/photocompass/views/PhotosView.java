package de.fraunhofer.fit.photocompass.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.activities.CameraActivity;
import de.fraunhofer.fit.photocompass.model.Settings;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.model.data.PhotoMetrics;
import de.fraunhofer.fit.photocompass.views.layouts.SimpleAbsoluteLayout;

/**
 * <p>
 * This view is used by the {@link de.fraunhofer.fit.photocompass.activities.CameraActivity} and displays the currently
 * visible photos.
 * </p>
 * <p>
 * The view should always know about all photos that are visible in any direction from the current position with the
 * current settings. These photos can be added and removed to/from the view with the methods
 * {@link #addPhotos(ArrayList, boolean)} and {@link #removePhotos(ArrayList)}. The photos the view currently uses are
 * stored in {@link #photos}.
 * </p>
 * <p>
 * For every photo the view creates an instance of {@link PhotoMetrics}, {@link PhotoView}, and {@link PhotoBorderView}.
 * When a photo is removed, these instances are not dismissed, but kept for later reuse (for better performance).
 * </p>
 * <p>
 * To initiate updates of the position and dimension of the photos use these methods:
 * {@link #updateXPositions(float, boolean)}, {@link #updateYPositions(boolean)}, {@link #updateSizes(boolean)}, and
 * {@link #updateTextInfos(boolean)}.
 * </p>
 * <p>
 * As photos can be interacted with, the view listens for touch events and interprets them with a gesture detector.
 * </p>
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
    
    /**
     * {@link CameraActivity} that uses this view. Package scoped for faster access by inner classes.
     */
    final CameraActivity activity;
    
    /**
     * Layer containing the {@link #photoViews}.
     */
    private final SimpleAbsoluteLayout _photoLayer;
    
    /**
     * Layer containing the photo {@link #_borderViews}.
     */
    private final SimpleAbsoluteLayout _borderLayer;
    
    /**
     * {@link PhotoView}s for photos (currently and previously used). Key is the resource/photo id. Package scoped for
     * faster access by inner classes.
     */
    final SparseArray<PhotoView> photoViews = new SparseArray<PhotoView>();
    
    /**
     * {@link PhotoBorderView}s for photos (currently and previously used). Key is the resource/photo id.
     */
    private final SparseArray<PhotoBorderView> _borderViews = new SparseArray<PhotoBorderView>();
    
    // TODO maybe we can increase performance if we don't hide and show the views directly but rather put them into ViewStubs
    // which we then inflate
    
    /**
     * Resource/photo ids of the currently used photos (sorted from farthest to nearest).
     */
    public final ArrayList<Integer> photos = new ArrayList<Integer>();
    
    /**
     * {@link PhotoMetrics} of photos (currently and previously used). Key is the resource/photo id.
     */
    private final SparseArray<PhotoMetrics> _photoMetrics = new SparseArray<PhotoMetrics>();
    
    /**
     * Current viewing direction in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West)
     */
    private float _direction;
    
    /**
     * {@link GestureDetector} that detects the gestures used for interacting with the displayed photos.
     */
    private final GestureDetector _gestureDetector;
    
    /**
     * {@link SimpleOnGestureListener} that performs the interactions with the displayed photos.
     */
    private final SimpleOnGestureListener _gestureListener = new SimpleOnGestureListener() {
        
        /**
         * Gets called when a fling/swipe gesture is detected. Determines if the gesture was performed either on an
         * unminimized photo in down direction and minimizes it, or on an minimized photo in up direction and restores
         * it.
         * 
         * @param event1 Event at the start of the gesture.
         * @param event2 Event at the end of the gesture.
         * @return <code>true</code> if a photo is minimized/restored, or <code>false</code> if no action is performed.
         */
        @Override
        public boolean onFling(final MotionEvent event1, final MotionEvent event2, final float velocityX,
                               final float velocityY) {

            Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onFling");
            
            final float startX = event1.getX();
            final float startY = event1.getY();
            final float endX = event2.getX();
            final float endY = event2.getY();
            
            if (Math.abs(startX - endX) > Math.abs(startY - endY)) return false; // fling in horizontal direction
                
            /*
             * Detect which photo is flinged.
             */
            int flingedPhoto = 0; // id of the flinged photo
            if (startY < endY) {
                // fling down (minimize)
                int id;
                PhotoView photoView;
                final ListIterator<Integer> lit = photos.listIterator(photos.size());
                while (lit.hasPrevious()) { // iterate front to back
                    id = lit.previous();
                    photoView = photoViews.get(id);
                    if (photoView.isMinimized()) {
                        continue; // ignore minimized photos
                    }
                    if (photoView.getLeft() < startX && photoView.getRight() > startX && // on the view in horizontal direction
                        photoView.getTop() < startY && photoView.getBottom() > startY && // on the view in vertical direction
                        endY - startY > photoView.getHeight() / 3) { // fling movement should run for at least one third of the photo height
                        flingedPhoto = id;
                        break;
                    }
                }
            } else {
                // fling up (restore)
                PhotoView photoView;
                for (final int id : photos) { // back to front
                    photoView = photoViews.get(id);
                    if (!photoView.isMinimized()) {
                        continue; // ignore restored photos
                    }
                    if (photoView.getLeft() < startX && photoView.getRight() > startX && // on the view in horizontal direction
                        photoView.getTop() < startY && photoView.getBottom() > startY && // on the view in vertical direction
                        Math.abs(endY - startY) > photoView.getHeight() / 3) { // fling movement should run for at least one third of the photo height
                        flingedPhoto = id;
                        break;
                    }
                }
            }
            if (flingedPhoto == 0) return false; // no photo matched
                
            // minimize/restore photo view
            photoViews.get(flingedPhoto).setMinimized((startY < endY) ? true : false);
            
            // redraw photo
            redrawPhoto(flingedPhoto);
            
            // set number of occlusions for border alpha value
            setBorderOcclusions();
            
            return true;
        }
        
        /**
         * Gets called when a single tap gesture is completed. Determines if the gesture was performed on an minimized
         * photo restores it.
         * 
         * @param event Event of the gesture.
         * @return <code>true</code> if a photo is restored, or <code>false</code> if no action is performed.
         */
        @Override
        public boolean onSingleTapUp(final MotionEvent event) {

            Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onSingleTapUp");
            
            final float eventX = event.getX();
            final float eventY = event.getY();
            
            // tap tolerance
            int y_tap_tolerance = 0;
            if (PhotoMetrics.MINIMIZED_PHOTO_HEIGHT < PhotoCompassApplication.MIN_TAP_SIZE) {
                y_tap_tolerance = (PhotoCompassApplication.MIN_TAP_SIZE - PhotoMetrics.MINIMIZED_PHOTO_HEIGHT) / 2;
            }
            
            /*
             * Detect which photo is tapped on.
             */
            int tappedPhoto = 0; // id of the tapped photo
            PhotoView photoView;
            for (final int id : photos) { // back to front
                photoView = photoViews.get(id);
                if (!photoView.isMinimized()) {
                    continue; // ignore not minimized photos
                }
                if (photoView.getLeft() < eventX && photoView.getRight() > eventX && // on the view in horizontal direction
                    photoView.getTop() - y_tap_tolerance < eventY && photoView.getBottom() + y_tap_tolerance > eventY) { // on the view in vertical direction
                    tappedPhoto = id;
                    break;
                }
            }
            if (tappedPhoto == 0) return false; // no photo matched
                
            // set photo view restored
            photoViews.get(tappedPhoto).setMinimized(false);
            
            // redraw photo
            redrawPhoto(tappedPhoto);
            
            // set number of occlusions for border alpha value
            setBorderOcclusions();
            
            return true;
        }
    };
    
    /**
     * Constructor. Sets constants and creates the layers for photo and border views.
     * 
     * @param activity {@link CameraActivity}
     * @param availableWidth Maximum width for this view (the display width).
     * @param availableHeight Maximum height for this view (the display height minus status bar height and minus the
     *        height of the controls on the bottom)
     */
    public PhotosView(final CameraActivity activity, final int availableWidth, final int availableHeight) {

        super(activity);
        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView");
        
        AVAILABLE_WIDTH = availableWidth;
        AVAILABLE_HEIGHT = availableHeight;
        DEGREE_WIDTH = AVAILABLE_WIDTH / PhotoCompassApplication.CAMERA_HDEGREES;
        
        this.activity = activity;
        
        // set height constants
        MAX_PHOTO_HEIGHT = Math.round(MAX_PHOTO_HEIGHT_PERCENT * AVAILABLE_HEIGHT);
        MIN_PHOTO_HEIGHT = Math.round(MIN_PHOTO_HEIGHT_PERCENT * AVAILABLE_HEIGHT);
        
        _photoLayer = new SimpleAbsoluteLayout(activity);
        _photoLayer.setLayoutParams(new LayoutParams(AVAILABLE_WIDTH, AVAILABLE_HEIGHT, 0, 0));
        addView(_photoLayer);
        
        _borderLayer = new SimpleAbsoluteLayout(activity);
        _borderLayer.setLayoutParams(new LayoutParams(AVAILABLE_WIDTH, AVAILABLE_HEIGHT, 0, 0));
        addView(_borderLayer);
        
// XXX unknown constructor for 1.1
        _gestureDetector = new GestureDetector(activity, _gestureListener);
//        _gestureDetector = new GestureDetector(_gestureListener);
    }
    
    /**
     * Adds photos to the list of currently used photos. If a photo has been used before its views are set to
     * {@link View#VISIBLE} and updated. If a photo has not been used before, metrics and views are created and updated.
     * The photos are added to {@link #photos} and the z orders of the photo and border views are updated.
     * 
     * @param newPhotos ArrayList of photo/resource ids of the photos to add.
     * @param doRedraw Redraw after changes.
     */
    public void addPhotos(final ArrayList<Integer> newPhotos, final boolean doRedraw) {

        if (newPhotos.size() == 0) return; // nothing to do
            
        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: addPhotos: newPhotos.size() = " + newPhotos.size());
        
        boolean sizeChanged, xPosChanged, yPosChanged;
        for (final int id : newPhotos) {
            if (activity.getPhoto(id) == null) {
                continue;
            }
            
            if (_photoMetrics.get(id) != null) { // has been used before
            
                // show views
                photoViews.get(id).setVisibility(View.VISIBLE);
                _borderViews.get(id).setVisibility(View.VISIBLE);
                
            } else {
                
                // create metrics
                _photoMetrics.append(id, new PhotoMetrics());
                
                // create views
                final PhotoView photoView = new PhotoView(activity, id);
                photoViews.append(id, photoView);
                _photoLayer.addView(photoView);
                final PhotoBorderView borderView = new PhotoBorderView(activity);
                _borderViews.append(id, borderView);
                _borderLayer.addView(borderView);
            }
            
            // add to list of currently used photos
            photos.add(id);
            
            // update size and position and redraw if changed and wanted
            sizeChanged = _updatePhotoSize(id);
            xPosChanged = _updatePhotoXPosition(id);
            yPosChanged = _updatePhotoYPosition(id);
            if (doRedraw && (sizeChanged || xPosChanged || yPosChanged)) {
                redrawPhoto(id);
            }
        }
        
        // sort photo order
        _sortPhotos();
        
        // update views z orders
        for (final int id : photos) {
            photoViews.get(id).bringToFront();
            _borderViews.get(id).bringToFront();
        }
        
        // set number of occlusions for border alpha value
        setBorderOcclusions();
    }
    
    /**
     * Removes photos from the list of currently used photos. The views of the photos are set to {@link View#GONE} and
     * their minimized state is reset. The photos are removed {@link #photos}.
     * 
     * @param oldPhotos ArrayList of photo/resource ids of the photos to remove.
     */
    public void removePhotos(final ArrayList<Integer> oldPhotos) {

        if (oldPhotos.size() == 0) return; // nothing to do
            
        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: removePhotos: oldPhotos.size() = " + oldPhotos.size());
        
        PhotoView photoView;
        for (final int id : oldPhotos) {
            photoView = photoViews.get(id);
            
            // hide views
            photoView.setVisibility(View.GONE);
            _borderViews.get(id).setVisibility(View.GONE);
            
            // reset minimized state
            photoView.setMinimized(false);
        }
        
        // remove from list of currently used photos
        photos.removeAll(oldPhotos);
        
        // update photo order
        _sortPhotos();
        
        // set number of occlusions for border alpha value
        setBorderOcclusions();
    }
    
    /**
     * Sorts ({@link #photos}) based on their distance. Farthest to nearest.
     */
    private void _sortPhotos() {

        Collections.sort(photos, new Comparator<Integer>() {
            
            public int compare(final Integer id1, final Integer id2) {

                final Photo photo1 = activity.getPhoto(id1);
                final Photo photo2 = activity.getPhoto(id2);
                if (photo1 == null || photo2 == null) return 0;
                if (photo1.distance > photo2.distance) return -1;
                return 1;
            }
        });
    }
    
    /**
     * Sets the number of occluding photos for every border view. The border view decreases it's alpha value for every
     * photo that occludes the photo it belongs to. Package scoped for faster access by inner classes.
     */
    void setBorderOcclusions() {

        for (final int resId1 : photos) {
            final PhotoMetrics met1 = _photoMetrics.get(resId1);
            final boolean status = photoViews.get(resId1).isMinimized();
            int numOccludingPhotos = 0;
            int resId2;
            PhotoMetrics met2;
            final ListIterator<Integer> lit = photos.listIterator(photos.size());
            while (lit.hasPrevious()) { // iterate front to back
                resId2 = lit.previous();
                if (resId1 == resId2) {
                    break;
                }
                if (photoViews.get(resId2).isMinimized() != status) {
                    continue; // ignore photos with different status
                }
                met2 = _photoMetrics.get(resId2);
                if (((met2.top >= met1.top && met2.top <= met1.getBottom()) ||
                     (met2.getBottom() >= met1.top && met2.getBottom() <= met1.getBottom()) || (met2.top < met1.top && met2.getBottom() > met1.getBottom())) &&
                    ((met2.left >= met1.left && met2.left <= met1.getRight()) ||
                     (met2.getRight() >= met1.left && met2.getRight() <= met1.getRight()) || (met2.left < met1.left && met2.getRight() > met1.getRight()))) {
                    numOccludingPhotos++;
                }
            }
            _borderViews.get(resId1).setNumberOfOcclusions(numOccludingPhotos);
        }
    }
    
    /**
     * Updates the text overlay on the photo views.
     * 
     * @param doRedraw Redraw after changes.
     */
    public void updateTextInfos(final boolean doRedraw) {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: updateTextInfos");
        
        for (final int id : photos) {
            photoViews.get(id).updateText();
        }
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
        
        for (final int id : photos) {
            if (_updatePhotoXPosition(id) && doRedraw) {
                redrawPhoto(id);
            }
        }
    }
    
    /**
     * Updates the x position of a photo.
     * 
     * @param id Photo/resource id of the photo to be updated.
     * @return <code>true</code> if the x position has changed, or <code>false</code> if the x position has not changed.
     */
    private boolean _updatePhotoXPosition(final int id) {

        final Photo photo = activity.getPhoto(id);
        final PhotoMetrics metrics = _photoMetrics.get(id);
        if (metrics == null || photo == null) return false;
        
        // normalize direction and photo direction (direction set to 180 and photo direction correspondingly)
        final double normalizeOffset = 180 - _direction;
        final double photoDirection = (photo.direction + normalizeOffset) % 360;
        
        // calculate direction offset and x position
        final double directionOffset = photoDirection - 180;
        final int photoX = (int) Math.round(AVAILABLE_WIDTH / 2 + directionOffset * DEGREE_WIDTH - metrics.width / 2);
        
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: _uPXP: dir = "+_direction+", photoDir = "+photo.direction+", dirOff = "+directionOffset+", photoX = "+photoX);
        
        if (metrics.left == photoX) return false;
        
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
        
        for (final int id : photos) {
            if (_updatePhotoYPosition(id) && doRedraw) {
                redrawPhoto(id);
            }
        }
    }
    
    /**
     * Updates the y position of a photo. The y position of the photo is determined by calculating the ratio between the
     * altitude offset of the photo and the maximum visible height at the distance of the photo. This ratio is then
     * mapped to the available screen height.
     * 
     * @param id Photo/resource id of the photo to be updated.
     * @return <code>true</code> if the y position has changed, or <code>false</code> if the y position has not changed.
     */
    private boolean _updatePhotoYPosition(final int id) {

//        final Photo photo = photosModel.getPhoto(id);
//        if (photo == null) return false;
        final PhotoMetrics metrics = _photoMetrics.get(id);
        if (metrics == null) return false;
        
        // calculate y position
        // TODO take the roll value of the orientation sensor into account, then the CameraActivity wouldn't need to subtract the
        // BOTTOM_CONTROLS_HEIGHT from the available height anymore -- also see the getPhotos method of the Photo model for this
        final int photoHeight = metrics.height;
        final int photoY = (AVAILABLE_HEIGHT - photoHeight) / 2;
// disabled altitude offset indication - keep the code for future versions
//		final double photoAltOffset = photo.altOffset;
//        if (photoAltOffset != 0) {
//	        final double halfOfMaxVisibleMeters = Math.sin(Math.toRadians(PhotoCompassApplication.CAMERA_VDEGREES / 2)) * photo.distance /
//	        							    	  Math.cos(Math.toRadians(PhotoCompassApplication.CAMERA_VDEGREES / 2));
//	        int pixelOffset = (int) Math.round(Math.abs(photoAltOffset) / halfOfMaxVisibleMeters *
//	        								   (AVAILABLE_HEIGHT - photoHeight) / 2);
//	        if (photoAltOffset > 0) pixelOffset *= -1;
//	        photoY += pixelOffset;
////	        	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: altOffset = "+photoAltOffset+
////	        										   ", halfOfMaxVisibleMeters = "+halfOfMaxVisibleMeters+
////	        										   ", pixelOffset = "+pixelOffset);
//        }
        
        if (metrics.top == photoY) return false;
        
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
        
        for (final int id : photos) {
            if (_updatePhotoSize(id)) {
                _updatePhotoXPosition(id);
                _updatePhotoYPosition(id);
                if (doRedraw) {
                    redrawPhoto(id);
                }
            }
        }
    }
    
    /**
     * Updates the size of a photo. The photo height is a linear mapping of the ratio between photo distance and maximum
     * visible distance to the range between minimum photo height and maximum photo height. To calculate the photo width
     * the original aspect ratio of the photo is used.
     * 
     * @param id Photo/resource id of the photo to be updated.
     * @return <code>true</code> if the height has changed, or <code>false</code> if the height has not changed.
     */
    private boolean _updatePhotoSize(final int id) {

        final Photo photo = activity.getPhoto(id);
        final PhotoMetrics metrics = _photoMetrics.get(id);
        if (metrics == null || photo == null) return false;
        
        // calculate the photo height
        // linear scaling
//        final int photoHeight = (int) Math.round(MIN_PHOTO_HEIGHT + (MAX_PHOTO_HEIGHT - MIN_PHOTO_HEIGHT) *
//        								   		 (1 - photo.distance / (appModel.maxDistance - appModel.minDistance)));
        // exponential scaling
        final Settings settings = activity.getSettings();
        final double s = (Math.exp(1 - (photo.distance - settings.minDistance) /
                                   (settings.maxDistance - settings.minDistance)) - 1) /
                         Math.E;
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: _updatePhotoSize: distance = "+photo.distance+", scale = "+s);
        final int photoHeight = (int) Math.round(MIN_PHOTO_HEIGHT + (MAX_PHOTO_HEIGHT - MIN_PHOTO_HEIGHT) * s);
        
        if (metrics.height == photoHeight) return false;
        
        // calculate the photo width
        photo.determineOrigSize(getResources());
        final float scale = (float) photoHeight / (float) photo.origHeight;
        final int photoWidth = Math.round(photo.origWidth * scale);
        
        // update metrics
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: _updatePhotoSize: id = "+id+", width = "+photoWidth+", height = "+photoHeight);
        metrics.setWidth(photoWidth);
        metrics.setHeight(photoHeight);
        return true;
    }
    
    /**
     * Redraws the photo and border view for a photo by updating its {@link SimpleAbsoluteLayout.LayoutParams}. Package
     * scoped for faster access by inner classes.
     * 
     * @param id Photo/Resource id of the photo to be redrawn.
     */
    void redrawPhoto(final int id) {

        LayoutParams layoutParams;
        if (photoViews.get(id).isMinimized()) {
            layoutParams = _photoMetrics.get(id).getMinimizedLayoutParams(AVAILABLE_HEIGHT - 21); // available height minus space for the labels and padding
        } else {
            layoutParams = _photoMetrics.get(id).getLayoutParams();
        }
        
        // skip if photo has layout parameters, and is not and will not be visible on screen
        if (photoViews.get(id).getLayoutParams() != null &&
            (photoViews.get(id).getRight() < 0 && layoutParams.x + layoutParams.width < 0) || // left of screen
            (photoViews.get(id).getLeft() > AVAILABLE_WIDTH && layoutParams.x > AVAILABLE_WIDTH)) { // right of screen
            photoViews.get(id).setVisibility(View.GONE);
            _borderViews.get(id).setVisibility(View.GONE);
            return;
        }
        
        photoViews.get(id).setVisibility(View.VISIBLE);
        _borderViews.get(id).setVisibility(View.VISIBLE);
        
        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: redrawPhoto: id = " + id + ", x = " + layoutParams.x +
                                               ", y = " + layoutParams.y + ", width = " + layoutParams.width +
                                               ", height = " + layoutParams.height);
        
        photoViews.get(id).setLayoutParams(layoutParams);
        _borderViews.get(id).setLayoutParams(layoutParams);
    }
    
    /**
     * Clears the currently not needed photo and border views to save memory.
     */
    public void clearUnneededViews() {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: clearUnneededViews");
        int numLayers;
        View view;
        for (final ViewGroup layer : new ViewGroup[] {_photoLayer, _borderLayer}) {
            numLayers = layer.getChildCount();
            for (int i = 0; i < numLayers; i++) {
                view = layer.getChildAt(i);
                if (photos.contains(view.getId())) {
                    continue; // is currently needed
                }
                layer.removeView(view);
            }
        }
    }
    
    /**
     * Gets called when a touch events occurs. Passes the event on to the gesture detector.
     */
    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        final int action = event.getAction();
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: onTouchEvent: action = "+action);
        
        // pass on to gesture detector
        _gestureDetector.onTouchEvent(event);
        
        if (action == MotionEvent.ACTION_UP) {
            try {
//				Log.d(PhotoCompassApplication.LOG_TAG, "PhotosView: sleep");
                Thread.sleep(PhotoCompassApplication.SLEEP_AFTER_TOUCH_EVENT);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        return true;
    }
}
