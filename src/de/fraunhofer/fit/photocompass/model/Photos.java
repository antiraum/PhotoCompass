package de.fraunhofer.fit.photocompass.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.location.Location;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.model.data.PhotoMetricsComparator;
import de.fraunhofer.fit.photocompass.views.PhotosView;

public class Photos {

    private static Photos _instance;
	private static List<Photo> _photos;
	
	protected Photos() {
		
	    _photos = new ArrayList<Photo>();
	    // TODO: get camera photos (look into MediaStore.Images.Thumbnails and MediaStore.Images.Media)
	    
	    // dummy Photos (stuff near B-IT)
	    _photos.add(new Photo(R.drawable.photo_0518, Location.convert("50:43:11.4"), Location.convert("7:7:18")));
	    _photos.add(new Photo(R.drawable.photo_0519, Location.convert("50:43:10.8"), Location.convert("7:7:18.6")));
	    _photos.add(new Photo(R.drawable.photo_0520, Location.convert("50:43:12"), Location.convert("7:7:19.2")));
	    _photos.add(new Photo(R.drawable.photo_0521, Location.convert("50:43:10.8"), Location.convert("7:7:20.4")));
	    _photos.add(new Photo(R.drawable.photo_0522, Location.convert("50:43:10.8"), Location.convert("7:7:21")));
	    _photos.add(new Photo(R.drawable.photo_0523, Location.convert("50:43:10.8"), Location.convert("7:7:21.6")));
	    _photos.add(new Photo(R.drawable.photo_0524, Location.convert("50:43:10.21"), Location.convert("7:7:22.8")));
	    _photos.add(new Photo(R.drawable.photo_0525, Location.convert("50:43:10.21"), Location.convert("7:7:22.8")));
	    // more photos -> OutOfMemory exception WTF?
	    // we may need to process the photos before loading
	    
	    // dummy Photos (stuff near FIT)
	    _photos.add(new Photo(R.drawable.fit_11067049, Location.convert("50:45:8.10"), Location.convert("7:12:28.59")));
	    _photos.add(new Photo(R.drawable.fit_4138394, Location.convert("50:45:20.71"), Location.convert("7:11:53.83")));
	    _photos.add(new Photo(R.drawable.fit_11092935, Location.convert("50:45:23.27"), Location.convert("7:12:16.96")));
	    _photos.add(new Photo(R.drawable.fit_12610213, Location.convert("50:45:19.29"), Location.convert("7:12:52.97")));
	    _photos.add(new Photo(R.drawable.fit_14308427, Location.convert("50:44:56.21"), Location.convert("7:13:16.02")));
	    _photos.add(new Photo(R.drawable.fit_8503628, Location.convert("50:44:29.47"), Location.convert("7:10:53.81")));
	    _photos.add(new Photo(R.drawable.fit_3038737, Location.convert("50:43:49.21"), Location.convert("7:13:11.66")));
	    _photos.add(new Photo(R.drawable.fit_4410168, Location.convert("50:45:22.80"), Location.convert("7:13:56.62")));
		_photos.add(new Photo(R.drawable.fit_12610204, Location.convert("50:45:17.73"), Location.convert("7:12:51.92")));
		_photos.add(new Photo(R.drawable.fit_14308344, Location.convert("50:44:57.02"), Location.convert("7:13:24.18")));
		_photos.add(new Photo(R.drawable.fit_1798151678_af72c8f78d, Location.convert("50:44:58"), Location.convert("7:12:21")));
		_photos.add(new Photo(R.drawable.fit_2580082727_1faf043ec1, Location.convert("50:44:5"), Location.convert("7:12:19")));
		_photos.add(new Photo(R.drawable.fit_2417313476_d588a4e2b5, Location.convert("50:44:56"), Location.convert("7:12:23")));
	}

    public static Photos getInstance() {
        if (_instance == null) _instance = new Photos();
        return _instance;
    }
    
    /**
     * Get the photos lying in the current viewing direction.
     * 
     * @param lat Current Latitude (from location service)
     * @param lng Current Longitude (from location service)
     * @param yaw Current Yaw (from orientation service)
     * @param maxDistance Maximum distance from the current position (in meters)
     * @param minAge Minimum age of the photos (in ...)
     * @param maxAge Maximum age of the photos (in ...)
     * @return List of Photos (sorted in order farthest to nearest)
     */
    public List<Photo> getPhotos(double lat, double lng, float yaw, float maxDistance, int minAge, int maxAge) {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: getPhotos");
    	
    	List<Photo> results = new ArrayList<Photo>(); 
    	
    	// update distance and direction for all photos and add the matching ones to the results
    	for (Photo photo : _photos) {
    		
    		photo.updateDistanceAndDirection(lat, lng);
    		    		
    		if (photo.getDistance() > maxDistance) {
    			// photo is too far away
//    	    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: photo is to far away: photo.getDistance() = "+photo.getDistance()+", maxDistance = "+maxDistance);
    			continue;
    		}
    		if (photo.getDirection() < yaw - PhotosView.PHOTO_VIEW_HDEGREES / 2 ||
    			photo.getDirection() > yaw + PhotosView.PHOTO_VIEW_HDEGREES / 2) {
    			// TODO also consider the photos that are partial visible because their direction is little off the horizontal camera angle 
    			// photo is not in viewing direction
//    	    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: photo not in viewing direction: photo.getDirection() = "+photo.getDirection()+", yaw = "+yaw);
    	    	continue;
    		}
    		if (photo.getAge() < minAge || photo.getAge() > maxAge) {
    			// photo is too young or too old
//    	    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: photo is too young or too old: photo.getAge() = "+photo.getAge()+", minAge = "+minAge+", maxAge = "+maxAge);
    			continue;
    		}
    		
    		results.add(photo);
    	}
    	
    	return results;
    }
}
