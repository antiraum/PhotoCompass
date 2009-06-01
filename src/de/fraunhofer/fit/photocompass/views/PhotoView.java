package de.fraunhofer.fit.photocompass.views;

import java.util.Formatter;

import android.content.Context;
import android.graphics.Color;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.Photos;
import de.fraunhofer.fit.photocompass.model.data.Photo;

public class PhotoView extends AbsoluteLayout {
	
	private int _resourceId; // resource id of the displayed photo
	private boolean _minimized = false;
	
	public PhotoView (Context context, Integer resourceId) { 
		super(context);
		_resourceId = resourceId;
		Photo photo = Photos.getInstance().getPhoto(_resourceId);
		
		// photo
		ImageView imgView = new ImageView(context);
		imgView.setScaleType(ImageView.ScaleType.FIT_XY); 
		imgView.setImageResource(resourceId); 
        addView(imgView);
        
        // distance and altitude offset text
        TextView textView = new TextView(context);
        String text;
        float photoDistance = photo.getDistance();
        if (photoDistance < 1000) {
        	text = (int)Math.round(photoDistance)+" m";
        } else {
        	Formatter fmt = new Formatter();
            fmt.format("%.1f", photoDistance / 1000); 
            text = fmt+" km";
        }
        text += " away";
        double photoAltOffset = photo.getAltOffset();
        if (photoAltOffset == 0) {
        	text += "\nsame level";
        } else {
        	text += "\n"+Math.abs(Math.round(photoAltOffset))+" m ";
        	text += (photoAltOffset > 0) ? "higher" : "lower";
        }
        textView.setText(text);
        textView.setTextColor(Color.parseColor(PhotoCompassApplication.ORANGE));
        textView.setPadding(5, 0, 5, 0);
        addView(textView);
	}
	
	public int getResourceId() {
		return _resourceId;
	}
	
	public void setMinimized(boolean value) {
		_minimized = value;
	}
	
	public boolean isMinimized() {
		return _minimized;
	}
}