package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;

public class PhotoView extends ImageView {
	
	public PhotoView (Context context, int photoId) { 
		super(context);
		setScaleType(ImageView.ScaleType.FIT_XY); 
        setImageResource(photoId); 
	} 
}