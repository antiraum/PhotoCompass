package de.fraunhofer.fit.photocompass.model.data;

public class Photo {

	private int _resourceId;
	private float _lat;
	private float _lng;
	
	public Photo(int resourceId, float lat, float lng) {
		_resourceId = resourceId;
		_lat = lat;
		_lng = lng;
	}

	public int getResourceId() {
		return _resourceId;
	}

	public float getLat() {
		return _lat;
	}

	public float getLng() {
		return _lng;
	}
	
	/**
	 * Returns distance of the photo from the current location.
	 * 
	 * @param lat Latitude of the current location.
	 * @param lng Longitude of the current location.
	 * @return Distance in meters???
	 */
	public double getDistance(float lat, float lng) {
		// TODO calculate distance
		double dist = Math.random();
		if (dist < 0.3) dist = 0.3;
		return dist;
	}
	
	/**
	 * Returns angle at which the photo lies from the current location.
	 * 
	 * @param lat
	 * @param lng
	 * @return Angle (0 to 359). 0=North, 90=East, 180=South, 270=West
	 */
	public float getAngle(float lat, float lng) {
		// TODO calculate angle
		double random = Math.random();
		return (float)(random * 359);
	}
}
