package de.fraunhofer.fit.photocompass.views.controls;

public interface IDoubleSeekBarCallback {
	public void onMinValueChange(float newValue);
	public void onMaxValueChange(float newValue);
	public float getMinValue();
	public float getMaxValue();
	public String getMinLabel();
	public String getMaxLabel();
}
