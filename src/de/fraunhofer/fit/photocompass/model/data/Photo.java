package de.fraunhofer.fit.photocompass.model.data;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;

import com.google.android.maps.GeoPoint;

import de.fraunhofer.fit.photocompass.model.util.OutputFormatter;

/**
 * This class is a custom data type for photos.
 */
public final class Photo {

	private int _resourceId = 0;
	private int _photoId = 0; // MediaStore.Images.Thumbnails.IMAGE_ID of the photo
	private double _lat;
	private double _lng;
	private double _alt = 0;
	private long _date; // The date & time that the image was taken in units of milliseconds since January 1, 1970
	private Uri _thumbUri; // URI of the Thumbnail file
	
	private GeoPoint _geoPoint;
	private int _origWidth = 0;
	private int _origHeight = 0;
	private float _distance = 0;
	private double _direction = 0;
	private double _altOffset = 0;
	
	// position on the last updateDistanceAndDirection call
	private double _lastUpdateLat;
	private double _lastUpdateLng;
	private double _lastUpdateAlt;
	
	/**
	 * Constructor.
	 * 
	 * @param photoId  {@link MediaStore.Images.Thumbnails.IMAGE_ID} of the photo.
	 * @param thumbUri URI of the thumbnail file.
	 * @param lat	   Latitude of the photo.
	 * @param lng	   Longitude of the photo.
	 * @param alt	   Altitude of the photo.
	 * @param date	   The date & time that the image was taken in units of milliseconds since January 1, 1970.
	 */
	public Photo(final int photoId, final Uri thumbUri, final double lat, final double lng, final double alt, final long date) {
		_photoId = photoId;
		_lat = lat;
		_lng = lng;
		_alt = alt;
		_date = date;
		_thumbUri = thumbUri;
	}
	
	/**
	 * Constructor for a dummy photo.
	 * 
	 * @param resourceId Resource id of the photo file.
	 * @param lat		 Latitude of the photo.
	 * @param lng		 Longitude of the photo.
	 * @param alt		 Altitude of the photo.
	 * @param date	     The date & time that the image was taken in units of milliseconds since January 1, 1970.
	 */
	public Photo(final int resourceId, final double lat, final double lng, final double alt, final long date) {
		_resourceId = resourceId;
		_lat = lat;
		_lng = lng;
		_alt = alt;
		_date = date;
	}
	
	/**
	 * Get the unique id for the photo.
	 * 
	 * @return {@link MediaStore.Images.Thumbnails.IMAGE_ID} of the photo (if MediaStore photo), or
	 * 		   the resource id of the photo file (if dummy photo).
	 */
	public int getId() {
		return isDummyPhoto() ? _resourceId : _photoId;
	}
	
	/**
	 * Used to check if this is a photo from the MediaStore or a dummy photo.
	 * 
	 * @return <code>true</code> if it's a dummy photo (use resourceId to access it), or,
	 * 		   <code>false</code> if it's a MediaStore photo (use photoId and thumbUri to access it).
	 */
	public boolean isDummyPhoto() {
		return (_photoId == 0) ? true : false;
	}
	
	/**
	 * @return URI of the thumbnail file.
	 */
	public Uri getThumbUri() {
		return _thumbUri;
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
	 * @return GeoPoint of the photo location for use in Google maps.
	 */
	public GeoPoint getGeoPoint() {
		if (_geoPoint == null) _geoPoint = new GeoPoint((int)(_lat * 1E6), (int)(_lng * 1E6));
		return _geoPoint;
	}

	/**
	 * Determines the original size of the photo.
	 * Loads the bitmap data and sets {@link #_origWidth} and {@link #_origHeight}.
	 * Always call this method before accessing {@link #getOrigWidth()} or {@link #getOrigHeight()} to ensure
	 * the size has been determined. 
	 * 
	 * @param resources {@link Resources} of the application.
	 */
	public void determineOrigSize(final Resources resources) {
		if (_origWidth != 0 && _origHeight != 0) return; // already determined
		Bitmap bmp;
		if (isDummyPhoto()) {
			bmp = BitmapFactory.decodeResource(resources, _resourceId);
		} else {
			bmp = BitmapFactory.decodeFile(_thumbUri.getPath());
		}
		_origWidth = bmp.getWidth();
		_origHeight = bmp.getHeight();
		bmp.recycle();
//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photo: _origWidth = "+_origWidth+", _origHeight = "+_origHeight);
	}

	/**
	 * @return Original width of the photo.
	 */
	public int getOrigWidth() {
		return _origWidth;
	}

	/**
	 * @return Original height of the photo.
	 */
	public int getOrigHeight() {
		return _origHeight;
	}
	
	/**
	 * Updates {@link #_distance}, {@link #_direction}, and {@link #_altOffset} relative to a given position.
	 * Only performs calculations if the position parameters have changed since the last call.
	 * 
	 * @param currentLat Latitude of the current location.
	 * @param currentLng Longitude of the current location.
	 * @param currentAlt Altitude of the current location.
	 */
	public void updateDistanceDirectionAndAltitudeOffset(final double currentLat, final double currentLng, final double currentAlt) {
		
		if (_lastUpdateLat != currentLat && _lastUpdateLng != currentLng) { // position has changed
		
			// distance calculation
			float[] results = new float[1];
			Location.distanceBetween(currentLat, currentLng, _lat, _lng, results);
			_distance = results[0];
	
			// direction calculation - taken from com.google.android.radar.GeoUtils (http://code.google.com/p/apps-for-android)
	        final double lat1Rad = Math.toRadians(currentLat);
	        final double lat2Rad = Math.toRadians(_lat);
	        final double deltaLonRad = Math.toRadians(_lng - currentLng);
	        final double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
	        final double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad);
	        _direction = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
	        // end direction calculation
		}

		if (_lastUpdateAlt != currentAlt && _alt != 0) { // altitude has changed
			
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
	 * 
	 * @return Distance in meters.
	 */
	public float getDistance() {
		return _distance;
	}
	
	/**
	 * Returns the saved distance.
	 * 
	 * @return Distance as a formatted string for display.
	 */
	public String getFormattedDistance() {
		return OutputFormatter.formatDistance(_distance);
	}
	
	/**
	 * Returns the saved direction.
	 * 
	 * @return Direction in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West).
	 */
	public double getDirection() {
		return _direction;
	}
	
	/**
	 * Returns the saved altitude offset.
	 * 
	 * @return Altitude offset in meters.
	 */
	public double getAltOffset() {
		return _altOffset;
	}
	
	/**
	 * Returns the saved altitude offset.
	 * 
	 * @return Altitude offset as a formatted string for display.
	 */
	public String getFormattedAltOffset() {
		return OutputFormatter.formatAltOffset(_altOffset);
	}
	
	/**
	 * @return Age of the photo in milliseconds.
	 */
	public long getAge() {
		return System.currentTimeMillis() - _date;
	}
}
