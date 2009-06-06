package de.fraunhofer.fit.photocompass.views.overlays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Shader;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.activities.PhotoMapActivity;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.model.data.Photo;

/**
 * This Overlay is used by {@link PhotoMapActivity} to display the currently visible photos on the map.
 */
public final class PhotosOverlay extends Overlay {
	
	private static final float PHOTO_SIZE = 60;
	private static final float ARROW_WIDTH = 15;
	private static final float ARROW_HEIGHT = 15;
	private static final float BORDER_WIDTH = 2.1F; // stroke width of the border
	
	/**
	 * Ids of the currently used photos (sorted from north to south).
	 */
	private final ArrayList<Integer> _photos = new ArrayList<Integer>();

	/**
	 * Bitmaps of the currently and previously used photos (key is photo/resource id).
	 * The bitmaps are pre-scaled for better performance.
	 */
	private final SparseArray<Bitmap> _photoBitmaps = new SparseArray<Bitmap>();
	
	/**
	 * Paths for the borders of the currently and previously used photos (key is photo/resource id).
	 */
	private final SparseArray<Path> _borderPaths = new SparseArray<Path>();
	
    private Photos _photosModel;
	private final Paint _borderPaint = new Paint();
	private final Point _point = new Point();
	
	public PhotosOverlay() {
        _photosModel = Photos.getInstance();
        
        _borderPaint.setStrokeWidth(BORDER_WIDTH);
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

	/**
	 * Draws the photos and their borders on the canvas.
	 * For photos that have not been drawn before, a pre-scaled bitmap is created and saved in {@link #_photoBitmaps}.
	 */
    @Override
    public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    	super.draw(canvas, mapView, shadow);

		final Projection projection = mapView.getProjection();
		Photo photo;
		Bitmap bmp;
		float width, height, xScale, yScale, scale;
		Path path;
		final Path drawPath = new Path();
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
				_photoBitmaps.append(id, bmp);
			}
			
			// get position and dimension
			width = bmp.getWidth();
			height = bmp.getHeight();
			projection.toPixels(photo.getGeoPoint(), _point);
			
			path = _borderPaths.get(id);
			if (path == null) {
			
				// create border path
				path = new Path();
				path.rLineTo(width + BORDER_WIDTH, 0); // top border
				path.rLineTo(0, height + BORDER_WIDTH); // right border
				path.rLineTo(-1 * (width + BORDER_WIDTH - ARROW_WIDTH) / 2, 0); // bottom border
				path.rLineTo(-1 * ARROW_WIDTH / 2, ARROW_HEIGHT); // arrow right border
				path.rLineTo(-1 * ARROW_WIDTH / 2, -1 * ARROW_HEIGHT); // arrow left border
				path.rLineTo(-1 * (width + BORDER_WIDTH - ARROW_WIDTH) / 2, 0); // bottom border
				path.close(); // left border
				_borderPaths.append(id, path);
			}
			
			// draw border
			path.offset(_point.x - (width + BORDER_WIDTH) / 2, _point.y - (height + ARROW_HEIGHT + BORDER_WIDTH), drawPath);
			_borderPaint.setShader(new LinearGradient(_point.x - (width + BORDER_WIDTH) / 2, 0, _point.x, 0,
													  PhotoCompassApplication.DARK_ORANGE, PhotoCompassApplication.LIGHT_ORANGE,
													  Shader.TileMode.MIRROR));
			canvas.drawPath(drawPath, _borderPaint);
			
			// draw bitmap
			canvas.drawBitmap(bmp, _point.x - width / 2, _point.y - (height + ARROW_HEIGHT + BORDER_WIDTH / 2), null);
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
}
