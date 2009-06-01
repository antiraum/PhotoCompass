package de.fraunhofer.fit.photocompass.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.location.Location;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.data.Photo;

/**
 * This model stores the informations about the photos used by the application.
 * It provides methods to access the photos and to determine which photos are visible with the current settings.
 * This is a Singleton.
 */
public class Photos {

    private static Photos _instance;
	private static HashMap<Integer, Photo> _photos; // map of all used photos (key is resource id, value is {@link Photo} object)
	
	/**
	 * Constructor.
	 * Reads the photos stored on the device and populates {@link #_photos} with the ones that can be used by the application.
	 */
	protected Photos() {
		
	    _photos = new HashMap<Integer, Photo>();
	    // TODO: get camera photos (look into MediaStore.Images.Thumbnails and MediaStore.Images.Media)
	    
	    // dummy Photos (stuff near B-IT)
	    _photos.put(R.drawable.photo_0518, new Photo(R.drawable.photo_0518, Location.convert("50:43:11.4"), Location.convert("7:7:18"), 103));
	    _photos.put(R.drawable.photo_0519, new Photo(R.drawable.photo_0519, Location.convert("50:43:10.8"), Location.convert("7:7:18.6"), 105));
	    _photos.put(R.drawable.photo_0520, new Photo(R.drawable.photo_0520, Location.convert("50:43:12"), Location.convert("7:7:19.2"), 107));
	    _photos.put(R.drawable.photo_0521, new Photo(R.drawable.photo_0521, Location.convert("50:43:10.8"), Location.convert("7:7:20.4"), 102));
	    _photos.put(R.drawable.photo_0522, new Photo(R.drawable.photo_0522, Location.convert("50:43:10.8"), Location.convert("7:7:21"), 103));
	    _photos.put(R.drawable.photo_0523, new Photo(R.drawable.photo_0523, Location.convert("50:43:10.8"), Location.convert("7:7:21.6"), 104));
	    _photos.put(R.drawable.photo_0524, new Photo(R.drawable.photo_0524, Location.convert("50:43:10.21"), Location.convert("7:7:22.8"), 101));
	    _photos.put(R.drawable.photo_0525, new Photo(R.drawable.photo_0525, Location.convert("50:43:10.21"), Location.convert("7:7:22.8"), 105));
	    
	    // dummy Photos (stuff near FIT)
	    _photos.put(R.drawable.fit_11067049, new Photo(R.drawable.fit_11067049, Location.convert("50:45:8.10"), Location.convert("7:12:28.59"), 105));
	    _photos.put(R.drawable.fit_4138394, new Photo(R.drawable.fit_4138394, Location.convert("50:45:20.71"), Location.convert("7:11:53.83"), 145));
	    _photos.put(R.drawable.fit_11092935, new Photo(R.drawable.fit_11092935, Location.convert("50:45:23.27"), Location.convert("7:12:16.96"), 160));
	    _photos.put(R.drawable.fit_12610213, new Photo(R.drawable.fit_12610213, Location.convert("50:45:19.29"), Location.convert("7:12:52.97"), 100));
	    _photos.put(R.drawable.fit_14308427, new Photo(R.drawable.fit_14308427, Location.convert("50:44:56.21"), Location.convert("7:13:16.02"), 120));
	    _photos.put(R.drawable.fit_8503628, new Photo(R.drawable.fit_8503628, Location.convert("50:44:29.47"), Location.convert("7:10:53.81"), 125));
	    _photos.put(R.drawable.fit_3038737, new Photo(R.drawable.fit_3038737, Location.convert("50:43:49.21"), Location.convert("7:13:11.66"), 123));
	    _photos.put(R.drawable.fit_4410168, new Photo(R.drawable.fit_4410168, Location.convert("50:45:22.80"), Location.convert("7:13:56.62"), 122));
		_photos.put(R.drawable.fit_12610204, new Photo(R.drawable.fit_12610204, Location.convert("50:45:17.73"), Location.convert("7:12:51.92"), 126));
		_photos.put(R.drawable.fit_14308344, new Photo(R.drawable.fit_14308344, Location.convert("50:44:57.02"), Location.convert("7:13:24.18"), 127));
		_photos.put(R.drawable.fit_1798151678_af72c8f78d, new Photo(R.drawable.fit_1798151678_af72c8f78d, Location.convert("50:44:58"), Location.convert("7:12:21"), 126));
		_photos.put(R.drawable.fit_2580082727_1faf043ec1, new Photo(R.drawable.fit_2580082727_1faf043ec1, Location.convert("50:44:5"), Location.convert("7:12:19"), 125));
		_photos.put(R.drawable.fit_2417313476_d588a4e2b5, new Photo(R.drawable.fit_2417313476_d588a4e2b5, Location.convert("50:44:56"), Location.convert("7:12:23"), 124));
	}

