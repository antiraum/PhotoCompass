package de.fraunhofer.fit.photocompass.services;

import java.util.ArrayList;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.util.SparseArray;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.Settings;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.util.ListArrayConversions;

/**
 * This model stores the informations about the photos used by the application. It provides methods to access the photos
 * and to determine which photos are visible with the current settings. This is a Singleton.
 */
public final class PhotosService extends Service {
    
    /**
     * Radius within photos will be merged into one (in meters).
     */
    private static final int PHOTO_MERGE_RADIUS = 5;
    
    /**
     * {@link SparseArray} of all photos usable by the application. Key is photo id, value is {@link Photo} object.
     * Package scoped for faster access by inner classes.
     */
    SparseArray<Photo> photos = new SparseArray<Photo>();
    
    /**
     * {@link SparseArray} of all dummy photos usable by the application. Key is photo id, value is {@link Photo}
     * object. Package scoped for faster access by inner classes.
     */
    final SparseArray<Photo> dummies = new SparseArray<Photo>();
    
    /**
     * List of callbacks that have been registered with the service. Package scoped for faster access by inner classes.
     */
    final RemoteCallbackList<IPhotosServiceCallback> remoteCallbacks = new RemoteCallbackList<IPhotosServiceCallback>();
    
    /**
     * Implementation of the interface to this service. Is provided to activities when they connect.
     */
    private final IPhotosService.Stub _binder = new IPhotosService.Stub() {
        
        private static final int ALL_VALUES = 0;
        private static final int DISTANCE_VALUES = 1;
        private static final int AGE_VALUES = 2;
        
        private float[] _photoDistances = {};
        private float[] _photoAges = {};
        
        /**
         * Initiates a rescan of the device's photos.
         */
        @Override
        public void initialize(final Settings settings) throws RemoteException {

            // initiate rescan
            readPhotos();
            
            // broadcast the photo distances and ages
            _broadcastPhotoDistances(settings);
            _broadcastPhotoAges(settings);
        }
        
        /**
         * Get a {@link Photo} object for a photo/resource id.
         * 
         * @param id Id of the requested photo (photo id for MediaStore photos; resource id for dummy photos).
         * @return <code>{@link Photo}</code> if the photo is known, or <code>null</code> if the photo is not known.
         */
        @Override
        public Photo getPhoto(final int id) throws RemoteException {

            Photo photo = photos.get(id);
            if (photo == null) {
                photo = dummies.get(id);
            }
            return photo;
        }
        
        /**
         * Determines which photos are newly visible for the current viewing settings.
         * 
         * @param currentPhotos Array with photo/resource ids of the currently displayed photos.
         * @param limitByDistance Consider minimum and maximum distance settings.
         * @param limitByAge Consider minimum and maximum age settings.
         * @return Array with photo/resource ids of the newly visible photos.
         */
        @Override
        public int[] getNewlyVisiblePhotos(final Settings settings, final int[] currentPhotos,
                                           final boolean limitByDistance, final boolean limitByAge)
                throws RemoteException {

            final ArrayList<Integer> currents = ListArrayConversions.intArrayToList(currentPhotos);
            final ArrayList<Integer> results = new ArrayList<Integer>();
            
            int numPhotos;
            for (final SparseArray<Photo> arr : new SparseArray[] {photos, dummies}) {
                numPhotos = arr.size();
                for (int i = 0; i < numPhotos; i++) {
                    if (_isPhotoVisible(settings, arr.valueAt(i), limitByDistance, limitByAge) &&
                        !currents.contains(arr.keyAt(i))) {
                        results.add(arr.keyAt(i));
                    }
                }
            }
            
            return ListArrayConversions.intListToPrimitives(results);
        }
        
        /**
         * Determines which photos are no longer visible for the current viewing settings.
         * 
         * @param currentPhotos Array with photo/resource ids of the currently displayed photos.
         * @param limitByDistance Consider minimum and maximum distance settings.
         * @param limitByAge Consider minimum and maximum age settings.
         * @return Array with photo/resource ids of the no longer visible photos.
         */
        @Override
        public int[] getNoLongerVisiblePhotos(final Settings settings, final int[] currentPhotos,
                                              final boolean limitByDistance, final boolean limitByAge)
                throws RemoteException {

            ListArrayConversions.intArrayToList(currentPhotos);
            final ArrayList<Integer> results = new ArrayList<Integer>();
            
            Photo photo;
            for (final int id : currentPhotos) {
                photo = getPhoto(id);
                if (photo == null || !_isPhotoVisible(settings, photo, limitByDistance, limitByAge)) {
                    results.add(id);
                }
            }
            
            return ListArrayConversions.intListToPrimitives(results);
        }
        
        /**
         * Checks if a photo is visible with the current settings.
         * 
         * @param photo Photo to check.
         * @param limitByDistance Consider minimum and maximum distance settings.
         * @param limitByAge Consider minimum and maximum age settings.
         * @return <code>true</code> if photo is visible, or <code>false</code> if photo is not visible.
         */
        private boolean _isPhotoVisible(final Settings settings, final Photo photo, final boolean limitByDistance,
                                        final boolean limitByAge) {

            if (settings == null) {
                Log.e(PhotoCompassApplication.LOG_TAG, "PhotosService: _isPhotoVisible: settings is null");
                return true;
            }
            
//          Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: _isPhotoVisible: id = "+photo.getId());
            if (limitByDistance && photo.distance < settings.minDistance || photo.distance > settings.maxDistance)
                return false;
            final long photoAge = photo.getAge();
            if (limitByAge && photoAge < settings.minAge || photoAge > settings.maxAge) //              Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: _isPhotoVisible: photoAge = "+photoAge+", minAge = "+appModel.minAge+", maxAge = "+appModel.maxAge);
                return false;
            return true;
        }
        
        /**
         * Updates distance, direction, and altitude offset of all photos stored in the model.
         * 
         * @param lat Current latitude.
         * @param lng Current longitude.
         * @param alt Current altitude.
         */
        @Override
        public Settings updatePhotoProperties(final Settings settings, final double lat, final double lng,
                                              final double alt) throws RemoteException {

            int numPhotos;
            for (final SparseArray<Photo> arr : new SparseArray[] {photos, dummies}) {
                numPhotos = arr.size();
                for (int i = 0; i < numPhotos; i++) {
                    arr.valueAt(i).updateDistanceDirectionAndAltitudeOffset(lat, lng, alt);
                }
            }
            return updateAppModelMaxValues(settings);
        }
        
        /**
         * Updates the maximum limits of the {@link #settings} for distance and age according to the data of the used
         * photos.
         */
        @Override
        public Settings updateAppModelMaxValues(final Settings settings) throws RemoteException {

            if (settings == null) {
                Log.e(PhotoCompassApplication.LOG_TAG, "PhotosService: updateAppModelMaxValues: settings is null");
                return settings;
            }
            
            Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: updateAppModelMaxValues");
            float maxDistance = 0;
            long maxAge = 0;
            int numPhotos;
            Photo photo;
            float dist;
            long age;
            for (final SparseArray<Photo> arr : new SparseArray[] {photos, dummies}) {
                numPhotos = arr.size();
                for (int i = 0; i < numPhotos; i++) {
                    photo = arr.valueAt(i);
                    dist = photo.distance;
                    if (dist == 0) {
                        continue; // photo properties not set
                    }
                    if (dist > maxDistance && dist <= settings.MAX_DISTANCE_LIMIT) {
                        maxDistance = dist;
                    }
                    age = photo.getAge();
                    if (age > maxAge && age <= settings.MAX_AGE_LIMIT) {
                        maxAge = age;
                    }
                }
            }
            if (settings.setMaxMaxDistance(maxDistance)) {
                _broadcastPhotoDistances(settings);
            }
            maxAge += 60 * 60 * 1000; // as the photo age is always calculated from the current time we add a buffer of one hour
            // which should be enough for any usage time of the application
            if (settings.setMaxMaxAge(maxAge)) {
                _broadcastPhotoAges(settings);
            }
            
            return settings;
        }
        
        @Override
        public void registerCallback(final IPhotosServiceCallback cb) throws RemoteException {

            if (cb == null) return;
            remoteCallbacks.register(cb);
            _broadcastValues(cb, ALL_VALUES);
        }
        
        @Override
        public void unregisterCallback(final IPhotosServiceCallback cb) throws RemoteException {

            if (cb == null) return;
            remoteCallbacks.unregister(cb);
        }
        
        private void _broadcastValues(final IPhotosServiceCallback cb, final int values) {

            try {
                if (values == ALL_VALUES || values == DISTANCE_VALUES) {
                    cb.onPhotoDistancesChange(_photoDistances);
                }
                if (values == ALL_VALUES || values == AGE_VALUES) {
                    cb.onPhotoAgesChange(_photoAges);
                }
            } catch (final RemoteException e) {
                Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: broadcast to callback failed");
            }
        }
        
        /**
         * Broadcasts a list with the distances of all photos used by the application. The distances are translated into
         * relative values.
         */
        private void _broadcastPhotoDistances(final Settings settings) {

            if (settings == null) {
                Log.e(PhotoCompassApplication.LOG_TAG, "PhotosService: _broadcastPhotoDistances: settings is null");
                return;
            }
            
            // check if there are callbacks registered
            final int numCallbacks = remoteCallbacks.beginBroadcast();
            if (numCallbacks == 0) {
                remoteCallbacks.finishBroadcast();
                return;
            }
            
            Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: _broadcastPhotoDistances");
            
            // collect distances
            final ArrayList<Float> dists = new ArrayList<Float>();
            int numPhotos;
            Photo photo;
            for (final SparseArray<Photo> arr : new SparseArray[] {photos, dummies}) {
                numPhotos = arr.size();
                for (int i = 0; i < numPhotos; i++) {
                    photo = arr.valueAt(i);
                    if (!_isPhotoUsed(settings, photo)) {
                        continue;
                    }
                    dists.add(settings.absoluteToRelativeDistance(photo.distance));
                }
            }
//          Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: _broadcastPhotoDistances: dists = "+dists.toString());
            _photoDistances = ListArrayConversions.floatListToPrimitives(dists);
            
            // broadcast
            for (int i = 0; i < numCallbacks; i++) {
                _broadcastValues(remoteCallbacks.getBroadcastItem(i), DISTANCE_VALUES);
            }
            remoteCallbacks.finishBroadcast();
        }
        
        /**
         * Broadcasts a list with the age of all photos used by the application to the registered callbacks. The ages
         * are translated into relative values.
         */
        private void _broadcastPhotoAges(final Settings settings) {

            if (settings == null) {
                Log.e(PhotoCompassApplication.LOG_TAG, "PhotosService: _broadcastPhotoAges: settings is null");
                return;
            }
            
            // check if there are callbacks registered
            final int numCallbacks = remoteCallbacks.beginBroadcast();
            if (numCallbacks == 0) {
                remoteCallbacks.finishBroadcast();
                return;
            }
            
            Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: _broadcastPhotoAges");
            
            // collect ages
            final ArrayList<Float> ages = new ArrayList<Float>();
            int numPhotos;
            Photo photo;
            for (final SparseArray<Photo> arr : new SparseArray[] {photos, dummies}) {
                numPhotos = arr.size();
                for (int i = 0; i < numPhotos; i++) {
                    photo = arr.valueAt(i);
                    if (!_isPhotoUsed(settings, photo)) {
                        continue;
                    }
                    ages.add(settings.absoluteToRelativeAge(photo.getAge()));
                }
            }
//          Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: _broadcastPhotoAges: ages = "+ages.toString());
            _photoAges = ListArrayConversions.floatListToPrimitives(ages);
            
            // broadcast
            for (int i = 0; i < numCallbacks; i++) {
                _broadcastValues(remoteCallbacks.getBroadcastItem(i), AGE_VALUES);
            }
            remoteCallbacks.finishBroadcast();
        }
        
        private boolean _isPhotoUsed(final Settings settings, final Photo photo) {

            if (settings == null) {
                Log.e(PhotoCompassApplication.LOG_TAG, "PhotosService: _isPhotoUsed: settings is null");
                return true;
            }
            
            if (photo.distance > settings.MAX_MAX_DISTANCE || photo.getAge() > settings.MAX_MAX_AGE) return false;
            return true;
        }
    };
    
