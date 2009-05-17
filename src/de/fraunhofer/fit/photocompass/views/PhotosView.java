package de.fraunhofer.fit.photocompass.views;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.widget.AbsoluteLayout;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.model.data.PhotoLayout;
import de.fraunhofer.fit.photocompass.model.data.PhotoLayoutComparator;

// TODO as AbsoluteLayout is depreciated in 1.5, we should implement our own layout
public class PhotosView extends AbsoluteLayout {
	
	private static final int MAX_PHOTO_HEIGHT = 200;
	private Context _context;

	public PhotosView(Context context) {
        super(context);
        _context = context;
	}
	
	public void setPhotos(LinkedList<Photo> photos) {

        int screenHeight = 240; // TODO not hard coded (canvas.getHeight())
        int statusbarHeight = 25; // TODO not hard coded
        
        // calculate the photo sizes and positions
        TreeMap<PhotoLayout, Photo> photoLayouts = new TreeMap<PhotoLayout, Photo>(new PhotoLayoutComparator());
        for (Photo photo : photos) {
	        int photoHeight = (int)(MAX_PHOTO_HEIGHT * photo.getDistance(0f, 0f));
	        int photoWidth = photoHeight / 4 * 3;
	        int photoX = (int)(4 * photo.getAngle(0f, 0f));
	        int photoY = statusbarHeight + (screenHeight - photoHeight) / 2;
	        photoLayouts.put(new PhotoLayout(photoX, photoY, photoWidth, photoHeight), photo);
        }
        
        // add photo views
        for (Map.Entry<PhotoLayout, Photo> photoLayout : photoLayouts.entrySet()) {
	        PhotoView pView = new PhotoView(_context, photoLayout.getValue().getResourceId());
	        pView.setLayoutParams(photoLayout.getKey().getAbsoluteLayoutParams());
	        addView(pView);
        }
        
        // add photo border views
        for (PhotoLayout photoLayout : photoLayouts.keySet()) {
	        PhotoBorderView pbView = new PhotoBorderView(_context, photoLayout.getWidth(), photoLayout.getHeight());
	        pbView.setLayoutParams(photoLayout.getAbsoluteLayoutParams());
	        addView(pbView);
        }
	}
	
}
