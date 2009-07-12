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
	public double lat; // Latitude of the photo
	public double lng; // Longitude of the photo
	private double _alt;
	private long _date; // The date & time that the image was taken in units of milliseconds since January 1, 1970
	public Uri thumbUri; // URI of the Thumbnail file
	
	private GeoPoint _geoPoint;
	public int origWidth = 0; // Original width of the photo
	public int origHeight = 0; // Original height of the photo
	public float distance = 0; // Saved distance in meters
	public double direction = 0; // Saved direction in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West)
	public double altOffset = 0; // Saved altitude offset in meters
	
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
		this.lat = lat;
		this.lng = lng;
		_alt = alt;
		_date = date;
		this.thumbUri = thumbUri;
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
		this.lat = lat;
		this.lng = lng;
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
	 * @return GeoPoint of the photo location for use in Google maps.
	 */
	public GeoPoint getGeoPoint() {
		if (_geoPoint == null) _geoPoint = new GeoPoint((int)(lat * 1E6), (int)(lng * 1E6));
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
		if (origWidth != 0 && origHeight != 0) return; // already determined
		Bitmap bmp;
		if (isDummyPhoto()) {
			bmp = BitmapFactory.decodeResource(resources, _resourceId);
		} else {
			bmp = BitmapFactory.decodeFile(thumbUri.getPath());
		}
		if (bmp == null) { // file does not exists
	    	return;
		}
		origWidth = bmp.getWidth();
		origHeight = bmp.getHeight();
		bmp.recycle();
		bmp = null;
//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photo: _origWidth = "+_origWidth+", _origHeight = "+_origHeight);
	}
	
	/**
	 * Updates {@link #distance}, {@link #direction}, and {@link #altOffset} relative to a given position.
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
			Location.distanceBetween(currentLat, currentLng, lat, lng, results);
			distance = results[0];
	
			// direction calculation - taken from com.google.android.radar.GeoUtils (http://code.google.com/p/apps-for-android)
	        final double lat1Rad = Math.toRadians(currentLat);
	        final double lat2Rad = Math.toRadians(lat);
	        final double deltaLonRad = Math.toRadians(lng - currentLng);
	        final double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
	        final double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad);
	        direction = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
	        // end direction calculation
		}

// disabled altitude offset calculation - keep the code for later releases
/*		if (_lastUpdateAlt != currentAlt && _alt != 0) { // altitude has changed
			
			// altitude offset calculation
	        altOffset = (currentAlt == 0) ? 0 // no valid altitude -> no valid offset possible
	        							  : _alt - currentAlt;
		} */
		
		_lastUpdateLat = currentLat;
		_lastUpdateLng = currentLng;
		_lastUpdateAlt = currentAlt;
		
//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photo: updateDistanceAndDirection: id = "+getId()+", distance = "+distance+", direction = "+direction+", altOffset = "+altOffset);
	}
	
	/**
	 * Returns the saved distance.
	 * 
	 * @return Distance as a formatted string for display.
	 */
	public String getFormattedDistance() {
		return OutputFormatter.formatDistance(distance);
	}
	
	/**
	 * Returns the saved altitude offset.
	 * 
	 * @return Altitude offset as a formatted string for display.
	 */
	public String getFormattedAltOffset() {
		return OutputFormatter.formatAltOffset(altOffset);
	}
	
	/**
	 * @return Age of the photo in milliseconds.
	 */
	public long getAge() {
		return System.currentTimeMillis() - _date;
	}
	
	/**
	 * Returns the saved age.
	 * 
	 * @return Age as a formatted string for display.
	 */
	public String getFormattedAge() {
		return OutputFormatter.formatAge(System.currentTimeMillis() - _date);
	}
	
	/**
	 * Merges the photo with another photo.
	 * 
	 * @param other Photo to merge with.
	 */
	public void mergeWith(final Photo other) {
		
		// merge data
		lat = (lat + other.lat) / 2;
		lng = (lng + other.lng) / 2;
		_alt = (_alt + other._alt) / 2;
		_date = (_date + other._date) / 2;
		
		// TODO merge images
		// possible strategy:
		// is portrait + other is portrait -> clip next to each other as landscape
		// is portrait + other is landscape -> do nothing & save other img for future merge
		// is landscape + other is landscape -> clip together as portrait
		// is landscape + other is portrait -> do nothing & save other img for future merge
		// if there is a saved img, these combinations can be made:
		// portrait + portrait + landscape -> portraits next to each other / landscape on bottom -> portrait
		// portrait + landscape + landscape -> landscapes upon each other / portrait next to it -> landscape
	}
}