    /**
     * Constructor.
     */
    public PhotosService() {

        if (PhotoCompassApplication.USE_DUMMY_PHOTOS) {
            _populateDummies(); // populate _dummies
        }
        
//        readPhotos(); // populate _photos
    }
    
    /**
     * Called by the system when the service is first created. Populates the photos and dummies (if
     * {@link PhotoCompassApplication#USE_DUMMY_PHOTOS} is <code>true</code>) arrays.
     */
    @Override
    public void onCreate() {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: onCreate");
        
        super.onCreate();
    }
    
    /**
     * Checks if the photos array is up-to-date with the photos stores on the device. Should be called by activities
     * after they become active again, as the user could have added or removed photos in the meantime. Populates the
     * photos array with the images on the device that can be used in the application. Package scoped for faster access
     * by inner classes.
     */
    void readPhotos() {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: readPhotos");
        
        final SparseArray<Photo> _photosNew = new SparseArray<Photo>();
        
        // FIXME MediaStore.Images has no column for altitude
        // We need to get the altitude ourselves from the JPG.
        final Uri mediaUris[] = {Media.INTERNAL_CONTENT_URI, Media.EXTERNAL_CONTENT_URI};
        final Uri thumbUris[] = {Thumbnails.INTERNAL_CONTENT_URI, Thumbnails.EXTERNAL_CONTENT_URI};
        final String mediaColumns[] = {
            BaseColumns._ID, ImageColumns.LATITUDE, ImageColumns.LONGITUDE, ImageColumns.DATE_TAKEN};
        final String thumbColumns[] = {Thumbnails.DATA};
        Cursor mediaCursor, thumbCursor;
        int idCol, latCol, lngCol, dateCol, thumbCol;
        int id;
        double lat, lng;
        String date, thumb;
        URIS: for (int i = 0; i < mediaUris.length; i++) {
//            Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: updatePhotos: uri = "+mediaUris[i].toString());
            
            // get cursor
            mediaCursor = getContentResolver().query(mediaUris[i], mediaColumns, null, null, null);
            if (mediaCursor == null) {
                continue;
            }
            
            // get row count
            final int numrows = mediaCursor.getCount();
//            Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: updatePhotos: numrows = "+numrows);
            if (numrows == 0) {
                continue;
            }
            
            // get column indexes
            idCol = mediaCursor.getColumnIndex(BaseColumns._ID);
            latCol = mediaCursor.getColumnIndex(ImageColumns.LATITUDE);
            lngCol = mediaCursor.getColumnIndex(ImageColumns.LONGITUDE);
            dateCol = mediaCursor.getColumnIndex(ImageColumns.DATE_TAKEN);
//            Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: idCol = "+idCol+", latCol = "+latCol+", lngCol = "+lngCol+", dateCol = "+dateCol);
            
            // get data
            mediaCursor.moveToFirst();
            Photo photo;
            for (int j = 0; j < numrows; j++) {
                
                id = mediaCursor.getInt(idCol);
                
                photo = photos.get(id);
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
                
                thumbCursor = getContentResolver().query(thumbUris[i], thumbColumns,
                                                         Thumbnails.IMAGE_ID + "='" + id + "'", null, null);
                if (thumbCursor == null || thumbCursor.getCount() == 0) {
                    continue URIS;
                }
                
                thumbCol = thumbCursor.getColumnIndex(Thumbnails.DATA);
//                Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: thumbCol = "+thumbCol);
                thumbCursor.moveToFirst();
                thumb = thumbCursor.getString(thumbCol);
                
                if (thumb == null) { // not enough data
                    mediaCursor.moveToNext();
                    continue;
                }
                
                // TODO generate thumbnails that don't exist
                
//                Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: id = "+id+", lat = "+lat+", lng = "+lng+", date = "+date+", thumb = "+thumb);
                
                _photosNew.append(id, new Photo(id, Uri.parse(Uri.encode(thumb)), lat, lng, 0, Long.parseLong(date)));
                
                mediaCursor.moveToNext();
            }
        }
        
        // replace the existing _photos
        photos = _photosNew;
        
        _mergePhotos();
    }
    
