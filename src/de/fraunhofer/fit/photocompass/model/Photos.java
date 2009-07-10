package de.fraunhofer.fit.photocompass.model;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.DeadObjectException;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.util.SparseArray;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.data.Photo;

/**
 * This model stores the informations about the photos used by the application.
 * It provides methods to access the photos and to determine which photos are visible with the current settings.
 * This is a Singleton.
 */
public final class Photos {

    private static final Photos _instance = new Photos();
	private boolean _initialized = false;
    
    /**
     * {@link SparseArray} of all photos usable by the application.
     * Key is photo id, value is {@link Photo} object.
     */
	private SparseArray<Photo> _photos = new SparseArray<Photo>();
	
	/**
	 * {@link SparseArray} of all dummy photos usable by the application.
	 * Key is photo id, value is {@link Photo} object.
	 */
	private final SparseArray<Photo> _dummies = new SparseArray<Photo>();
	
    private final RemoteCallbackList<IPhotosCallback> _remoteCallbacks = new RemoteCallbackList<IPhotosCallback>();

    /**
     * Constructor.
     * Private because Singleton. Use {@link #getInstance()}.
     */
    private Photos() {}
    
	/**
	 * @return The instance of this Singleton model.
	 */
    public static Photos getInstance() {
        return _instance;
    }
	
	/**
	 * Initialization.
	 * Populates {@link #_photos} and {@link #_dummies} (if {@link #PhotoCompassApplication.USE_DUMMY_PHOTOS} is <code>true</code>).
	 * This needs to run only once.
	 * 
	 * @param activity Calling {@link Activity}.
	 */
	public void initialize(final Activity activity) {
		
		if (_initialized) return; // only run once

	    if (PhotoCompassApplication.USE_DUMMY_PHOTOS) _populateDummies(); // populate _dummies
	    
		updatePhotos(activity); // populate _photos
	    
        _initialized = true;
	}
    
    /**
     * Register a callback object.
     */
    public void registerCallback(final IPhotosCallback cb) {
        if (cb != null) _remoteCallbacks.register(cb);
    }
    
    /**
     * Unregister a callback object.
     */
    public void unregisterCallback(final IPhotosCallback cb) {
        if (cb != null) _remoteCallbacks.unregister(cb);
    }
    
