package de.fraunhofer.fit.photocompass.views.overlays;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;

/**
 * 
 */
public final class CustomMyLocationOverlay extends Overlay {
	
	private GeoPoint _location;
	
	public void update(final GeoPoint location) {
        Log.d(PhotoCompassApplication.LOG_TAG, "CustomMyLocationOverlay: update");
		_location = location;
	}
	
	@Override
	public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
    	super.draw(canvas, mapView, shadow);
    	
		if (_location == null) return;
		
		// Transform geoposition to Point on canvas
		Projection projection = mapView.getProjection();
		Point point = new Point();
		projection.toPixels(_location, point);
 
		// the circle to mark the spot
        Bitmap bitmap = BitmapFactory.decodeResource(mapView.getResources(), R.drawable.maps_current_position);
        canvas.drawBitmap(bitmap, point.x - bitmap.getWidth() / 2, point.y - bitmap.getHeight() / 2, new Paint());
        bitmap.recycle();
	}
}
