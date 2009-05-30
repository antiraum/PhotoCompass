package de.fraunhofer.fit.photocompass.views;

import java.util.Formatter;

import android.content.Context;
import android.graphics.Color;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.data.Photo;

public class PhotoView extends AbsoluteLayout {
	
	private Photo _photo;
	private boolean _minimized = false;
	
	public PhotoView (Context context, Photo photo) { 
		super(context);
		_photo = photo;
		
		// photo
		ImageView imgView = new ImageView(context);
		imgView.setScaleType(ImageView.ScaleType.FIT_XY); 
		imgView.setImageResource(_photo.getResourceId()); 
        addView(imgView);
        
        // distance and altitude offset text
        TextView textView = new TextView(context);
        String text;
        if (_photo.getDistance() < 1000) {
        	text = (int)Math.round(_photo.getDistance())+" m";
        } else {
        	Formatter fmt = new Formatter();
            fmt.format("%.1f", _photo.getDistance() / 1000); 
            text = fmt+" km";
        }
        text += " away";
        if (photo.getAltOffset() == 0) {
        	text += "\nsame level";
        } else {
        	text += "\n"+Math.abs(Math.round(_photo.getAltOffset()))+" m ";
        	text += (photo.getAltOffset() > 0) ? "higher" : "lower";
        }
        textView.setText(text);
        textView.setTextColor(Color.parseColor(PhotoCompassApplication.ORANGE));
        textView.setPadding(5, 0, 5, 0);
        addView(textView);
	}
	
	public Photo getPhoto() {
		return _photo;
	}
	
	public void setMinimized(boolean value) {
		_minimized = value;
	}
	
	public boolean isMinimized() {
		return _minimized;
	}
}