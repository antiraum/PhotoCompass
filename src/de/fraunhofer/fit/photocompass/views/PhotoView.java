package de.fraunhofer.fit.photocompass.views;

import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import android.content.Context;
import android.graphics.Color;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AbsoluteLayout.LayoutParams;

public class PhotoView extends AbsoluteLayout {
	
	public PhotoView (Context context, int photoId, float distance) { 
		super(context);
//		this.setLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		ImageView imgView = new ImageView(context);
		imgView.setScaleType(ImageView.ScaleType.FIT_XY); 
		imgView.setImageResource(photoId); 
        addView(imgView);
        TextView textView = new TextView(context);
        textView.setText((int)Math.round(distance)+" m");
        textView.setTextColor(Color.parseColor(PhotoCompassApplication.ORANGE));
        textView.setPadding(5, 0, 5, 0);
        addView(textView);
	} 
}