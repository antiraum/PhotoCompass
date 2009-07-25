package de.fraunhofer.fit.photocompass.views.layouts;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Container for the map view of the {@link de.fraunhofer.fit.photocompass.activities.PhotoMapActivity}. Taken from
 * com.example.android.apis.view.MapViewCompassDemo.
 */
public final class RotateView extends ViewGroup {
    
    private static final float SQ2 = 1.414213562373095f;
    private final SmoothCanvas _canvas = new SmoothCanvas();
    private float _heading = 0;
    
    
    public RotateView(final Context context) {

        super(context);
    }
    

    public void setHeading(final float value) {

        _heading = value;
        invalidate();
    }
    

    @Override
    protected void dispatchDraw(final Canvas canvas) {

        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(0, 80); // move current location to bottom
        canvas.rotate(-_heading, getWidth() * 0.5f, getHeight() * 0.5f);
        _canvas.delegate = canvas;
        super.dispatchDraw(_canvas);
        canvas.restore();
    }
    

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {

        final int width = getWidth();
        final int height = getHeight();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            final int childWidth = view.getMeasuredWidth();
            final int childHeight = view.getMeasuredHeight();
            final int childLeft = (width - childWidth) / 2;
            final int childTop = (height - childHeight) / 2;
            view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }
    }
    

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        final int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int sizeSpec;
        if (w > h)
            sizeSpec = MeasureSpec.makeMeasureSpec((int) (w * SQ2), MeasureSpec.EXACTLY);
        else
            sizeSpec = MeasureSpec.makeMeasureSpec((int) (h * SQ2), MeasureSpec.EXACTLY);
        final int count = getChildCount();
        for (int i = 0; i < count; i++)
            getChildAt(i).measure(sizeSpec, sizeSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {

        // TODO rotate events too
        return super.dispatchTouchEvent(event);
    }
}