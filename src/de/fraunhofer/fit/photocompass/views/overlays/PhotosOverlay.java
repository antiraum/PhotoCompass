package de.fraunhofer.fit.photocompass.views.overlays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.activities.PhotoMapActivity;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.model.data.PhotoMetrics;

/**
 * This Overlay is used by {@link PhotoMapActivity} to display the currently visible photos on the map.
 */
public final class PhotosOverlay extends Overlay {
	
	private static final float PHOTO_SIZE = 60;
//	private static final float ARROW_WIDTH = 15;
	private static final float ARROW_HEIGHT = 18;
	private static final float BORDER_WIDTH = 2.1F; // stroke width of the border
	
	/**
	 * Ids of the currently used photos (sorted from north to south).
	 */
	private final ArrayList<Integer> _photos = new ArrayList<Integer>();

	/**
	 * {@link PhotoMetrics} of photos (currently and previously used).
	 * Key is the resource/photo id.
	 */
	private final SparseArray<PhotoMetrics> _photoMetrics = new SparseArray<PhotoMetrics>();
	
	/**
	 * Ids of the currently minimized photos.
	 */
	private final ArrayList<Integer> _minimizedPhotos = new ArrayList<Integer>();
	
	/**
	 * Bitmaps for currently and previously used photos (key is photo/resource id).
	 * The bitmaps are pre-scaled for better performance.
	 */
	private final SparseArray<Bitmap> _photoBitmaps = new SparseArray<Bitmap>();
	
//	/**
//	 * Border paths for currently and previously used photos (key is photo/resource id).
//	 */
//	private final SparseArray<Path> _borderPaths = new SparseArray<Path>();
//	
//	/**
//	 * Minimized border paths for currently and previously used photos (key is photo/resource id).
//	 */
//	private final SparseArray<Path> _minimizedBorderPaths = new SparseArray<Path>();
	
    private Photos _photosModel;
	private final Paint _borderPaint = new Paint();
	private Bitmap _borderBmp;
	private Bitmap _arrowBmp;
	
	public PhotosOverlay() {
        _photosModel = Photos.getInstance();
        
        _borderPaint.setStrokeWidth(BORDER_WIDTH);
        _borderPaint.setColor(PhotoCompassApplication.ORANGE);
        _borderPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	}

	/**
	 * Adds photos to the list of currently used photos.
	 * 
	 * @param newPhotos ArrayList of photo/resource ids of the photos to add.
	 */
	public void addPhotos(final ArrayList<Integer> newPhotos) {
    	if (newPhotos.size() == 0) return; // nothing to do

    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosOverlay: addPhotos: newPhotos.size() = "+newPhotos.size());
    	
    	// add to list of currently used photos
		_photos.addAll(newPhotos);
		
		// sort photo order
		_sortPhotos();
	}

	/**
	 * Removes photos from the list of currently used photos.
	 * 
	 * @param oldPhotos ArrayList of photo/resource ids of the photos to remove.
	 */
	public void removePhotos(final ArrayList<Integer> oldPhotos) {
    	if (oldPhotos.size() == 0) return; // nothing to do

    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotosOverlay: removePhotos: oldPhotos.size() = "+oldPhotos.size());
    	
    	// remove from list of currently used photos
		_photos.removeAll(oldPhotos);

		// update photo order
		_sortPhotos();
	}
	
	/**
	 * Sorts ({@link #_photos}) based on their latitude. North to south.
	 */
	private void _sortPhotos() {
		Collections.sort(_photos, new Comparator<Integer>() {
	    	public int compare(final Integer id1, final Integer id2) {
	    		final Photo photo1 = _photosModel.getPhoto(id1);
	    		final Photo photo2 = _photosModel.getPhoto(id2);
	    		if (photo1 == null || photo2 == null) return 0;
	    		if (photo1.getGeoPoint().getLatitudeE6() > photo2.getGeoPoint().getLatitudeE6()) return -1;
	    		return 1;
	        }
	    });
	}
	
