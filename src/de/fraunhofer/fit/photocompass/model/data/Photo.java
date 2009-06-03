package de.fraunhofer.fit.photocompass.model.data;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

/**
 * This class is a custom data type for photos.
 */
public final class Photo {

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
	
	/**
	 * Constructor.
	 * @param resourceId Resource id of the photo.
	 * @param lat		 Latitude of the photo.
	 * @param lng		 Longitude of the photo.
	 * @param alt		 Altitude of the photo.
	 */
	public Photo(final int resourceId, final double lat, final double lng, final double alt) {
		_resourceId = resourceId;
		_lat = lat;
		_lng = lng;
		_alt = alt;
		_origWidth = 0;
		_origHeight = 0;
		_distance = 0f;
		_direction = 0f;
	}

	/**
	 * @return Resource id of the photo.
	 */
	public int getResourceId() {
		return _resourceId;
	}

	/**
	 * @return Latitude of the photo.
	 */
	public double getLat() {
		return _lat;
	}

	/**
	 * @return Longitude of the photo.
	 */
	public double getLng() {
		return _lng;
	}

	/**
	 * Determines the original size of the resource.
	 * Loads the bitmap data of the resource and sets {@link #_origWidth} and {@link #_origHeight}.
	 * Always call this method before accessing {@link #getOrigWidth()} or {@link #getOrigHeight()} to ensure
	 * the size has been determined. 
	 * @param resources {@link Resources} of the application.
	 */
	public void determineOrigSize(final Resources resources) {
		if (_origWidth != 0 && _origHeight != 0) return; // already determined
		Bitmap bitmap = BitmapFactory.decodeResource(resources, _resourceId);
		_origWidth = bitmap.getWidth();
		_origHeight = bitmap.getHeight();
		bitmap = null;
//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photo: _origWidth = "+_origWidth+", _origHeight = "+_origHeight);
	}

	/**
	 * @return Original width of the photo resource.
	 */
	public int getOrigWidth() {
		return _origWidth;
	}

	/**
	 * @return Original height of the photo resource.
	 */
	public int getOrigHeight() {
		return _origHeight;
	}
	
	/**
	 * Updates {@link #_distance}, {@link #_direction}, and {@link #_altOffset} relative to a given position.
	 * Only performs calculations if the position parameters have changed since the last call.
	 * @param lat Latitude of the current location.
	 * @param lng Longitude of the current location.
	 * @param alt Altitude of the current location.
	 */
	public void updateDistanceDirectionAndAltitudeOffset(final double currentLat, final double currentLng, final double currentAlt) {
		
		if (_lastUpdateLat != currentLat && _lastUpdateLng != currentLng) { // position has changed
		
			// distance calculation
			float[] results = new float[1];
			Location.distanceBetween(currentLat, currentLng, _lat, _lng, results);
			_distance = results[0];
	
			// direction calculation - taken from com.google.android.radar.GeoUtils (http://code.google.com/p/apps-for-android)
	        double lat1Rad = Math.toRadians(currentLat);
	        double lat2Rad = Math.toRadians(_lat);
	        double deltaLonRad = Math.toRadians(_lng - currentLng);
	        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
	        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad);
	        _direction = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
	        // end direction calculation
		}

		if (_lastUpdateAlt != currentAlt) { // altitude has changed
			
			// altitude offset calculation
	        _altOffset = (currentAlt == 0) ? 0 // no valid altitude -> no valid offset possible
	        							   : _alt - currentAlt;
		}
		
		_lastUpdateLat = currentLat;
		_lastUpdateLng = currentLng;
		_lastUpdateAlt = currentAlt;
		
//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photo: updateDistanceAndDirection: resourceId = "+_resourceId+", _distance = "+_distance+", _direction = "+_direction+", _altOffset = "+_altOffset);
	}
	
	/**
	 * Returns the saved distance.
	 * @return Distance in meters.
	 */
	public float getDistance() {
		return _distance;
	}
	
	/**
	 * Returns the saved direction.
	 * @return Direction in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West).
	 */
	public double getDirection() {
		return _direction;
	}
	
	/**
	 * Returns the saved altitude offset.
	 * @return Altitude offset in meters.
	 */
	public double getAltOffset() {
		return _altOffset;
	}
	
	/**
	 * @return Age of the photo in ... (maybe minutes or seconds)
	 */
	public int getAge() {
		// FIXME 
		return 24 * 60;
	}
}
