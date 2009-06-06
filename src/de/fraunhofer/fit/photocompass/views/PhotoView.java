package de.fraunhofer.fit.photocompass.views;

import java.util.Formatter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.model.data.Photo;

/**
 * This view is used by the {@link PhotosView} to display a photo.
 * It shows the photo and an overlaying text with information about the photo.
 */
final class PhotoView extends AbsoluteLayout {
	
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
		
        String text;
        
        // distance
        final float photoDistance = _photo.getDistance();
        if (photoDistance < 1000) {
        	text = (int)Math.round(photoDistance)+" m";
        } else {
        	final Formatter fmt = new Formatter();
            fmt.format("%.1f", photoDistance / 1000); 
            text = fmt+" km";
        }
        text += " away";
        
        // altitude offset
        final double photoAltOffset = _photo.getAltOffset();
        if (photoAltOffset == 0) {
        	text += "\non same level";
        } else {
        	text += "\n"+Math.abs(Math.round(photoAltOffset))+" m ";
        	text += (photoAltOffset > 0) ? "higher" : "lower";
        }
        
        _textView.setText(text);
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
				rawBmp = BitmapFactory.decodeFile(_photo.getThumbUri().getPath());
			}
			_imgView.setImageBitmap(Bitmap.createScaledBitmap(rawBmp, _width, _height, true));
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