	/**
	 * @return The photos currently used by the view.
	 */
	public ArrayList<Integer> getPhotos() {
		return _photos;
	}
	
	// variables for draw
	private final Point _point = new Point();
//	private final Path _drawPath = new Path();
	private final Matrix _matrix = new Matrix();

	/**
	 * Draws the photos and their borders on the canvas.
	 * For photos that have not been drawn before, a pre-scaled bitmap is created and saved in {@link #_photoBitmaps}.
	 */
    @Override
    public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    	super.draw(canvas, mapView, shadow);
    	
//    	if (_borderBmp == null) _borderBmp = BitmapFactory.decodeResource(mapView.getResources(), R.drawable.maps_photo_border);
    	if (_arrowBmp == null) _arrowBmp = BitmapFactory.decodeResource(mapView.getResources(), R.drawable.maps_photo_arrow);

		final Projection projection = mapView.getProjection();
		Photo photo;
		Bitmap bmp;
		float width, height, xScale, yScale, scale;
		PhotoMetrics metrics;
//		Path path;
		float bmpXPos, bmpYPos;
		for (int id : _photos) {

			// get photo
			photo = _photosModel.getPhoto(id);
			if (photo == null) continue;

			bmp = _photoBitmaps.get(id);
			if (bmp == null) {
				
				// create pre-scaled bitmap
				if (photo.isDummyPhoto()) {
					bmp = BitmapFactory.decodeResource(mapView.getResources(), id);
				} else {
					bmp = BitmapFactory.decodeFile(photo.getThumbUri().getPath());
				}
				width = bmp.getWidth();
				height = bmp.getHeight();
				xScale = PHOTO_SIZE / width;
				yScale = PHOTO_SIZE / height;
				scale = (xScale > yScale) ? xScale : yScale;
				bmp = Bitmap.createScaledBitmap(bmp, Math.round(width * scale), Math.round(height * scale), true);
				
				// save bitmap
				_photoBitmaps.append(id, bmp);
			}	
			
			// get position and dimension
			width = bmp.getWidth();
			height = _minimizedPhotos.contains(id) ? PhotoMetrics.MAPS_MINIMIZED_PHOTO_HEIGHT : bmp.getHeight();
			projection.toPixels(photo.getGeoPoint(), _point);
			
			metrics = _photoMetrics.get(id);
			if (metrics == null) {
				
				// create metrics
				metrics = new PhotoMetrics();
				_photoMetrics.append(id, metrics);
			}
			
			// update metrics
			metrics.setLeft(Math.round(_point.x - (width + BORDER_WIDTH) / 2));
			metrics.setTop(Math.round(_point.y - (height + ARROW_HEIGHT + BORDER_WIDTH)));
			metrics.setWidth(Math.round(width + BORDER_WIDTH));
			metrics.setHeight(Math.round(height + BORDER_WIDTH));

//			path = _minimizedPhotos.contains(id) ? _minimizedBorderPaths.get(id) : _borderPaths.get(id);
//			if (path == null) {
//			
//				// create border path
//				path = new Path();
//				path.rLineTo(width + BORDER_WIDTH, 0); // top border
//				path.rLineTo(0, height + BORDER_WIDTH); // right border
//				path.rLineTo(-1 * (width + BORDER_WIDTH - ARROW_WIDTH) / 2, 0); // bottom border
//				path.rLineTo(-1 * ARROW_WIDTH / 2, ARROW_HEIGHT); // arrow right border
//				path.rLineTo(-1 * ARROW_WIDTH / 2, -1 * ARROW_HEIGHT); // arrow left border
//				path.rLineTo(-1 * (width + BORDER_WIDTH - ARROW_WIDTH) / 2, 0); // bottom border
//				path.close(); // left border
//				
//				// save path
//				if (_minimizedPhotos.contains(id)) {
//					_minimizedBorderPaths.append(id, path);
//				} else {
//					_borderPaths.append(id, path);
//				}
//			}
			
			// draw arrow bitmap
			canvas.drawBitmap(_arrowBmp, _point.x - _arrowBmp.getWidth() / 2, _point.y - ARROW_HEIGHT, null);
			
			// draw border
//			path.offset(metrics.getLeft(), metrics.getTop(), _drawPath);
//			_borderPaint.setShader(new LinearGradient(_point.x - (width + BORDER_WIDTH) / 2, 0, _point.x, 0,
//													  PhotoCompassApplication.DARK_ORANGE, PhotoCompassApplication.LIGHT_ORANGE,
//													  Shader.TileMode.MIRROR));
//			canvas.drawPath(_drawPath, _borderPaint);
			canvas.drawRect(metrics.getLeft(), metrics.getTop(), metrics.getRight(), metrics.getBottom(), _borderPaint);
			
			// draw arrow bitmap
			canvas.drawBitmap(_arrowBmp, _point.x - _arrowBmp.getWidth() / 2, _point.y - ARROW_HEIGHT, null);
			
			// draw bitmap
			_matrix.reset();
			bmpXPos = _point.x - width / 2;
			bmpYPos = _point.y - (height + ARROW_HEIGHT + BORDER_WIDTH / 2);
			_matrix.postTranslate(bmpXPos, bmpYPos);
			if (_minimizedPhotos.contains(id)) _matrix.postScale(1, height / bmp.getHeight(), bmpXPos, bmpYPos);
			canvas.drawBitmap(bmp, _matrix, null);
        }
    }
    
    /**
     * Removes the currently not needed bitmaps to save memory.
     */
    public void clearUnneededBitmaps() {
    	for (int i = 0; i < _photoBitmaps.size(); i++) {
    		if (_photos.contains(_photoBitmaps.keyAt(i))) continue; // currently needed
    		_photoBitmaps.remove(_photoBitmaps.keyAt(i));
    	}
    }
    
    /**
     * Handle a tap event.
     * A tap on a photo minimizes it. A tap on a minimized photo restores it.
     * @return <code>true</code> if the tap was handled by this overlay, or
     * 		   <code>false</code> if the tap was not handled with.
     */
    public boolean onTap(final GeoPoint geoPoint, final MapView mapView) {

    	// translate geopoint
		final Projection projection = mapView.getProjection();
		projection.toPixels(geoPoint, _point);

    	// tap tolerance
    	int y_tap_tolerance = 0;
    	if (PhotoMetrics.MAPS_MINIMIZED_PHOTO_HEIGHT < PhotoCompassApplication.MIN_TAP_SIZE)
    		y_tap_tolerance = (PhotoCompassApplication.MIN_TAP_SIZE - PhotoMetrics.MAPS_MINIMIZED_PHOTO_HEIGHT) / 2;
    	
    	/*
    	 * Detect which photo is tapped on.
    	 */
    	Integer tappedPhoto = 0; // id of the tapped photo
    	int id;
    	PhotoMetrics metrics;
		ListIterator<Integer> lit = _photos.listIterator(_photos.size());
        while (lit.hasPrevious()) { // iterate south to north
        	id = lit.previous();
        	metrics = _photoMetrics.get(id);
    		if (metrics.getLeft() < _point.x && metrics.getRight() > _point.x && // on the photo in horizontal direction
				metrics.getTop() - y_tap_tolerance < _point.y && metrics.getBottom() + y_tap_tolerance > _point.y) { // on the photo in vertical direction
    			tappedPhoto = id;
    			break;
    		}
    	}
        if (tappedPhoto == 0) return false; // no photo matched
    	
    	// minimize/restore photo
        if (_minimizedPhotos.contains(tappedPhoto)) {
        	_minimizedPhotos.remove(tappedPhoto);
        } else {
        	_minimizedPhotos.add(tappedPhoto);
        }
        
        return true;
    }
}
