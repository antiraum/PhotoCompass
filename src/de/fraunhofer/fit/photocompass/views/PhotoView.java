package de.fraunhofer.fit.photocompass.views;

import java.util.Formatter;

import android.content.Context;
import android.graphics.Color;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

public class PhotoView extends AbsoluteLayout {
	
	private boolean _minimized = false;
	
	public PhotoView (Context context, int photoId, float distance) { 
		super(context);
		
		// photo
		ImageView imgView = new ImageView(context);
		imgView.setScaleType(ImageView.ScaleType.FIT_XY); 
		imgView.setImageResource(photoId); 
        addView(imgView);
        
        // distance text
        TextView textView = new TextView(context);
        String text;
        if (distance < 1000) {
        	text = (int)Math.round(distance)+" m";
        } else {
        	Formatter fmt = new Formatter();
            fmt.format("%.1f", distance / 1000); 
            text = fmt+" km";
        }
        textView.setText(text);
        textView.setTextColor(Color.parseColor(PhotoCompassApplication.ORANGE));
        textView.setPadding(5, 0, 5, 0);
        addView(textView);
	}
	
	public void setMinimized(boolean value) {
		_minimized = value;
	}
	
	public boolean isMinimized() {
		return _minimized;
	}
}