	/**
	 * @return The instance of this Singleton model.
	 */
    public static Photos getInstance() {
        if (_instance == null) _instance = new Photos();
        return _instance;
    }
    
    /**
     * Checks if {@link #_photos} is up-to-date with the photos stores on the device.
     * Should be called by activities after they become active again, as the user could have added or removed photos in the meantime.
     */
    public void updatePhotos() {
    	// TODO check if photos on the device have changed, if yes refresh _photos
    }
    
    /**
     * Get a {@link Photo} object for a resource id.
     * @param resourceId Resource id of the requested photo.
     * @return			 <code>{@link Photo}</code> if the photo is known, or
     * 					 <code>null</code> if the photo is not known.
     */
    public Photo getPhoto(int resourceId) {
    	return _photos.containsKey(resourceId) ? _photos.get(resourceId) : null;
    }

    /**
     * Determines which photos are newly visible for the current viewing settings.
     * @param currentPhotos ArrayList with resource ids of the currently displayed photos.
     * @param maxDistance   Maximum distance from the current position (in meters).
     * @param minAge 	    Minimum age of the photos (in ...).
     * @param maxAge 	    Maximum age of the photos (in ...).
     * @return				ArrayList with resource ids of the newly visible photos.
     */
    public ArrayList<Integer> getNewlyVisiblePhotos(ArrayList<Integer> currentPhotos,
    												float maxDistance, int minAge, int maxAge) {
    	
    	ArrayList<Integer> results = new ArrayList<Integer>();
    	
    	for (Map.Entry<Integer, Photo> photoEntry : _photos.entrySet()) {
    		if (_isPhotoVisible(photoEntry.getValue(), maxDistance, minAge, maxAge)) results.add(photoEntry.getKey());
    	}
    	
    	return results;
    }

    /**
     * Determines which photos are no longer visible for the current viewing settings.
     * @param currentPhotos ArrayList with resource ids of the currently displayed photos.
     * @param maxDistance   Maximum distance from the current position (in meters).
     * @param minAge 	    Minimum age of the photos (in ...).
     * @param maxAge 	    Maximum age of the photos (in ...).
     * @return				ArrayList with resource ids of the no longer visible photos.
     */
    public ArrayList<Integer> getNoLongerVisiblePhotos(ArrayList<Integer> currentPhotos,
    												   float maxDistance, int minAge, int maxAge) {
    	
    	ArrayList<Integer> results = new ArrayList<Integer>();
    	
    	for (int resourceId : currentPhotos) {
    		if (! _isPhotoVisible(_photos.get(resourceId), maxDistance, minAge, maxAge)) results.add(resourceId);
    	}
    	
    	return results;
    }
    
    /**
     * Checks if a photo is visible with the current settings.
     * @param photo		  Photo to check.
     * @param maxDistance Maximum distance from the current position (in meters).
     * @param minAge 	  Minimum age of the photos (in ...).
     * @param maxAge 	  Maximum age of the photos (in ...).
     * @return			  <code>true</code> if photo is visible, or
     * 					  <code>false</code> if photo is not visible.
     */
    private boolean _isPhotoVisible(Photo photo, float maxDistance, int minAge, int maxAge) {
		if (photo.getDistance() > maxDistance) { // photo is too far away
			return false;
		}
		int photoAge = photo.getAge();
		if (photoAge < minAge || photoAge > maxAge) { // photo is too young or too old
			return false;
		}
		return true;
    }
    
    /**
     * Updates distance, direction, and altitude offset of all photos stored in the model.
     * @param lat Current latitude.
     * @param lng Current longitude.
     * @param alt Current altitude.
     */
    public void updatePhotoProperties(double lat, double lng, double alt) {
    	for (Photo photo : _photos.values()) photo.updateDistanceDirectionAndAltitudeOffset(lat, lng, alt);
    }
}