    /**
     * Checks if {@link #_photos} is up-to-date with the photos stores on the device.
     * Should be called by activities after they become active again, as the user could have added or removed photos in the meantime.
     * Populates {@link #_photos} with the images on the device that can be used in the application.
     * 
	 * @param activity Calling {@link Activity}. Needed for calling {@link Activity#managedQuery(Uri, String[], String, String[], String)}
	 * 				   on {@link MediaStore.Images}.
     */
    public void updatePhotos(final Activity activity) {
    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: updatePhotos");
    	
    	final SparseArray<Photo> _photosNew = new SparseArray<Photo>();
    	
		// FIXME MediaStore.Images has no column for altitude
		// We need to get the altitude ourselves from the JPG.
		final Uri mediaUris[] = {Media.INTERNAL_CONTENT_URI, Media.EXTERNAL_CONTENT_URI};
	    final Uri thumbUris[] = {Thumbnails.INTERNAL_CONTENT_URI, Thumbnails.EXTERNAL_CONTENT_URI};
	    final String mediaColumns[] = { 
    		BaseColumns._ID,
	        ImageColumns.LATITUDE, 
	        ImageColumns.LONGITUDE,
	        ImageColumns.DATE_TAKEN
	    };
	    final String thumbColumns[] = {
    		Thumbnails.DATA
	    };
	    Cursor mediaCursor, thumbCursor;
	    int idCol, latCol, lngCol, dateCol, thumbCol;
	    int id; double lat, lng; String date, thumb;
	    URIS: for (int i = 0; i < mediaUris.length; i++) {
//            Log.d(PhotoCompassApplication.LOG_TAG, "Photos: updatePhotos: uri = "+mediaUris[i].toString());
            
            // get cursor
            mediaCursor = activity.managedQuery(mediaUris[i], mediaColumns, null, null, null);
		    if (mediaCursor == null) continue;
		    
		    // get row count
	        final int numrows = mediaCursor.getCount();
//            Log.d(PhotoCompassApplication.LOG_TAG, "Photos: updatePhotos: numrows = "+numrows);
	        if (numrows == 0) continue;

		    // get column indexes
	    	idCol = mediaCursor.getColumnIndex(BaseColumns._ID);
	    	latCol = mediaCursor.getColumnIndex(ImageColumns.LATITUDE); 
	    	lngCol = mediaCursor.getColumnIndex(ImageColumns.LONGITUDE); 
	    	dateCol = mediaCursor.getColumnIndex(ImageColumns.DATE_TAKEN);
//            Log.d(PhotoCompassApplication.LOG_TAG, "Photos: idCol = "+idCol+", latCol = "+latCol+", lngCol = "+lngCol+", dateCol = "+dateCol);

	    	// get data
		    mediaCursor.moveToFirst();
		    Photo photo;
	        for (int j = 0; j < numrows; j++) {
	        	
	        	id = mediaCursor.getInt(idCol);
	        	
	        	photo = _photos.get(id);
	        	if (photo != null) {
	        		// photo is known, we can copy it from the existing _photos
	        		_photosNew.append(id, photo);
                	mediaCursor.moveToNext();
                	continue;
	        	}
	        	
                lat = mediaCursor.getDouble(latCol); 
                lng = mediaCursor.getDouble(lngCol); 
                date = mediaCursor.getString(dateCol);
                
                if (lat == 0 || lng == 0 || date == null) { // not enough data
                	mediaCursor.moveToNext();
                	continue;
                }
    	        
                thumbCursor = activity.managedQuery(thumbUris[i], thumbColumns, Thumbnails.IMAGE_ID+"='"+id+"'", null, null);
    		    if (thumbCursor == null || thumbCursor.getCount() == 0) continue URIS;

    	    	thumbCol = thumbCursor.getColumnIndex(Thumbnails.DATA);
//                Log.d(PhotoCompassApplication.LOG_TAG, "Photos: thumbCol = "+thumbCol);
    		    thumbCursor.moveToFirst();
                thumb = thumbCursor.getString(thumbCol); 
                
                if (thumb == null) { // not enough data
                	mediaCursor.moveToNext();
                	continue;
                }
                
                // TODO generate thumbnails that don't exist
                
//                Log.d(PhotoCompassApplication.LOG_TAG, "Photos: id = "+id+", lat = "+lat+", lng = "+lng+", date = "+date+", thumb = "+thumb);
                
                _photosNew.append(id, new Photo(id, Uri.parse(Uri.encode(thumb)), lat, lng, 0, Long.parseLong(date)));
                
                mediaCursor.moveToNext();
	        }
		}
	    
	    // replace the existing _photos
	    _photos = _photosNew;
	    
	    // broadcast the photo distances and ages
	    _broadcastPhotoDistances();
	    _broadcastPhotoAges();
    }
    
    /**
     * Get a {@link Photo} object for a photo/resource id.
     * 
     * @param id Id of the requested photo (photo id for MediaStore photos; resource id for dummy photos).
     * @return <code>{@link Photo}</code> if the photo is known, or
     * 		   <code>null</code> if the photo is not known.
     */
    public Photo getPhoto(final int id) {
    	Photo photo = _photos.get(id);
    	if (photo == null) photo = _dummies.get(id);
    	return photo;
    }

    /**
     * Determines which photos are newly visible for the current viewing settings.
     * 
     * @param currentPhotos   ArrayList with photo/resource ids of the currently displayed photos.
     * @param limitByDistance Consider minimum and maximum distance settings.
     * @param limitByAge	  Consider minimum and maximum age settings.
     * @return				  ArrayList with photo/resource ids of the newly visible photos.
     */
    public ArrayList<Integer> getNewlyVisiblePhotos(final ArrayList<Integer> currentPhotos,
    												final boolean limitByDistance, final boolean limitByAge) {
    	
    	ArrayList<Integer> results = new ArrayList<Integer>();
    	
    	int numPhotos;
    	for (SparseArray<Photo> photos : new SparseArray[] {_photos, _dummies}) {
    		numPhotos = photos.size();
            for (int i = 0; i < numPhotos; i++) {
	    		if (_isPhotoVisible(photos.valueAt(i), limitByDistance, limitByAge) &&
	    			! currentPhotos.contains(photos.keyAt(i))) results.add(photos.keyAt(i));
	    	}
    	}
    	
    	return results;
    }

