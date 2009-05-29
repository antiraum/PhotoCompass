package de.fraunhofer.fit.photocompass.views;

import de.fraunhofer.fit.photocompass.views.controls.DoubleSeekBar;
import android.content.Context;
import android.widget.AbsoluteLayout;

public class ControlsView extends AbsoluteLayout {
	
	public ControlsView(Context context) {
		super(context);
		
        DoubleSeekBar distanceSlider = new DoubleSeekBar(context, DoubleSeekBar.VERTICAL);
        distanceSlider.setLayoutParams(new LayoutParams(62, 257, 6, 7));
        addView(distanceSlider);
                
        DoubleSeekBar ageSlider = new DoubleSeekBar(context, DoubleSeekBar.HORIZONTAL);
        ageSlider.setLayoutParams(new LayoutParams(441, 41, 32, 249));
        addView(ageSlider);
    }
}
