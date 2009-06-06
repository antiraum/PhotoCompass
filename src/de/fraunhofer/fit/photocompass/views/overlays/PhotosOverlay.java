package de.fraunhofer.fit.photocompass.views.overlays;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
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
	private static final float BORDER_WIDTH = 2; // stroke width of the border
	
	/**
	 * Ids of the currently used photos (sorted from north to south).
	 */
	private final ArrayList<Integer> _photos = new ArrayList<Integer>();

	/**
	 * Bitmaps of the currently and previously used photos (key is photo/resource id).
	 * The bitmaps are pre-scaled for better performance.
	 */
	private final SparseArray<Bitmap> _photoBitmaps = new SparseArray<Bitmap>();
	
    private Photos _photosModel;
	private final Paint _borderPaint = new Paint();
	
	public PhotosOverlay() {
        _photosModel = Photos.getInstance();
        _borderPaint.setColor(PhotoCompassApplication.ORANGE);
//        _borderPaint.setStrokeWidth(BORDER_WIDTH);
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
		final Path path = new Path();
		final Point point = new Point();
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
			projection.toPixels(photo.getGeoPoint(), point);
			
			// draw border
			path.reset();
			path.moveTo(point.x - 1/3 * width - BORDER_WIDTH, point.y - 4/3 * height - 2 * BORDER_WIDTH);
			path.rLineTo(width + 2 * BORDER_WIDTH, 0);
			path.rLineTo(0, height + 2 * BORDER_WIDTH);
			path.rLineTo(-1 * (width / 2 + BORDER_WIDTH), 0);
			path.rLineTo(-1 * width / 6, 1/3 * height);
			path.rLineTo(-1 * width / 6, -1 * 1/3 * height);
			path.rLineTo(-1 * (width / 6 + BORDER_WIDTH), 0);
			path.rLineTo(0, -1 * (height + 2 * BORDER_WIDTH));
			path.close();
			canvas.drawPath(path, _borderPaint);
			
			// draw bitmap
			canvas.drawBitmap(bmp, point.x - 1/3 * width, point.y - 4/3 * height - BORDER_WIDTH, null);
			
//			break;
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
