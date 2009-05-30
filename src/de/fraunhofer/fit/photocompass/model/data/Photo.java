package de.fraunhofer.fit.photocompass.model.data;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;

public class Photo {

	private int _resourceId;
	private double _lat;
	private double _lng;
	private double _alt;
	private int _origWidth;
	private int _origHeight;
	private float _distance;
	private double _direction;
	private double _altOffset;
	
	// position on the last updateDistanceAndDirection call
	private double _lastUpdateLat;
	private double _lastUpdateLng;
	private double _lastUpdateAlt;
	
	public Photo(int resourceId, double lat, double lng, double alt) {
		_resourceId = resourceId;
		_lat = lat;
		_lng = lng;
		_alt  = alt;
		_origWidth = 0;
		_origHeight = 0;
		_distance = 0f;
		_direction = 0f;
	}

	public int getResourceId() {
		return _resourceId;
	}

	public double getLat() {
		return _lat;
	}

	public double getLng() {
		return _lng;
	}

	public void determineOrigSize(Resources resources) {
		if (_origWidth != 0 && _origHeight != 0) return; // already determined
		Bitmap bitmap = BitmapFactory.decodeResource(resources, _resourceId);
		_origWidth = bitmap.getWidth();
		_origHeight = bitmap.getHeight();
		bitmap = null;
    	Log.d(PhotoCompassApplication.LOG_TAG, "Photo: _origWidth = "+_origWidth+", _origHeight = "+_origHeight);
	}

	public int getOrigWidth() {
		return _origWidth;
	}

	public int getOrigHeight() {
		return _origHeight;
	}
	
	/**
	 * Set the distance and direction to a given position.
	 * 
	 * @param lat Latitude of the current location
	 * @param lng Longitude of the current location
	 * @return
	 */
	public void updateDistanceDirectionAndAltitudeOffset(double lat, double lng, double alt) {
		
		// this is are expensive calculations, so we double check here if they really have to be done
		if (_lastUpdateLat == lat && _lastUpdateLng == lng && _lastUpdateAlt == alt) return;
		_lastUpdateLat = lat;
		_lastUpdateLng = lng;
		_lastUpdateAlt = alt;

//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photo: updateDistanceAndDirection");
		
		// distance calculation
		float[] results = new float[1];
		Location.distanceBetween(lat, lng, _lat, _lng, results);
		_distance = results[0];

		// direction calculation - taken from com.google.android.radar.GeoUtils (http://code.google.com/p/apps-for-android)
        double lat1Rad = Math.toRadians(lat);
        double lat2Rad = Math.toRadians(_lat);
        double deltaLonRad = Math.toRadians(_lng - lng);
        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad);
        _direction = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        // end direction calculation

		// altitude offset calculation
        _altOffset = _alt - alt;
	}
	
	/**
	 * Returns the saved distance
	 * 
	 * @return Distance in meters
	 */
	public float getDistance() {
		return _distance;
	}
	
	/**
	 * Returns the saved direction
	 * 
	 * @return Direction in degrees (in degrees 0 - 360) 0 = North, 90 = East, 180 = South, 270 = West
	 */
	public double getDirection() {
		return _direction;
	}
	
	/**
	 * Returns the saved altitude offset
	 * 
	 * @return Altitude offset (in meters)
	 */
	public double getAltOffset() {
		return _altOffset;
	}
	
	/**
	 * 
	 * @return Age of the photo in ... (maybe minutes or seconds)
	 */
	public int getAge() {
		// FIXME 
		return 24 * 60;
	}
}
