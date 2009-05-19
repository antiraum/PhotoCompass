package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import de.fraunhofer.fit.photocompass.R;

public class ControlsView extends AbsoluteLayout {
	
	public ControlsView(Context context) {
		super(context);
		
        ImageView distanceSliderDummy = new ImageView(context);
        distanceSliderDummy.setScaleType(ImageView.ScaleType.FIT_XY); 
        distanceSliderDummy.setImageResource(R.drawable.distance_slider_dummy); 
        distanceSliderDummy.setLayoutParams(new LayoutParams(62, 257, 6, 7));
        addView(distanceSliderDummy);

        ImageView ageSliderDummy = new ImageView(context);
        ageSliderDummy.setScaleType(ImageView.ScaleType.FIT_XY); 
        ageSliderDummy.setImageResource(R.drawable.age_slider_dummy); 
        ageSliderDummy.setLayoutParams(new LayoutParams(441, 41, 32, 249));
        addView(ageSliderDummy);
    }
}
