package de.fraunhofer.fit.photocompass.model.data;

import android.location.Location;

public class Photo {

	private int _resourceId;
	private double _lat;
	private double _lng;
	private float _distance;
	private double _direction;
	
	public Photo(int resourceId, double lat, double lng) {
		_resourceId = resourceId;
		_lat = lat;
		_lng = lng;
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
	
	/**
	 * Set the distance and direction to a given position.
	 * 
	 * @param lat Latitude of the current location
	 * @param lng Longitude of the current location
	 * @return
	 */
	public void updateDistanceAndDirection(double lat, double lng) {
		
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

		// TODO check how we can use the altitude for the direction
        // (I'm not sure the altitude is in the meta data of the photos - check this first)
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
	 * 
	 * @return Age of the photo in ... (maybe minutes or seconds)
	 */
	public int getAge() {
		// TODO 
		return 24 * 60;
	}
}
