package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.model.data.Photo;
import de.fraunhofer.fit.photocompass.views.layouts.SimpleAbsoluteLayout;

/**
 * This view is used by the {@link PhotosView} to display a photo.
 * It shows the photo and an overlaying text with information about the photo.
 */
final class PhotoView extends SimpleAbsoluteLayout {
	
	private Photo _photo; // Photo object for the displayed photo
	private ImageView _imgView;
	private TextView _textView;
	private int _width; // current width
	private int _height; // current height
	private boolean _minimized = false;
	
	/**
	 * Constructor.
	 * Initializes Image and Text View.
	 * 
	 * @param context
	 * @param id      Photo/resource id of the photo to display.
	 */
	PhotoView (final Context context, final int id) { 
		super(context);
		
		_photo = Photos.getInstance().getPhoto(id);
		
		// image view
		_imgView = new ImageView(context);
//		_imgView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(_imgView);
        
        // distance and altitude offset text
        _textView = new TextView(context);
        updateText();
        _textView.setTextColor(PhotoCompassApplication.ORANGE);
        _textView.setPadding(5, 0, 5, 0);
        addView(_textView);
	}
	
	/**
	 * Updates the Text View.
	 * Call this when the distance or altitude offset of the photo has changed.
	 */
	void updateText() {

		final StringBuilder stringBuilder = new StringBuilder();
        
        // distance
		stringBuilder.append(_photo.getFormattedDistance());
        stringBuilder.append(" away");
        
        // altitude offset
//    	stringBuilder.append(_photo.getFormattedAltOffset());
        
        _textView.setText(stringBuilder.toString());
	}
    
    /**
     * Called by the {@link PhotosView} when the view is repositioned or resized.
     * We intercept to get the new dimensions and pre-scale the bitmap for better performance.
     */
    @Override
    public void setLayoutParams(final ViewGroup.LayoutParams params) {
		
		// create pre-scaled bitmap if the dimensions changed
		if (_width != params.width || _height != params.height) {
    	
			_width = params.width;
			_height = params.height;
			
			Bitmap rawBmp;
			if (_photo.isDummyPhoto()) {
				rawBmp = BitmapFactory.decodeResource(getResources(), _photo.getId());
			} else {
				rawBmp = BitmapFactory.decodeFile(_photo.thumbUri.getPath());
			}
			if (rawBmp == null) { // file does not exists
		    	return;
			}
			_imgView.setImageBitmap(Bitmap.createScaledBitmap(rawBmp, _width, _height, true));
			rawBmp.recycle();
			rawBmp = null;
		}
		
        super.setLayoutParams(params);
    }
	
	/**
	 * Changes the minimized state.
	 * 
	 * @param value <code>true</code> to minimize the photo, or
	 * 				<code>false</code> to restore the photo.
	 */
	void setMinimized(final boolean value) {
		_minimized = value;
		_textView.setVisibility(_minimized ? View.GONE : View.VISIBLE);
	}

	/**
	 * Get the minimized state.
	 * 
	 * @return <code>true</code> when the photo is minimized, or
	 * 		   <code>false</code> when the photo is not minimized.
	 */
	boolean isMinimized() {
		return _minimized;
	}
}