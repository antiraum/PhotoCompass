package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import de.fraunhofer.fit.photocompass.R;

/**
 * This view is used by the {@link de.fraunhofer.fit.photocompass.activities.FinderActivity} and the UI controls.
 */
public final class ControlsView extends AbsoluteLayout {
	
	/**
	 * Constructor.
	 * Sets up the controls.
	 * 
	 * @param context
	 */
	public ControlsView(final Context context) {
		super(context);

        ImageView distanceSliderDummy = new ImageView(context);
        distanceSliderDummy.setScaleType(ImageView.ScaleType.FIT_XY); 
        distanceSliderDummy.setImageResource(R.drawable.distance_slider_dummy); 
        distanceSliderDummy.setLayoutParams(new LayoutParams(62, 257, 6, 7));
        addView(distanceSliderDummy);
		
//        DoubleSeekBar distanceSlider = new DoubleSeekBar(context, DoubleSeekBar.VERTICAL);
//        distanceSlider.setLayoutParams(new LayoutParams(62, 257, 6, 7));
//        addView(distanceSlider);

        ImageView ageSliderDummy = new ImageView(context);
        ageSliderDummy.setScaleType(ImageView.ScaleType.FIT_XY); 
        ageSliderDummy.setImageResource(R.drawable.age_slider_dummy); 
        ageSliderDummy.setLayoutParams(new LayoutParams(441, 41, 32, 249));
        addView(ageSliderDummy);
                
//        DoubleSeekBar ageSlider = new DoubleSeekBar(context, DoubleSeekBar.HORIZONTAL);
//        ageSlider.setLayoutParams(new LayoutParams(441, 41, 32, 249));
//        addView(ageSlider);
    }
}
