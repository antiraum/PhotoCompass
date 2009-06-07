package de.fraunhofer.fit.photocompass.views.overlays;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.activities.PhotoMapActivity;

/**
 * This Overlay is used by {@link PhotoMapActivity} to display the current location on the map when a dummy location is used.
 */
public final class CustomMyLocationOverlay extends Overlay {
	
	private GeoPoint _location; // current location
	private Bitmap _bmp; // marker bitmap

	private final Point _point = new Point();
	
	/**
	 * Update current location.
	 * @param location
	 */
	public void update(final GeoPoint location) {
        Log.d(PhotoCompassApplication.LOG_TAG, "CustomMyLocationOverlay: update");
		_location = location;
	}
	
	/**
	 * Draws the overlay.
	 */
	@Override
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    	super.draw(canvas, mapView, shadow);
    	
		if (_location == null) return;
		
		// transform current position to point on canvas
		final Projection projection = mapView.getProjection();
		projection.toPixels(_location, _point);
 
		// create bitmap
		if (_bmp == null) {
	        _bmp = BitmapFactory.decodeResource(mapView.getResources(), R.drawable.maps_current_position);
		}
		
		// draw the marker
        canvas.drawBitmap(_bmp, _point.x - _bmp.getWidth() / 2, _point.y - _bmp.getHeight() / 2, null);
	}
}