    /**
     * Merges photos close to each other.
     */
    private void _mergePhotos() {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: _mergePhotos");
//        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: _mergePhotos: #photos = "+_photos.size()+", #dummies = "+_dummies.size());
        int numPhotos, numOthers, numMerged, photoId, otherId;
        Photo photo, other;
        final SparseArray<Photo> toMergeWith = new SparseArray<Photo>();
        final float[] distanceData = new float[1];
        final ArrayList<Integer> mergedPhotos = new ArrayList<Integer>();
        for (final SparseArray<Photo> arr : new SparseArray[] {photos, dummies}) {
            numPhotos = arr.size();
            for (int i = 0; i < numPhotos; i++) {
                photoId = arr.keyAt(i);
                if (mergedPhotos.contains(photoId)) {
                    continue;
                }
                photo = arr.valueAt(i);
                toMergeWith.clear();
                for (final SparseArray<Photo> others : new SparseArray[] {photos, dummies}) {
                    numOthers = others.size();
                    for (int j = 0; j < numOthers; j++) {
                        otherId = others.keyAt(j);
                        if (otherId == photoId || mergedPhotos.contains(otherId)) {
                            continue;
                        }
                        other = others.valueAt(j);
                        Location.distanceBetween(photo.lat, photo.lng, other.lat, other.lng, distanceData);
                        if (distanceData[0] > PHOTO_MERGE_RADIUS) {
                            continue;
                        }
                        toMergeWith.append(others.keyAt(j), other);
                    }
                }
                if (toMergeWith.size() == 0) {
                    continue;
                }
//              Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: _mergePhotos: "+photoId+" merge with "+toMergeWith.size()+" other(s)");
                numMerged = toMergeWith.size();
                for (int k = 0; k < numMerged; k++) {
                    photo.mergeWith(toMergeWith.valueAt(k));
                    mergedPhotos.add(toMergeWith.keyAt(k));
                }
            }
        }
        for (final int mergedId : mergedPhotos) {
            for (final SparseArray<Photo> arr : new SparseArray[] {photos, dummies}) {
                arr.delete(mergedId);
            }
        }
    }
    