    /**
     * Determines which photos are no longer visible for the current viewing settings.
     * 
     * @param currentPhotos   ArrayList with photo/resource ids of the currently displayed photos.
     * @param limitByDistance Consider minimum and maximum distance settings.
     * @param limitByAge	  Consider minimum and maximum age settings.
     * @return				  ArrayList with photo/resource ids of the no longer visible photos.
     */
    public ArrayList<Integer> getNoLongerVisiblePhotos(final ArrayList<Integer> currentPhotos,
													   final boolean limitByDistance, final boolean limitByAge) {
    	
    	ArrayList<Integer> results = new ArrayList<Integer>();
    	
    	Photo photo;
    	for (int id : currentPhotos) {
    		photo = getPhoto(id);
    		if (photo == null || ! _isPhotoVisible(photo, limitByDistance, limitByAge)) results.add(id);
    	}
    	
    	return results;
    }
    
    /**
     * Checks if a photo is visible with the current settings.
     * 
     * @param photo		      Photo to check.
     * @param limitByDistance Consider minimum and maximum distance settings.
     * @param limitByAge	  Consider minimum and maximum age settings.
     * @return			      <code>true</code> if photo is visible, or
     * 					      <code>false</code> if photo is not visible.
     */
    private boolean _isPhotoVisible(final Photo photo,
			   						final boolean limitByDistance, final boolean limitByAge) {
	    final ApplicationModel appModel = ApplicationModel.getInstance();
//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: _isPhotoVisible: id = "+photo.getId());
		if (limitByDistance &&
			photo.distance < appModel.minDistance || photo.distance > appModel.maxDistance) { // photo is too close or too far away
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: _isPhotoVisible: photo is too close or too far away: photo.distance = "+photo.distance);
			return false;
		}
		final long photoAge = photo.getAge();
		if (limitByAge &&
			photoAge < appModel.minAge || photoAge > appModel.maxAge) { // photo is too young or too old
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: _isPhotoVisible: photo is too young or too old");
//	    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: _isPhotoVisible: photoAge = "+photoAge+", minAge = "+minAge+", maxAge = "+maxAge);
			return false;
		}
		return true;
    }
    
    /**
     * Updates distance, direction, and altitude offset of all photos stored in the model.
     * 
     * @param lat Current latitude.
     * @param lng Current longitude.
     * @param alt Current altitude.
     */
    public void updatePhotoProperties(final double lat, final double lng, final double alt) {
    	int numPhotos;
    	for (SparseArray<Photo> photos : new SparseArray[] {_photos, _dummies}) {
    		numPhotos = photos.size();
            for (int i = 0; i < numPhotos; i++) photos.valueAt(i).updateDistanceDirectionAndAltitudeOffset(lat, lng, alt);
    	}
    	updateAppModelMaxValues();
    }
    
    /**
     * Updates the maximum limits of the {@link ApplicationModel} for distance and age according to
     * the data of the used photos.
     */
    public void updateAppModelMaxValues() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: updateAppModelMaxValues");
	    final ApplicationModel appModel = ApplicationModel.getInstance();
	    float maxDistance = 0;
	    long maxAge = 0;
    	int numPhotos;
	    Photo photo;
	    float dist;
	    long age;
    	for (SparseArray<Photo> photos : new SparseArray[] {_photos, _dummies}) {
    		numPhotos = photos.size();
            for (int i = 0; i < numPhotos; i++) {
            	photo = photos.valueAt(i);
            	dist = photo.distance;
            	if (dist == 0) continue; // photo properties not set
            	if (dist > maxDistance && dist <= appModel.MAX_DISTANCE_LIMIT) maxDistance = dist;
            	age = photo.getAge();
            	if (age > maxAge && age <= appModel.MAX_AGE_LIMIT) maxAge = age;
            }
    	}
    	if (appModel.setMaxMaxDistance(maxDistance)) _broadcastPhotoDistances();
    	maxAge += 60 * 60 * 1000; // as the photo age is always calculated from the current time we add a buffer of one hour
    	                          // which should be enough for any usage time of the application
    	if (appModel.setMaxMaxAge(maxAge)) _broadcastPhotoAges();
    }
    
    /**
     * Broadcasts a list with the distances of all photos used by the application.
     * The distances are translated into relative values.
     */
    private void _broadcastPhotoDistances() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: _broadcastPhotoDistances");
    	
    	// check if there are callbacks registered
	    final int numCallbacks = _remoteCallbacks.beginBroadcast();
    	if (numCallbacks == 0) {
    		_remoteCallbacks.finishBroadcast();
    		return;
    	}
    	
    	// collect distances
    	final ArrayList<Float> dists = new ArrayList<Float>();
    	int numPhotos;
    	Photo photo;
    	final ApplicationModel appModel = ApplicationModel.getInstance();
    	for (SparseArray<Photo> photos : new SparseArray[] {_photos, _dummies}) {
    		numPhotos = photos.size();
            for (int i = 0; i < numPhotos; i++) {
            	photo = photos.valueAt(i);
            	if (! _isPhotoUsed(photo)) continue;
            	dists.add(appModel.absoluteToRelativeDistance(photo.distance));
            }
    	}
