package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.os.RemoteException;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.IApplicationModelCallback;
import de.fraunhofer.fit.photocompass.views.controls.DoubleSeekBar;
import de.fraunhofer.fit.photocompass.views.controls.HorizontalDoubleSeekBar;
import de.fraunhofer.fit.photocompass.views.controls.IDoubleSeekBarCallback;
import de.fraunhofer.fit.photocompass.views.controls.VerticalDoubleSeekBar;
import de.fraunhofer.fit.photocompass.views.layouts.SimpleAbsoluteLayout;

/**
 * This view is used by the {@link de.fraunhofer.fit.photocompass.activities.FinderActivity} and displays the UI controls.
 */
public final class ControlsView extends SimpleAbsoluteLayout {

	private static final int CONTROL_END_PADDING = 8; // padding at the top of the distance control and
													  // at the right of the age control
	public static final int CONTROL_SIDE_PADDING = 5; // padding at the left of the distance control and
													  // at the bottom of the age control
	private static final int BOTTOM_LEFT_PADDING = 39; // padding at the bottom of the distance control and
	   												   // at the left of the age control
	public static final int DISTANCE_CONTROL_WIDTH = 80; // width of the distance seek bar (including text labels)
	private static final int AGE_CONTROL_HEIGHT = 42; // height of the age seek bar (including text labels)
	public static final int BOTTOM_EXTRA_PADDING = 12; // extra bottom padding, because the touch screen on the G1 
													   // doesn't work well at the bottom

	DoubleSeekBar distanceSlider = null; // package scoped for faster access by inner classes
	DoubleSeekBar ageSlider = null; // package scoped for faster access by inner classes

	/**
	 * Constructor.
	 * Sets up the controls and registers as a callback at the application model.
	 * 
	 * @param context
	 * @param availableWidth  Maximum width for this view (the display width).
	 * @param availableHeight Maximum height for this view (the display height).
	 */
	public ControlsView(final Context context, final int availableWidth, final int availableHeight,
						boolean showDistanceControl, boolean showAgeControl, boolean lightBackground) {
		super(context);

		// distance slider
		if (showDistanceControl) {
			distanceSlider = new VerticalDoubleSeekBar(context, new IDoubleSeekBarCallback() {
				final ApplicationModel model = ApplicationModel.getInstance();
				
				public String getMaxLabel() {
					return model.maxDistanceStr;
				}
	
				public float getMaxValue() {
					return model.maxDistanceRel;
				}
	
				public String getMinLabel() {
					return model.minDistanceStr;
				}
	
				public float getMinValue() {
					return model.minDistanceRel;
				}
	
				public void onMaxValueChange(float newValue) {
					model.setRelativeMaxDistance(newValue);
				}
	
				public void onMinValueChange(float newValue) {
					model.setRelativeMinDistance(newValue);
				}		
			}, lightBackground);
			final int bottomPadding = showAgeControl ? BOTTOM_LEFT_PADDING : CONTROL_END_PADDING;
			distanceSlider.setLayoutParams(
				new LayoutParams(DISTANCE_CONTROL_WIDTH,
								 availableHeight - CONTROL_END_PADDING - bottomPadding - BOTTOM_EXTRA_PADDING,
								 CONTROL_SIDE_PADDING, CONTROL_END_PADDING)
			);
			addView(distanceSlider);
		}

		// age slider
		if (showAgeControl) {
			ageSlider = new HorizontalDoubleSeekBar(context, new IDoubleSeekBarCallback() {
				final ApplicationModel model = ApplicationModel.getInstance();
				
				public String getMaxLabel() {
					return model.maxAgeStr;
				}
	
				public float getMaxValue() {
					return model.maxAgeRel;
				}
	
				public String getMinLabel() {
					return model.minAgeStr;
				}
	
				public float getMinValue() {
					return model.minAgeRel;
				}
	
				public void onMaxValueChange(float newValue) {
					model.setRelativeMaxAge(newValue);
				}
	
				public void onMinValueChange(float newValue) {
					model.setRelativeMinAge(newValue);
				}	
			}, lightBackground);
			final int xPos = showDistanceControl ? BOTTOM_LEFT_PADDING : CONTROL_END_PADDING;
			ageSlider.setLayoutParams(
				new LayoutParams(
					availableWidth - CONTROL_END_PADDING - xPos, AGE_CONTROL_HEIGHT,
					xPos,
					availableHeight - AGE_CONTROL_HEIGHT - CONTROL_SIDE_PADDING - BOTTOM_EXTRA_PADDING
				)
			);
			addView(ageSlider);
		}

		// register as a callback at the application model
		ApplicationModel.getInstance().registerCallback(
			new IApplicationModelCallback.Stub() {

				public void onMaxAgeChange(long maxAge, float maxAgeRel)
						throws RemoteException {
					if (ageSlider != null) ageSlider.updateEndValue(maxAgeRel);
				}

				public void onMaxDistanceChange(float maxDistance,
						float maxDistanceRel) throws RemoteException {
					if (distanceSlider != null) distanceSlider.updateEndValue(maxDistanceRel);
				}

				public void onMinAgeChange(long minAge, float minAgeRel)
						throws RemoteException {
					if (ageSlider != null) ageSlider.updateStartValue(minAgeRel);
				}

				public void onMinDistanceChange(float minDistance,
						float minDistanceRel) throws RemoteException {
					if (distanceSlider != null) distanceSlider.updateStartValue(minDistanceRel);
				}
			}
		);

	}
}
