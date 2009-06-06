package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

/**
 * This view is used by the {@link PhotosView} to display the border for a photo.
 * It's always used in conjunction with a {@link PhotoView} which displays the photo. The decoupling is needed cause
 * photo borders should be visible even if the photo itself is occluded.
 * The border is a thin {@link #PhotoCompassApplication.ORANGE} line than is less opaque the more photos occlude the 
 * one this border belongs to.
 */
final class PhotoBorderView extends View {

	private static final float BORDER_WIDTH = 3.1F; // stroke width of the border
	private static final int ALPHA_DECREASE_PER_OCCLUSION = 15; // number by which the alpha value gets decreased for 
															    // each photo occluding the one this border belongs to
	
	private int _width = 0;
	private int _height = 0;
	private final Paint _paint = new Paint();
	
	/**
	 * Constructor.
	 * Initializes the {@link #_paint}.
	 * 
	 * @param context
	 */
	PhotoBorderView(final Context context) {
		super(context);
		_paint.setColor(PhotoCompassApplication.ORANGE);
		_paint.setStrokeWidth(BORDER_WIDTH);
	}
	
	/**
	 * Set the number of photo occluding the one this border belongs to.
	 * The more occlusions, the lower the alpha value of the {@link #_paint} is set.
	 * 
	 * @param numOcclusions Number of photos occluding the photo this border belongs to.
	 */
	void setNumberOfOcclusions(final int numOcclusions) {
		final int alpha = 255 - numOcclusions * ALPHA_DECREASE_PER_OCCLUSION;
		_paint.setAlpha(alpha);
	}
	
	/**
	 * Called when the view should render its content.
	 * All drawing is done here.
	 */
    @Override 
    protected void onDraw(final Canvas canvas) {
    	canvas.drawLine(0, 0, _width, 0, _paint); 
    	canvas.drawLine(_width, 0, _width, _height, _paint); 
    	canvas.drawLine(_width, _height, 0, _height, _paint); 
    	canvas.drawLine(0, _height, 0, 0, _paint); 
    }
    
    /**
     * Called by the {@link PhotosView} when the view is repositioned or resized.
     * We intercept to get the new dimensions.
     */
    @Override
    public void setLayoutParams(final ViewGroup.LayoutParams params) {
		_width = params.width;
		_height = params.height;
        super.setLayoutParams(params);
    }
}
