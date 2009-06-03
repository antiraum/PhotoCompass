package de.fraunhofer.fit.photocompass.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.data.Photo;

/**
 * This model stores the informations about the photos used by the application.
 * It provides methods to access the photos and to determine which photos are visible with the current settings.
 * This is a Singleton.
 */
public final class Photos {

    private static Photos _instance;
	private HashMap<Integer, Photo> _photos = new HashMap<Integer, Photo>(); // map of all used photos
																			 // (key is resource id, value is {@link Photo} object)
	private boolean _initialized = false;
	
	/**
	 * Constructor.
	 * Reads the photos stored on the device and populates {@link #_photos} with the ones that can be used by the application.
	 */
	protected Photos() {
        
	    if (! PhotoCompassApplication.USE_DUMMY_PHOTOS) return;
	    
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
	
	public void initialize(final Activity activity) {
		
		if (_initialized) return;
		
		ArrayList<Integer> photoIds = new ArrayList<Integer>();

	    Uri uris[] = {MediaStore.Images.Thumbnails.INTERNAL_CONTENT_URI, MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI};
	    String columns[] = { 
	        MediaStore.Images.ImageColumns._ID, 
	        MediaStore.Images.ImageColumns.DATA, 
	        MediaStore.Images.ImageColumns.LATITUDE, 
	        MediaStore.Images.ImageColumns.LONGITUDE,
	        MediaStore.Images.ImageColumns.DATE_TAKEN
	    };
	    Cursor cursor;
	    int idCol, fileCol, latCol, lngCol, altCol, dateCol;
		for (Uri uri : uris) {
            Log.d(PhotoCompassApplication.LOG_TAG, "Photos: initialize: uri = "+uri.toString());
            
            // get cursor
		    cursor = activity.managedQuery(uri, null, null, null, null);
		    if (cursor == null) continue;
		    
		    // get row count
	        int numrows = cursor.getCount();
            Log.d(PhotoCompassApplication.LOG_TAG, "Photos: initialize: numrows = "+numrows);
	        if (numrows == 0) continue;

		    // get column indexes
	    	idCol = cursor.getColumnIndex(Images.ImageColumns._ID);
	    	fileCol = cursor.getColumnIndex(Images.ImageColumns.DATA); 
	    	latCol = cursor.getColumnIndex(Images.ImageColumns.LATITUDE); 
	    	lngCol = cursor.getColumnIndex(Images.ImageColumns.LONGITUDE); 
//		    altCol = cursor.getColumnIndex(Images.ImageColumns.ALTITUDE); // FIXME: no such column!!!
	    	dateCol = cursor.getColumnIndex(Images.ImageColumns.DATE_TAKEN);
	    	Log.d(PhotoCompassApplication.LOG_TAG, "Photos: idCol = "+idCol+", fileCol = "+fileCol+", latCol = "+latCol+", lngCol = "+lngCol+", dateCol = "+dateCol);
	    	
	    	// get photo data
//	        cursor.moveToFirst();
//	        for (int i = 0; i < numrows; i++) {
//	        	int id = cursor.getInt(idCol);
//                String file = cursor.getString(fileCol); 
//                double lat = cursor.getDouble(latCol); 
//                double lng = cursor.getDouble(lngCol); 
//                int date = cursor.getInt(idCol);
//                Log.d(PhotoCompassApplication.LOG_TAG, "Photos: id = "+id+", file = "+file+", lat = "+lat+", lng = "+lng+", date = "+date);
////	        	_photos.put(id, new Photo());
//	            cursor.moveToNext();
//	        }
		}
		
//		ImageManager.instance()
		
		for (int photoId : photoIds) {
            Log.d(PhotoCompassApplication.LOG_TAG, "Photos: initialize: "+photoId);
		}

//        int dataColIdx;
//        String imageData;
//
//        try {
//                //trying to fetch the photo's associated thumbnail Uri
//                dataColIdx = thumbnail.getColumnIndex(android.provider.MediaStore.Images.Thumbnails.DATA);
//                imageData = thumbnail.getString(dataColIdx); //Throws CursorIndexOutOfBoundsExceptions
//                android.util.Log.d("lens","photoID="+photoID+" imageData="+imageData);
//        } catch (CursorIndexOutOfBoundsException e) {
//                try {
//                        //when that fails, manually generate the thumbnail, and try again
//                        MediaStore.Images.Media.insertImage(
//                                        mContext.getContentResolver(),
//                                        photo.getColumnName(photo.getColumnIndex(MediaStore.Images.Media.DATA)),
//                                        "", "");
//                        thumbnail.requery();
//                        thumbnail.moveToFirst();
//
//                        imageData = thumbnail.getString(thumbnail.getColumnIndex(MediaStore.Images.Media.DATA));
//                } catch (FileNotFoundException f) {
//                        //at the very least, just fetch the actual photo
//                        //this is bad because it's processor- and memory-intensive, but
//                        //chances of getting this far are rare
//                        //TODO: turn this into a proxy process, and generate the images in the background
//                        dataColIdx = photo.getColumnIndex(android.provider.MediaStore.Images.Media.DATA);
//                        imageData = photo.getString(dataColIdx);
//                }
//        }
//
//        final Uri dataURI = Uri.parse(imageData);
//
//        ImageView imageView = new ImageView(mContext);
//        //TODO: formatting stuff, need to shove this to XML
//        imageView.setLayoutParams(new GridView.LayoutParams(90, 90));
//        imageView.setAdjustViewBounds(false);
//        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        imageView.setId(position);
//        imageView.setOnClickListener(new View.OnClickListener() {
//                private Uri imageURI = dataURI;
//
//                public void onClick(View view) {
//                        Intent mIntent = new Intent(mContext, PhotoView.class);
//
//                        // need to pass in position of the photo, as well as point to it
//                        mIntent.setData(imageURI);
//                        mIntent.putExtra("position", position);
//                        mIntent.putExtra(LensBlasterDB.PA_KEY_PHOTOID, String.valueOf(photoID));
//                        mIntent.putExtra(LensBlasterDB.ALBUM_KEY_ROWID, mAlbumID);
//
//                        startActivityForResult(mIntent, ACTIVITY_PHOTOVIEW);
//                }
//        });
//		private void setData(ImageView imageView, Cursor cursor) {
//            long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
//
//            imageView.setTag(id);
//            imageView.setImageURI(ImageTools.getBestFitImageUri(mContext.getContentResolver(), id))
	    
        _initialized = true;
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
    public Photo getPhoto(final int resourceId) {
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
    public ArrayList<Integer> getNewlyVisiblePhotos(final ArrayList<Integer> currentPhotos,
    												final float maxDistance, final int minAge, final int maxAge) {
    	
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
    public ArrayList<Integer> getNoLongerVisiblePhotos(final ArrayList<Integer> currentPhotos,
    												   final float maxDistance, final int minAge, final int maxAge) {
    	
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
    private boolean _isPhotoVisible(final Photo photo, final float maxDistance, final int minAge, final int maxAge) {
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
    public void updatePhotoProperties(final double lat, final double lng, final double alt) {
    	for (Photo photo : _photos.values()) photo.updateDistanceDirectionAndAltitudeOffset(lat, lng, alt);
    }
}
