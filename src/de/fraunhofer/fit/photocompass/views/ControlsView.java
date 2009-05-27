package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import de.fraunhofer.fit.photocompass.R;

public class ControlsView extends AbsoluteLayout {
	
	public ControlsView(Context context) {
		super(context);
		
        DoubleSeekBar distanceSlider = new DoubleSeekBar(context, DoubleSeekBar.VERTICAL);
        distanceSlider.setLayoutParams(new LayoutParams(62, 257, 6, 7));
        addView(distanceSlider);

//        SeekBar slider = new SeekBar(context);
//        slider.setLayoutParams(new LayoutParams(441,29,32,200));
//        this.addView(slider);
//                
        DoubleSeekBar ageSlider = new DoubleSeekBar(context, DoubleSeekBar.HORIZONTAL);
//        ageSliderDummy.setImageResource(R.drawable.age_slider_dummy); 
        ageSlider.setLayoutParams(new LayoutParams(441, 41, 32, 249));
        addView(ageSlider);
    }
}
