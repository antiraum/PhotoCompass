package de.fraunhofer.fit.photocompass.views.overlays;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.activities.PhotoMapActivity;

/**
 * This Overlay is used by {@link PhotoMapActivity} to display the current viewing direction on the map.
 */
public final class ViewingDirectionOverlay extends Overlay {
	
	private GeoPoint _location; // current location
	private float _direction; // in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West).
	private boolean _directionSet = false;
	private Bitmap _bmp; // arrow bitmap

	private final Point _point = new Point();
	private final Matrix _matrix = new Matrix();
	private final Paint _paint = new Paint();
	
	public ViewingDirectionOverlay() {
		_paint.setStrokeWidth(5F);
		_paint.setColor(Color.BLUE);
	}
	
	/**
	 * Update current location.
	 * @param location
	 */
	public void updateLocation(final GeoPoint location) {
//        Log.d(PhotoCompassApplication.LOG_TAG, "ViewingDirectionOverlay: updateLocation");
		_location = location;
	}
	
	/**
	 * Update viewing direction.
	 * @param direction
	 */
	public void updateDirection(final float direction) {
//        Log.d(PhotoCompassApplication.LOG_TAG, "ViewingDirectionOverlay: updateDirection");
		_direction = direction;
		_directionSet = true;
	}
	
	/**
	 * Draws the overlay.
	 */
	@Override
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    	super.draw(canvas, mapView, shadow);
    	
		if (_location == null || ! _directionSet) return;
		
		// transform current position to point on canvas
		final Projection projection = mapView.getProjection();
		projection.toPixels(_location, _point);
 
		// create bitmap
//		if (_bmp == null) {
//	        _bmp = BitmapFactory.decodeResource(mapView.getResources(), R.drawable.maps_direction_arrow);
//		}
		
		canvas.rotate(_direction, _point.x, _point.y);
		
		// draw the marker
//		_matrix.reset();
//		_matrix.postTranslate(_point.x - _bmp.getWidth() / 2, _point.y - _bmp.getHeight() / 2);
//		_matrix.postRotate(_direction, _point.x, _point.y);
//        canvas.drawBitmap(_bmp, _matrix, null);
		
		canvas.drawLine(_point.x, _point.y, _point.x + 40, _point.y - 250, _paint);
		canvas.drawLine(_point.x, _point.y, _point.x - 40, _point.y - 250, _paint);
        
		canvas.rotate(-_direction, _point.x, _point.y);
	}
}
