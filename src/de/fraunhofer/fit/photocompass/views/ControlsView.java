package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.activities.CameraActivity;
import de.fraunhofer.fit.photocompass.activities.IServiceActivity;
import de.fraunhofer.fit.photocompass.model.ISettingsCallback;
import de.fraunhofer.fit.photocompass.model.Settings;
import de.fraunhofer.fit.photocompass.views.controls.DoubleSeekBar;
import de.fraunhofer.fit.photocompass.views.controls.HorizontalDoubleSeekBar;
import de.fraunhofer.fit.photocompass.views.controls.IDoubleSeekBarCallback;
import de.fraunhofer.fit.photocompass.views.controls.VerticalDoubleSeekBar;
import de.fraunhofer.fit.photocompass.views.layouts.SimpleAbsoluteLayout;

/**
 * This view is used by the {@link de.fraunhofer.fit.photocompass.activities.CameraActivity} and displays the UI
 * controls.
 */
public final class ControlsView extends SimpleAbsoluteLayout {
    
    /**
     * padding at the top of the distance control and at the right of the age control
     */
    private static final int CONTROL_END_PADDING = 8;
    /**
     * padding at the left of the distance control and at the bottom of the age control
     */
    public static final int CONTROL_SIDE_PADDING = 5;
    /**
     * padding at the bottom of the distance control and at the left of the age control
     */
    private static final int BOTTOM_LEFT_PADDING = 39;
    /**
     * width of the distance seek bar (including text labels)
     */
    public static final int DISTANCE_CONTROL_WIDTH = 120;
    /**
     * height of the age seek bar (including text labels)
     */
    private static final int AGE_CONTROL_HEIGHT = 58;
    /**
     * extra bottom padding, because the touch screen on the G1 doesn't work well at the bottom
     */
    public static final int BOTTOM_EXTRA_PADDING = 12;
    
    /**
     * Slider control on the left edge of the screen for setting the distance limits. Package scoped for faster access
     * by inner classes.
     */
    DoubleSeekBar distanceSlider = null;
    /**
     * Slider control on the bottom edge of the screen for setting the age limits. Package scoped for faster access by
     * inner classes.
     */
    DoubleSeekBar ageSlider = null;
    
    private final ISettingsCallback _settingsCallback = new ISettingsCallback.Stub() {
        
        @Override
        public void onMaxAgeChange(final long maxAge, final float maxAgeRel) throws RemoteException {

            if (ageSlider == null) return;
            ageSlider.updateEndValue(maxAgeRel);
        }
        
        @Override
        public void onMaxDistanceChange(final float maxDistance, final float maxDistanceRel) throws RemoteException {

            if (distanceSlider == null) return;
            distanceSlider.updateEndValue(maxDistanceRel);
        }
        
        @Override
        public void onMinAgeChange(final long minAge, final float minAgeRel) throws RemoteException {

            if (ageSlider == null) return;
            ageSlider.updateStartValue(minAgeRel);
        }
        
        @Override
        public void onMinDistanceChange(final float minDistance, final float minDistanceRel) throws RemoteException {

            if (distanceSlider == null) return;
            distanceSlider.updateStartValue(minDistanceRel);
        }
    };
    
    /**
     * Constructor. Sets up the controls and registers as a callback at the application model.
     * 
     * @param context
     * @param activity {@link CameraActivity} that uses this view.
     * @param availableWidth Maximum width for this view (the display width).
     * @param availableHeight Maximum height for this view (the display height).
     * @param showDistanceControl Show the distance control.
     * @param showAgeControl Show the age control.
     * @param lightBackground Whether the Controls are drawn on a light ( <code>true</code>) or a dark (
     *        <code>false</code>) background.
     */
    public ControlsView(final Context context, final IServiceActivity activity, final int availableWidth,
                        final int availableHeight, final boolean showDistanceControl, final boolean showAgeControl,
                        final boolean lightBackground) {

        super(context);
        
        Log.d(PhotoCompassApplication.LOG_TAG, "ControlsView");
        
        // distance slider
        if (showDistanceControl) {
            distanceSlider = new VerticalDoubleSeekBar(context, new IDoubleSeekBarCallback() {
                
                public String getMaxLabel() {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: VerticalDoubleSeekBar: IDoubleSeekBarCallback: getMaxLabel: settings is null");
                        return "";
                    }
                    
                    return settings.maxDistanceStr;
                }
                
                public float getMaxValue() {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: VerticalDoubleSeekBar: IDoubleSeekBarCallback: getMaxValue: settings is null");
                        return 0;
                    }
                    
                    return settings.maxDistanceRel;
                }
                
                public String getMinLabel() {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: VerticalDoubleSeekBar: IDoubleSeekBarCallback: getMinLabel: settings is null");
                        return "";
                    }
                    
                    return settings.minDistanceStr;
                }
                
                public float getMinValue() {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: VerticalDoubleSeekBar: IDoubleSeekBarCallback: getMinValue: settings is null");
                        return 0;
                    }
                    
                    return settings.minDistanceRel;
                }
                
