package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.os.RemoteException;
import android.widget.AbsoluteLayout;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;
import de.fraunhofer.fit.photocompass.model.IApplicationModelCallback;
import de.fraunhofer.fit.photocompass.views.controls.DoubleSeekBar;
import de.fraunhofer.fit.photocompass.views.controls.HorizontalDoubleSeekBar;
import de.fraunhofer.fit.photocompass.views.controls.IDoubleSeekBarCallback;
import de.fraunhofer.fit.photocompass.views.controls.VerticalDoubleSeekBar;

/**
 * This view is used by the {@link de.fraunhofer.fit.photocompass.activities.FinderActivity} and displays the UI controls.
 */
public final class ControlsView extends AbsoluteLayout {

	/**
	 * Constructor.
	 * Sets up the controls and registers as a callback at the application model.
	 * 
	 * @param context
	 */
	public ControlsView(final Context context) {
		super(context);

		// distance slider
		final DoubleSeekBar distanceSlider = new VerticalDoubleSeekBar(context, new IDoubleSeekBarCallback() {
			ApplicationModel model = ApplicationModel.getInstance();
			
			public String getMaxLabel() {
				return model.getFormattedMaxDistance();
			}

			public float getMaxValue() {
				return model.getRelativeMaxDistance();
			}

			public String getMinLabel() {
				return model.getFormattedMinDistance();
			}

			public float getMinValue() {
				return model.getRelativeMinDistance();
			}

			public void onMaxValueChange(float newValue) {
				model.setRelativeMaxDistance(newValue);
			}

			public void onMinValueChange(float newValue) {
				model.setRelativeMinDistance(newValue);
			}		
		});
		distanceSlider.setLayoutParams(new LayoutParams(150, 240, 6, 7)); // TODO no absolute values
		addView(distanceSlider);

		// age slider
		final DoubleSeekBar ageSlider = new HorizontalDoubleSeekBar(context, new IDoubleSeekBarCallback() {
			ApplicationModel model = ApplicationModel.getInstance();
			
			public String getMaxLabel() {
				return model.getFormattedMaxAge();
			}

			public float getMaxValue() {
				return model.getRelativeMaxAge();
			}

			public String getMinLabel() {
				return model.getFormattedMinAge();
			}

			public float getMinValue() {
				return model.getRelativeMinAge();
			}

			public void onMaxValueChange(float newValue) {
				model.setRelativeMaxAge(newValue);
			}

			public void onMinValueChange(float newValue) {
				model.setRelativeMinAge(newValue);
			}	
		});
		ageSlider.setLayoutParams(new LayoutParams(433, 42, 40, 241)); // TODO no absolute values
		addView(ageSlider);

		// register as a callback at the application model
		ApplicationModel.getInstance().registerCallback(
			new IApplicationModelCallback.Stub() {

				public void onMaxAgeChange(long maxAge, float maxAgeRel)
						throws RemoteException {
					ageSlider.updateEndValue(maxAgeRel);
				}

				public void onMaxDistanceChange(float maxDistance,
						float maxDistanceRel) throws RemoteException {
					distanceSlider.updateEndValue(maxDistanceRel);
				}

				public void onMinAgeChange(long minAge, float minAgeRel)
						throws RemoteException {
					ageSlider.updateStartValue(minAgeRel);
				}

				public void onMinDistanceChange(float minDistance,
						float minDistanceRel) throws RemoteException {
					distanceSlider.updateStartValue(minDistanceRel);
				}
			}
		);

	}
}
