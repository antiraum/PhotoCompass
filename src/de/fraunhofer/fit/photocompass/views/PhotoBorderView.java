package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

/**
 * This view is used by the {@link PhotosView} to display the border for a photo. It's always used in conjunction with a
 * {@link PhotoView} which displays the photo. The decoupling is needed cause photo borders should be visible even if
 * the photo itself is occluded. The border is a thin {@link PhotoCompassApplication#ORANGE} line than is less opaque
 * the more photos occlude the one this border belongs to.
 */
final class PhotoBorderView extends View {
    
    private static final float BORDER_WIDTH = 3.6F; // stroke width of the border
    private static final float BORDER_WIDTH_DECREASE_PER_OCCLUSION = 0.25F; // number by which the stroke width of the border
    // gets decreased for each photo occluding the
    // one this border belongs to
    private static final float MIN_BORDER_WIDTH = 2.1F; // minimum stroke width of the border
    private static final int ALPHA_DECREASE_PER_OCCLUSION = 22; // number by which the alpha value gets decreased for
    // each photo occluding the one this border belongs to
    private static final int MINIMAL_ALPHA = 33; // minimum alpha value
    
    private int _width = 0;
    private int _height = 0;
    private final Paint _paint = new Paint();
    
    /**
     * Constructor. Initializes the paint object.
     * 
     * @param context
     */
    PhotoBorderView(final Context context) {

        super(context);
        _paint.setColor(PhotoCompassApplication.ORANGE);
        _paint.setStrokeWidth(BORDER_WIDTH);
    }
    
    /**
     * Set the number of photo occluding the one this border belongs to. The more occlusions, the lower the alpha value
     * of the paint is set.
     * 
     * @param numOcclusions Number of photos occluding the photo this border belongs to.
     */
    void setNumberOfOcclusions(final int numOcclusions) {

        final int alpha = 255 - numOcclusions * ALPHA_DECREASE_PER_OCCLUSION;
        _paint.setAlpha(Math.max(alpha, MINIMAL_ALPHA));
        final float strokeWidth = BORDER_WIDTH - numOcclusions * BORDER_WIDTH_DECREASE_PER_OCCLUSION;
        _paint.setStrokeWidth(Math.max(strokeWidth, MIN_BORDER_WIDTH));
//    	Log.d(PhotoCompassApplication.LOG_TAG, "PhotoBorderView: setNumberOfOcclusions: alpha = "+alpha+", strokeWidth = "+strokeWidth);
    }
    
    /**
     * Called when the view should render its content. All drawing is done here.
     */
    @Override
    protected void onDraw(final Canvas canvas) {

        canvas.drawLine(0, 0, _width, 0, _paint);
        canvas.drawLine(_width, 0, _width, _height, _paint);
        canvas.drawLine(_width, _height, 0, _height, _paint);
        canvas.drawLine(0, _height, 0, 0, _paint);
    }
    
    /**
     * Called to determine the size requirements for this view and all of its children.
     */
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        setMeasuredDimension(_width, _height);
    }
    
    /**
     * Called by the {@link PhotosView} when the view is repositioned or resized. We intercept to get the new
     * dimensions.
     */
    @Override
    public void setLayoutParams(final ViewGroup.LayoutParams params) {

        _width = params.width;
        _height = params.height;
        super.setLayoutParams(params);
    }
}
