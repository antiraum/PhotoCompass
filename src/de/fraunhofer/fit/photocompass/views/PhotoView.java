package de.fraunhofer.fit.photocompass.views;

import java.util.Formatter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
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
	private TextView _textView;
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
		final ImageView imgView = new ImageView(context);
		imgView.setScaleType(ImageView.ScaleType.FIT_XY);
		if (_photo.isDummyPhoto()) {
			imgView.setImageResource(id);
		} else {
			// TODO this should work without loading the bitmap data by hand
			imgView.setImageURI(_photo.getThumbUri());
			final Bitmap bmp = BitmapFactory.decodeFile(_photo.getThumbUri().getPath());
			imgView.setImageBitmap(bmp);
//			bmp.recycle();
		}
        addView(imgView);
        
        // distance and altitude offset text
        _textView = new TextView(context);
        updateText();
        _textView.setTextColor(Color.parseColor(PhotoCompassApplication.ORANGE));
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