package de.fraunhofer.fit.photocompass.views.overlays;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.activities.PhotoMapActivity;

/**
 * This Overlay is used by {@link PhotoMapActivity} to display the current viewing direction on the map.
 */
public final class ViewingDirectionOverlay extends Overlay {
    
    private static final int OVERLAY_HEIGHT = PhotoCompassApplication.DISPLAY_HEIGHT -
                                              PhotoCompassApplication.STATUSBAR_HEIGHT;
    private static final int OVERLAY_HALF_WIDTH = (int) (Math.sin(Math.toRadians(PhotoCompassApplication.CAMERA_HDEGREES / 2)) * OVERLAY_HEIGHT);
    
    private GeoPoint _location; // current location
    private float _direction; // in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West).
    private boolean _directionSet = false;
    
    private final Point _point = new Point();
    private final Paint _borderPaint = new Paint();
    private final Paint _fillPaint = new Paint();
    
    /**
     * Constructor
     */
    public ViewingDirectionOverlay() {

        _borderPaint.setStrokeWidth(2.1F);
        _borderPaint.setColor(Color.BLUE);
        _fillPaint.setColor(Color.BLUE);
        _fillPaint.setAlpha(50);
        _fillPaint.setStyle(Paint.Style.FILL);
    }
    
    /**
     * Update current location.
     * 
     * @param location
     */
    public void updateLocation(final GeoPoint location) {

//        Log.d(PhotoCompassApplication.LOG_TAG, "ViewingDirectionOverlay: updateLocation");
        _location = location;
    }
    
    /**
     * Update viewing direction.
     * 
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
        
        if (_location == null || !_directionSet) return;
        
        // transform current position to point on canvas
        final Projection projection = mapView.getProjection();
        projection.toPixels(_location, _point);
        
        canvas.rotate(_direction, _point.x, _point.y);
        
        // draw the marker
        final Path path = new Path();
        path.moveTo(_point.x, _point.y);
        path.lineTo(_point.x + OVERLAY_HALF_WIDTH, _point.y - OVERLAY_HEIGHT);
        path.lineTo(_point.x - OVERLAY_HALF_WIDTH, _point.y - OVERLAY_HEIGHT);
        path.close(); // left border
        canvas.drawPath(path, _fillPaint);
        canvas.drawLine(_point.x, _point.y, _point.x + OVERLAY_HALF_WIDTH, _point.y - OVERLAY_HEIGHT, _borderPaint);
        canvas.drawLine(_point.x, _point.y, _point.x - OVERLAY_HALF_WIDTH, _point.y - OVERLAY_HEIGHT, _borderPaint);
        
        canvas.rotate(-_direction, _point.x, _point.y);
    }
}
