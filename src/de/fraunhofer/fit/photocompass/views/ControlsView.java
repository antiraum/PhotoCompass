package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.widget.AbsoluteLayout;
import de.fraunhofer.fit.photocompass.R;

public class ControlsView extends AbsoluteLayout {
	
	public ControlsView(Context context) {
		super(context);
        PhotoView distanceSliderDummy = new PhotoView(context, R.drawable.distance_slider_dummy);
        addView(distanceSliderDummy);
        distanceSliderDummy.setLayoutParams(new LayoutParams(62, 257, 6, 7));
        PhotoView ageSliderDummy = new PhotoView(context, R.drawable.age_slider_dummy);
        addView(ageSliderDummy);
        ageSliderDummy.setLayoutParams(new LayoutParams(441, 41, 32, 249));
        
    }
}
