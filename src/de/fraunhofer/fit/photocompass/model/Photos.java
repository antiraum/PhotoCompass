package de.fraunhofer.fit.photocompass.model;

import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.R;
import java.util.LinkedList;

public class Photos {

    private static Photos _instance;
	private static LinkedList<Photo> _photos;
	
	public Photos() {
		
	    _photos = new LinkedList<Photo>();
	    // TODO: get camera photos (look into MediaStore.Images.Thumbnails and MediaStore.Images.Media)
	    
	    // dummy Photos
	    _photos.add(new Photo(R.drawable.photo_0518, 0.0f, 0.0f));
	    _photos.add(new Photo(R.drawable.photo_0519, 0.0f, 0.0f));
	    _photos.add(new Photo(R.drawable.photo_0520, 0.0f, 0.0f));
	    _photos.add(new Photo(R.drawable.photo_0521, 0.0f, 0.0f));
	    _photos.add(new Photo(R.drawable.photo_0522, 0.0f, 0.0f));
	    _photos.add(new Photo(R.drawable.photo_0523, 0.0f, 0.0f));
	    _photos.add(new Photo(R.drawable.photo_0524, 0.0f, 0.0f));
	    _photos.add(new Photo(R.drawable.photo_0525, 0.0f, 0.0f));
	    // more photos -> OutOfMemory exception WTF?
	    // we may need to process the photos before loading
	}

    public static Photos getInstance() {
        if (_instance == null) _instance = new Photos();
        return _instance;
    }
    
    // TODO getPhotos should only return the photos in the current viewing direction
    public LinkedList<Photo> getPhotos() {
    	return _photos;
    }
}
