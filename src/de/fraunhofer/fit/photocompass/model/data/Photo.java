package de.fraunhofer.fit.photocompass.model.data;

import android.location.Location;

public class Photo {

	private int _resourceId;
	private double _lat;
	private double _lng;
	private float _distance;
	private float _direction;
	
	public Photo(int resourceId, float lat, float lng) {
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
	 * Set the distance to a given position.
	 * 
	 * @param lat Latitude of the current location
	 * @param lng Longitude of the current location
	 * @return Distance in meters
	 */
	public float updateDistance(double lat, double lng) {
		
		// calculate the distance
		float[] results = new float[] {};
		Location.distanceBetween(lat, lng, _lat, _lng, results);
		_distance = results[0];
		
		// return result
		return _distance;
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
	 * Set the direction to a given position.
	 * 
	 * @param lat Latitude of the current location
	 * @param lng Longitude of the current location
	 * @return Azimuth in degrees (0 - 360) 0 = North, 90 = East, 180 = South, 270 = West 
	 */
	public float updateDirection(double lat, double lng) {
		
		// TODO
		
		// dummy calculation
		_direction = (float)(Math.random() * 359);
		
		return _direction;
	}
	
	/**
	 * Returns the saved direction
	 * 
	 * @return Azimuth in degrees
	 */
	public float getDirection() {
		return _direction;
	}
}