    /**
     * Called when an activity connects to the service.
     * 
     * @return The interface to the service.
     */
    @Override
    public IBinder onBind(final Intent intent) {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: onBind");
        return _binder;
    }
    
    /**
     * Called by the system to notify a Service that it is no longer used and is being removed. Shuts down the service.
     */
    @Override
    public void onDestroy() {

        Log.d(PhotoCompassApplication.LOG_TAG, "PhotosService: onDestroy");
        
        // unregister all callbacks
        remoteCallbacks.kill();
        
        super.onDestroy();
    }
    
    /**
     * Populates {@link #dummies}.
     */
    private void _populateDummies() {

        final long dateTime = System.currentTimeMillis();
        
        // dummy photos (stuff near B-IT)
//      _dummies.append(R.drawable.photo_0518, new Photo(R.drawable.photo_0518, Location.convert("50:43:11.4"), Location.convert("7:7:18"), 103, dateTime));
//      _dummies.append(R.drawable.photo_0519, new Photo(R.drawable.photo_0519, Location.convert("50:43:10.8"), Location.convert("7:7:18.6"), 105, dateTime));
//      _dummies.append(R.drawable.photo_0520, new Photo(R.drawable.photo_0520, Location.convert("50:43:12"), Location.convert("7:7:19.2"), 107, dateTime));
//      _dummies.append(R.drawable.photo_0521, new Photo(R.drawable.photo_0521, Location.convert("50:43:10.8"), Location.convert("7:7:20.4"), 102, dateTime));
//      _dummies.append(R.drawable.photo_0522, new Photo(R.drawable.photo_0522, Location.convert("50:43:10.8"), Location.convert("7:7:21"), 103, dateTime));
//      _dummies.append(R.drawable.photo_0523, new Photo(R.drawable.photo_0523, Location.convert("50:43:10.8"), Location.convert("7:7:21.6"), 104, dateTime));
//      _dummies.append(R.drawable.photo_0524, new Photo(R.drawable.photo_0524, Location.convert("50:43:10.21"), Location.convert("7:7:22.8"), 101, dateTime));
//      _dummies.append(R.drawable.photo_0525, new Photo(R.drawable.photo_0525, Location.convert("50:43:10.21"), Location.convert("7:7:22.8"), 105, dateTime));
        
        // dummy photos (stuff near FIT)
//      _dummies.append(R.drawable.fit_11067049, new Photo(R.drawable.fit_11067049, Location.convert("50:45:8.10"), Location.convert("7:12:28.59"), 105, dateTime));
        dummies.append(R.drawable.fit_4138394, new Photo(R.drawable.fit_4138394, Location.convert("50:45:20.71"),
                                                         Location.convert("7:11:53.83"), 145, dateTime));
//      _dummies.append(R.drawable.fit_11092935, new Photo(R.drawable.fit_11092935, Location.convert("50:45:23.27"), Location.convert("7:12:16.96"), 160, dateTime));
        dummies.append(R.drawable.fit_12610213, new Photo(R.drawable.fit_12610213, Location.convert("50:45:19.29"),
                                                          Location.convert("7:12:52.97"), 100, dateTime));
//      _dummies.append(R.drawable.fit_14308427, new Photo(R.drawable.fit_14308427, Location.convert("50:44:56.21"), Location.convert("7:13:16.02"), 120, dateTime));
        dummies.append(R.drawable.fit_8503628, new Photo(R.drawable.fit_8503628, Location.convert("50:44:29.47"),
                                                         Location.convert("7:10:53.81"), 125, dateTime));
//      _dummies.append(R.drawable.fit_3038737, new Photo(R.drawable.fit_3038737, Location.convert("50:43:49.21"), Location.convert("7:13:11.66"), 123, dateTime));
        dummies.append(R.drawable.fit_4410168, new Photo(R.drawable.fit_4410168, Location.convert("50:45:22.80"),
                                                         Location.convert("7:13:56.62"), 122, dateTime));
//      _dummies.append(R.drawable.fit_12610204, new Photo(R.drawable.fit_12610204, Location.convert("50:45:17.73"), Location.convert("7:12:51.92"), 126, dateTime));
        dummies.append(R.drawable.fit_14308344, new Photo(R.drawable.fit_14308344, Location.convert("50:44:57.02"),
                                                          Location.convert("7:13:24.18"), 127, dateTime));
        dummies.append(R.drawable.fit_1798151678_af72c8f78d, new Photo(R.drawable.fit_1798151678_af72c8f78d,
                                                                       Location.convert("50:44:58"),
                                                                       Location.convert("7:12:21"), 126, dateTime));
        dummies.append(R.drawable.fit_2580082727_1faf043ec1, new Photo(R.drawable.fit_2580082727_1faf043ec1,
                                                                       Location.convert("50:44:5"),
                                                                       Location.convert("7:12:19"), 125, dateTime));
        dummies.append(R.drawable.fit_2417313476_d588a4e2b5, new Photo(R.drawable.fit_2417313476_d588a4e2b5,
                                                                       Location.convert("50:44:56"),
                                                                       Location.convert("7:12:23"), 124, dateTime));
        
        // dummy photos (stuff near Thomas)
//      _dummies.append(R.drawable.tom_0047, new Photo(R.drawable.tom_0047, Location.convert("50:55:38.4"), Location.convert("6:56:31.2"), 102, dateTime));
//      _dummies.append(R.drawable.tom_0049, new Photo(R.drawable.tom_0049, Location.convert("50:55:37.8"), Location.convert("6:56:31.2"), 102, dateTime));
    }
}
