package de.fraunhofer.fit.photocompass.views.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

/**
 * Abstract SeekBar (slider) class that supports the selection of an interval. For this purpose, two thumbs are
 * displayed and draggable. For concrete implementations, see {@link HorizontalDoubleSeekBar} or
 * {@link VerticalDoubleSeekBar}.
 * 
 * @author joni
 */
public abstract class DoubleSeekBar extends View {
    
    /**
     * Tolerance for MOVE touch events in pixels. Only events above this tolerance will be taken into account.
     */
    private final static float TOUCH_MOVE_TOLERANCE = 1f;
    
    /**
     * Tolerance for DOWN touch events in pixels: Events with a larger distance to the bar will be ignored.
     */
    private final static float TOUCH_DOWN_TOLERANCE = 15f;
    
    /**
     * Minimum offset of the positions of the two thumbs, in pixels.
     */
    protected final static int MINIMUM_THUMB_OFFSET = 15;
    
    protected int barThickness = 22;
    protected int barPadding = 4;
    
    private float startValue = 0f;
    private float endValue = 1f;
    
    protected float labelSize = 12f;
    protected float labelSizeHighlight = 24f;
    
    /**
     * Minimum offset (relative value) of start and end value, calculated upon resizing from MINIMUM_THUMB_OFFSET
     */
    private float minOffset = 0f;
    protected int startOffset;
    protected int endOffset;
    /**
     * Size of the bar (not of the entire control, excluding start and end offset)
     */
    protected int size;
    
    protected RectF backgroundRect;
    protected Drawable startThumb;
    protected Drawable startThumbNormal;
    protected Drawable startThumbActive;
    protected Drawable endThumb;
    protected Drawable endThumbNormal;
    protected Drawable endThumbActive;
    protected final Rect selectionRect = new Rect();
    protected int halfAThumb = -1;
    
    protected int startLabelX = 0;
    protected int startLabelY = 0;
    protected int endLabelX = 0;
    protected int endLabelY = 0;
    protected String startLabel;
    protected String endLabel;
    //
    // private float touchX = -5f;
    // private float touchY = -5f;
    
    protected final static int NONE = 0;
    protected final static int START = 1;
    protected final static int END = 2;
    
    protected int thumbDown = NONE;
    
    protected final Paint paint = new Paint();
    protected final Paint highlightPaint = new Paint();
    protected LinearGradient backgroundGradient;
    protected LinearGradient selectionGradient;
    
    protected IDoubleSeekBarCallback callback;
    
    private boolean _lightBackground = false;
    
    protected float[] _photoMarks;
    
    /**
     * Creates a new {@link DoubleSeekBar}.
     * 
     * @param context The application's context
     * @param callback The callback used for interaction with the model
     * @param lightBackground Whether the DoubleSeekBar is drawn on a light ( <code>true</code>) or a dark (
     *        <code>false</code>) background
     */
    public DoubleSeekBar(final Context context, final IDoubleSeekBarCallback callback, final boolean lightBackground) {
        
        super(context);
        this.callback = callback;
        _lightBackground = lightBackground;
        
        setStartValue(callback.getMinValue());
        startLabel = callback.getMinLabel();
        
        setEndValue(callback.getMaxValue());
        endLabel = callback.getMaxLabel();
        
        paint.setStyle(Style.FILL);
        paint.setAntiAlias(true);
        paint.setTextSize(labelSize);
        paint.setStrokeWidth(1.1F);
        
        Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar initialized");
    }
    
    protected final void initialize() {
        
        startOffset = halfAThumb;
        endOffset = halfAThumb;
    }
    
    @Override
    protected final void onDraw(final Canvas canvas) {
        
        // this.updateAllBounds();
        
        super.onDraw(canvas);
        // paint.setColor(Color.GRAY);
        paint.setShader(backgroundGradient);
        canvas.drawRoundRect(backgroundRect, 5f, 5f, paint);
        // paint.setColor(PhotoCompassApplication.ORANGE);
        paint.setShader(selectionGradient);
        canvas.drawRect(selectionRect, paint);
        
        // draw photo marks
        paint.setShader(null);
        paint.setColor(PhotoCompassApplication.RED);
        
        drawPhotoMarks(canvas);
        
        startThumb.draw(canvas);
        endThumb.draw(canvas);
        
        paint.setShader(null);
        paint.setColor(_lightBackground ? Color.DKGRAY : Color.WHITE);
        // paint.setTextSize(10);
        // Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar: text size "
        // + paint.getTextSize());
        drawLabels(canvas);
        // paint.setColor(Color.RED);
        // canvas.drawCircle(this.touchX, this.touchY, 4, this.paint);
    }
    
    protected abstract void drawPhotoMarks(Canvas canvas);
    
    protected abstract void drawLabels(Canvas canvas);
    
    /**
     * Updates size-dependent positions and values upon resizing. backgroundRect and size have to be updated beforehand
     * by the subclass.
     */
    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        
        Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar.onSizeChanged(), new size " + size);
        minOffset = (float) MINIMUM_THUMB_OFFSET / size;
        updateStartBounds();
        updateEndBounds();
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    protected final void callbackStartValue(final float newValue) {
        
//		Log.d(PhotoCompassApplication.LOG_TAG,
//				"DoubleSeekBar.callbackStartValue(" + newValue + ")");
        callback.onMinValueChange(tryStartValue(newValue));
    }
    