//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: _broadcastPhotoDistances: dists = "+dists.toString());
    	
		// broadcast
	    for (int i = 0; i < numCallbacks; i++) {
	        try {
	            _remoteCallbacks.getBroadcastItem(i).onPhotosDistancesChange(_listToPrimitives(dists));
	        } catch (final DeadObjectException e) {
	            // the RemoteCallbackList will take care of removing the dead object
	        } catch (final RemoteException e) {
		    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: broadcast to callback failed");
	        }
	    }
	    _remoteCallbacks.finishBroadcast();
    }
    
    /**
     * Broadcasts a list with the age of all photos used by the application to the registered callbacks.
     * The ages are translated into relative values.
     */
    private void _broadcastPhotoAges() {
    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: _broadcastPhotoAges");
    	
    	// check if there are callbacks registered
	    final int numCallbacks = _remoteCallbacks.beginBroadcast();
    	if (numCallbacks == 0) {
    		_remoteCallbacks.finishBroadcast();
    		return;
    	}
    	
    	// collect ages
    	final ArrayList<Float> ages = new ArrayList<Float>();
    	int numPhotos;
    	Photo photo;
    	final ApplicationModel appModel = ApplicationModel.getInstance();
    	for (SparseArray<Photo> photos : new SparseArray[] {_photos, _dummies}) {
    		numPhotos = photos.size();
            for (int i = 0; i < numPhotos; i++) {
            	photo = photos.valueAt(i);
            	if (! _isPhotoUsed(photo)) continue;
            	ages.add(appModel.absoluteToRelativeAge(photo.getAge()));
            }
    	}
//    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: _broadcastPhotoAges: ages = "+ages.toString());
		
		// broadcast
	    for (int i = 0; i < numCallbacks; i++) {
	        try {
	            _remoteCallbacks.getBroadcastItem(i).onPhotosAgesChange(_listToPrimitives(ages));
	        } catch (final DeadObjectException e) {
	            // the RemoteCallbackList will take care of removing the dead object
	        } catch (final RemoteException e) {
		    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: broadcast to callback failed");
	        }
	    }
	    _remoteCallbacks.finishBroadcast();
    }
    
    private boolean _isPhotoUsed(final Photo photo) {
	    final ApplicationModel appModel = ApplicationModel.getInstance();
		if (photo.distance > appModel.MAX_MAX_DISTANCE ||
			photo.getAge() > appModel.MAX_MAX_AGE) return false;
		return true;
    }
    
    private static float[] _listToPrimitives(ArrayList<Float> list) {
    	final int size = list.size();
    	final float[] array = new float[size];
    	for (int i = 0; i < size; i++) array[i] = list.get(i);
    	return array;
    }
	
	/**
	 * Populates {@link #_dummies}.
	 */
    private void _populateDummies() {
    	
	    final long dateTime = System.currentTimeMillis();
	    
	    // dummy photos (stuff near B-IT)
	    _dummies.append(R.drawable.photo_0518, new Photo(R.drawable.photo_0518, Location.convert("50:43:11.4"), Location.convert("7:7:18"), 103, dateTime));
//	    _dummies.append(R.drawable.photo_0519, new Photo(R.drawable.photo_0519, Location.convert("50:43:10.8"), Location.convert("7:7:18.6"), 105, dateTime));
	    _dummies.append(R.drawable.photo_0520, new Photo(R.drawable.photo_0520, Location.convert("50:43:12"), Location.convert("7:7:19.2"), 107, dateTime));
//	    _dummies.append(R.drawable.photo_0521, new Photo(R.drawable.photo_0521, Location.convert("50:43:10.8"), Location.convert("7:7:20.4"), 102, dateTime));
	    _dummies.append(R.drawable.photo_0522, new Photo(R.drawable.photo_0522, Location.convert("50:43:10.8"), Location.convert("7:7:21"), 103, dateTime));
//	    _dummies.append(R.drawable.photo_0523, new Photo(R.drawable.photo_0523, Location.convert("50:43:10.8"), Location.convert("7:7:21.6"), 104, dateTime));
	    _dummies.append(R.drawable.photo_0524, new Photo(R.drawable.photo_0524, Location.convert("50:43:10.21"), Location.convert("7:7:22.8"), 101, dateTime));
//	    _dummies.append(R.drawable.photo_0525, new Photo(R.drawable.photo_0525, Location.convert("50:43:10.21"), Location.convert("7:7:22.8"), 105, dateTime));
	    
	    // dummy photos (stuff near FIT)
	    _dummies.append(R.drawable.fit_11067049, new Photo(R.drawable.fit_11067049, Location.convert("50:45:8.10"), Location.convert("7:12:28.59"), 105, dateTime));
//	    _dummies.append(R.drawable.fit_4138394, new Photo(R.drawable.fit_4138394, Location.convert("50:45:20.71"), Location.convert("7:11:53.83"), 145, dateTime));
	    _dummies.append(R.drawable.fit_11092935, new Photo(R.drawable.fit_11092935, Location.convert("50:45:23.27"), Location.convert("7:12:16.96"), 160, dateTime));
//	    _dummies.append(R.drawable.fit_12610213, new Photo(R.drawable.fit_12610213, Location.convert("50:45:19.29"), Location.convert("7:12:52.97"), 100, dateTime));
	    _dummies.append(R.drawable.fit_14308427, new Photo(R.drawable.fit_14308427, Location.convert("50:44:56.21"), Location.convert("7:13:16.02"), 120, dateTime));
//	    _dummies.append(R.drawable.fit_8503628, new Photo(R.drawable.fit_8503628, Location.convert("50:44:29.47"), Location.convert("7:10:53.81"), 125, dateTime));
	    _dummies.append(R.drawable.fit_3038737, new Photo(R.drawable.fit_3038737, Location.convert("50:43:49.21"), Location.convert("7:13:11.66"), 123, dateTime));
//	    _dummies.append(R.drawable.fit_4410168, new Photo(R.drawable.fit_4410168, Location.convert("50:45:22.80"), Location.convert("7:13:56.62"), 122, dateTime));
		_dummies.append(R.drawable.fit_12610204, new Photo(R.drawable.fit_12610204, Location.convert("50:45:17.73"), Location.convert("7:12:51.92"), 126, dateTime));
//		_dummies.append(R.drawable.fit_14308344, new Photo(R.drawable.fit_14308344, Location.convert("50:44:57.02"), Location.convert("7:13:24.18"), 127, dateTime));
		_dummies.append(R.drawable.fit_1798151678_af72c8f78d, new Photo(R.drawable.fit_1798151678_af72c8f78d, Location.convert("50:44:58"), Location.convert("7:12:21"), 126, dateTime));
		_dummies.append(R.drawable.fit_2580082727_1faf043ec1, new Photo(R.drawable.fit_2580082727_1faf043ec1, Location.convert("50:44:5"), Location.convert("7:12:19"), 125, dateTime));
		_dummies.append(R.drawable.fit_2417313476_d588a4e2b5, new Photo(R.drawable.fit_2417313476_d588a4e2b5, Location.convert("50:44:56"), Location.convert("7:12:23"), 124, dateTime));
		
		// dummy photos (stuff near Thomas)
		_dummies.append(R.drawable.tom_0047, new Photo(R.drawable.tom_0047, Location.convert("50:55:38.4"), Location.convert("6:56:31.2"), 102, dateTime));
//		_dummies.append(R.drawable.tom_0049, new Photo(R.drawable.tom_0049, Location.convert("50:55:37.8"), Location.convert("6:56:31.2"), 102, dateTime));
	}
}
