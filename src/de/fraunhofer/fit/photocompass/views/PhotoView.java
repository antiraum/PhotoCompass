package de.fraunhofer.fit.photocompass.views;

import java.util.Formatter;

import android.content.Context;
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
public class PhotoView extends AbsoluteLayout {
	
	private int _resourceId; // resource id of the displayed photo
	private TextView _textView;
	private boolean _minimized = false;
	
	/**
	 * Constructor.
	 * Initializes Image and Text View.
	 * @param context
	 * @param resourceId Resource id of the photo to display.
	 */
	public PhotoView (Context context, Integer resourceId) { 
		super(context);
		_resourceId = resourceId;
		
		// image view
		ImageView imgView = new ImageView(context);
		imgView.setScaleType(ImageView.ScaleType.FIT_XY); 
		imgView.setImageResource(resourceId); 
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
	public void updateText() {
		
		Photo photo = Photos.getInstance().getPhoto(_resourceId);
        String text;
        
        // distance
        float photoDistance = photo.getDistance();
        if (photoDistance < 1000) {
        	text = (int)Math.round(photoDistance)+" m";
        } else {
        	Formatter fmt = new Formatter();
            fmt.format("%.1f", photoDistance / 1000); 
            text = fmt+" km";
        }
        text += " away";
        
        // altitude offset
        double photoAltOffset = photo.getAltOffset();
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
	 * @param value <code>true</code> to minimize the photo, or
	 * 				<code>false</code> to restore the photo.
	 */
	public void setMinimized(boolean value) {
		_minimized = value;
		_textView.setVisibility(_minimized ? View.GONE : View.VISIBLE);
	}

	/**
	 * Get the minimized state.
	 * @return <code>true</code> when the photo is minimized, or
	 * 		   <code>false</code> when the photo is not minimized.
	 */
	public boolean isMinimized() {
		return _minimized;
	}
}