    protected final void callbackEndValue(final float newValue) {
        
//		Log.d(PhotoCompassApplication.LOG_TAG,
//				"DoubleSeekBar.callbackEndValue(" + newValue + ")");
        callback.onMaxValueChange(tryEndValue(newValue));
    }
    
    
    /**
     * @param newValue
     */
    public final void updateStartValue(final float newValue) {
        
//		Log.d(PhotoCompassApplication.LOG_TAG,
//				"DoubleSeekBar.updateStartValue() to " + newValue);
        setStartValue(newValue);
        startLabel = callback.getMinLabel();
        updateStartBounds();
    }
    
    
    /**
     * @param newValue
     */
    public final void updateEndValue(final float newValue) {
        
//		Log.d(PhotoCompassApplication.LOG_TAG,
//				"DoubleSeekBar.updateEndValue() to " + newValue);
        setEndValue(newValue);
        endLabel = callback.getMaxLabel();
        updateEndBounds();
    }
    
    protected abstract void updateStartBounds();
    
    protected abstract void updateEndBounds();
    
    @Override
    public final boolean onTouchEvent(final MotionEvent event) {
        
        // TODO check GestureDetector
        final int action = event.getAction();
        // Log.d(PhotoCompassApplication.LOG_TAG,
        // "DoubleSeekBar: onTouchEvent: action = "+action);
        final float touchX = event.getX();
        final float touchY = event.getY();
        final float newValue = convertToAbstract(getEventCoordinate(event));
        if (action == MotionEvent.ACTION_DOWN) {
            // ignore if distance to bar larger than tolerance constant
            if ((backgroundRect.left - touchX) > TOUCH_DOWN_TOLERANCE ||
                    (touchX - backgroundRect.right) > TOUCH_DOWN_TOLERANCE ||
                    (backgroundRect.top - touchY) > TOUCH_DOWN_TOLERANCE ||
                    (touchY - backgroundRect.bottom) > TOUCH_DOWN_TOLERANCE) {
                thumbDown = NONE;
                return false;
            }
            // determine whether left or right thumb concerned
            if (Math.abs(newValue - startValue) < Math.abs(newValue - endValue)) {
                // distance to start is less than distance to end
                thumbDown = START;
                startThumb = startThumbActive;
                callbackStartValue(newValue);
            } else {
                // distance to end is less than to start
                thumbDown = END;
                endThumb = endThumbActive;
                callbackEndValue(newValue);
            }
            this.invalidate(); // TODO determine "dirty" region
        } else if (action == MotionEvent.ACTION_MOVE && thumbDown != NONE) {
            if (thumbDown == START && ((Math.abs(startValue - newValue) * size) > DoubleSeekBar.TOUCH_MOVE_TOLERANCE)) {
                callbackStartValue(newValue);
                this.invalidate();
            } else if (thumbDown == END && (Math.abs(endValue - newValue) * size) > DoubleSeekBar.TOUCH_MOVE_TOLERANCE) {
                callbackEndValue(newValue);
                this.invalidate();
            }
        } else {
            if (action == MotionEvent.ACTION_UP && thumbDown != NONE) {
                if (thumbDown == START) {
                    startThumb = startThumbNormal;
                    callbackStartValue(newValue);
                } else {
                    endThumb = endThumbNormal;
                    callbackEndValue(newValue);
                }
                thumbDown = NONE;
                this.invalidate();
            } else
                Log.w(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar: Unexpected TouchEvent, action " + action);
            
            // sleep to avoid event flooding
            try {
                // Log.d(PhotoCompassApplication.LOG_TAG,
                // "DoubleSeekBar: sleep");
                Thread.sleep(PhotoCompassApplication.SLEEP_AFTER_TOUCH_EVENT);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    
    /**
     * @return the start value (left slider thumb), as a float from the range [0,1].
     */
    public final float getStartValue() {
        
        return startValue;
    }
    
    /**
     * @return the end value (right slider thumb), as a float from the range [0,1].
     */
    public final float getEndValue() {
        
        return endValue;
    }
    
    protected final float setStartValue(final float newValue) {
        
        return startValue = tryStartValue(newValue);
    }
    
    protected final float setEndValue(final float newValue) {
        
        return endValue = tryEndValue(newValue);
    }
    
    private final float tryStartValue(final float newValue) {
        
        return Math.max(0f, Math.min(newValue, endValue - minOffset));
    }
    
    private final float tryEndValue(final float newValue) {
        
        return Math.min(1f, Math.max(newValue, startValue + minOffset));
    }
    
    protected abstract float getEventCoordinate(final MotionEvent event);
    
    protected abstract int convertToConcrete(final float abstractValue);
    
    protected abstract float convertToAbstract(final float concreteValue);
    
    
    /**
     * @param callback
     */
    public final void setCallback(final IDoubleSeekBarCallback callback) {
        
        this.callback = callback;
    }
    
    
    /**
     * @param photoMarks
     */
    public final void setPhotoMarks(final float[] photoMarks) {
        
        _photoMarks = photoMarks;
    }
}