                public void onMaxValueChange(final float newValue) {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: VerticalDoubleSeekBar: IDoubleSeekBarCallback: onMaxValueChange: settings is null");
                        return;
                    }
                    
                    settings.setRelativeMaxDistance(newValue);
                    activity.updateSettings(settings);
                }
                
                public void onMinValueChange(final float newValue) {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: VerticalDoubleSeekBar: IDoubleSeekBarCallback: onMinValueChange: settings is null");
                        return;
                    }
                    
                    settings.setRelativeMinDistance(newValue);
                    activity.updateSettings(settings);
                }
            }, lightBackground);
            final int bottomPadding = showAgeControl ? BOTTOM_LEFT_PADDING : CONTROL_END_PADDING;
            distanceSlider.setLayoutParams(new LayoutParams(DISTANCE_CONTROL_WIDTH, availableHeight -
                                                                                    CONTROL_END_PADDING -
                                                                                    bottomPadding -
                                                                                    BOTTOM_EXTRA_PADDING,
                                                            CONTROL_SIDE_PADDING, CONTROL_END_PADDING));
            addView(distanceSlider);
        }
        
        // age slider
        if (showAgeControl) {
            ageSlider = new HorizontalDoubleSeekBar(context, new IDoubleSeekBarCallback() {
                
                public String getMaxLabel() {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: HorizontalDoubleSeekBar: IDoubleSeekBarCallback: getMaxLabel: settings is null");
                        return "";
                    }
                    
                    return settings.maxAgeStr;
                }
                
                public float getMaxValue() {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: HorizontalDoubleSeekBar: IDoubleSeekBarCallback: getMaxValue: settings is null");
                        return 0;
                    }
                    
                    return settings.maxAgeRel;
                }
                
                public String getMinLabel() {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: HorizontalDoubleSeekBar: IDoubleSeekBarCallback: getMinLabel: settings is null");
                        return "";
                    }
                    
                    return settings.minAgeStr;
                }
                
                public float getMinValue() {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: HorizontalDoubleSeekBar: IDoubleSeekBarCallback: getMinValue: settings is null");
                        return 0;
                    }
                    
                    return settings.minAgeRel;
                }
                
                public void onMaxValueChange(final float newValue) {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: HorizontalDoubleSeekBar: IDoubleSeekBarCallback: onMaxValueChange: settings is null");
                        return;
                    }
                    
                    settings.setRelativeMaxAge(newValue);
                    activity.updateSettings(settings);
                }
                
                public void onMinValueChange(final float newValue) {

                    final Settings settings = activity.getSettings();
                    if (settings == null) {
                        Log.e(PhotoCompassApplication.LOG_TAG,
                              "ControlsView: HorizontalDoubleSeekBar: IDoubleSeekBarCallback: onMinValueChange: settings is null");
                        return;
                    }
                    
                    settings.setRelativeMinAge(newValue);
                    activity.updateSettings(settings);
                }
            }, lightBackground);
            final int xPos = showDistanceControl ? BOTTOM_LEFT_PADDING : CONTROL_END_PADDING;
            ageSlider.setLayoutParams(new LayoutParams(availableWidth - CONTROL_END_PADDING - xPos, AGE_CONTROL_HEIGHT,
                                                       xPos, availableHeight - AGE_CONTROL_HEIGHT -
                                                             CONTROL_SIDE_PADDING - BOTTOM_EXTRA_PADDING));
            addView(ageSlider);
        }
    }
    
    public Settings registerToSettings(final Settings settings) {

        Log.d(PhotoCompassApplication.LOG_TAG, "ControlsView: registerToSettings");
        
        // FIXME
//        settings.registerCallback(_settingsCallback);
        
        return settings;
    }
    
    public Settings unregisterFromSettings(final Settings settings) {

        Log.d(PhotoCompassApplication.LOG_TAG, "ControlsView: unregisterFromSettings");
        
        // FIXME
//        settings.unregisterCallback(_settingsCallback);
        
        return settings;
    }
    
    public void setPhotoAges(final float[] photoAges) {

        Log.d(PhotoCompassApplication.LOG_TAG, "ControlsView: setPhotoAges");
        
        if (ageSlider == null) return;
        ageSlider.setPhotoMarks(photoAges);
    }
    
    public void setPhotoDistances(final float[] photoDistances) {

        Log.d(PhotoCompassApplication.LOG_TAG, "ControlsView: setPhotoDistances");
        
        if (distanceSlider == null) return;
        distanceSlider.setPhotoMarks(photoDistances);
    }
}
