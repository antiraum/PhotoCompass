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
 * Abstract SeekBar (slider) class that supports the selection of an interval. For this purpose,
 * two thumbs are displayed and draggable. For concrete implementations, see
 * {@link HorizontalDoubleSeekBar} or {@link VerticalDoubleSeekBar}.
 * 
 * @author joni
 *
 */
public abstract class DoubleSeekBar extends View {

	/**
	 * Tolerance for MOVE touch events in pixels. Only events above this
	 * tolerance will be taken into account.
	 */
	private final static float TOUCH_MOVE_TOLERANCE = 1f;

	/**
	 * Tolerance for DOWN touch events in pixels: Events with a larger distance
	 * to the bar will be ignored.
	 */
	private final static float TOUCH_DOWN_TOLERANCE = 15f;

	protected int barThickness = 22;
	protected int barPadding = 4;

	private float startValue = 0f;
	private float endValue = 1f;
	protected int startOffset;
	protected int endOffset;
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

	private final static int NONE = 0;
	private final static int START = 1;
	private final static int END = 2;

	private int thumbDown = NONE;

	protected final Paint paint = new Paint();
	protected LinearGradient backgroundGradient;
	protected LinearGradient selectionGradient;

	protected IDoubleSeekBarCallback callback;
	
	private boolean _lightBackground = false;

	public DoubleSeekBar(final Context context, IDoubleSeekBarCallback callback, boolean lightBackground) {
		super(context);
		this.callback = callback;
		_lightBackground = lightBackground;

		this.setStartValue(callback.getMinValue());
		this.startLabel = callback.getMinLabel();

		this.setEndValue(callback.getMaxValue());
		this.endLabel = callback.getMaxLabel();

		this.paint.setStyle(Style.FILL);
		this.paint.setAntiAlias(true);

		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar initialized");
	}

	protected final void initialize() {
		this.startOffset = this.halfAThumb;
		this.endOffset = this.halfAThumb;
	}

	@Override
	protected final void onDraw(final Canvas canvas) {
		// this.updateAllBounds();

		super.onDraw(canvas);
//		paint.setColor(Color.GRAY);
		paint.setShader(backgroundGradient);
		canvas.drawRoundRect(this.backgroundRect, 5f, 5f, paint);
//		paint.setColor(PhotoCompassApplication.ORANGE);
		paint.setShader(selectionGradient);
		canvas.drawRect(this.selectionRect, paint);

		startThumb.draw(canvas);
		endThumb.draw(canvas);

		paint.setShader(null);
		paint.setColor(_lightBackground ? Color.DKGRAY : Color.WHITE);
		canvas.drawText(this.startLabel, this.startLabelX, this.startLabelY,
				paint);
		canvas.drawText(this.endLabel, this.endLabelX, this.endLabelY,
				this.paint);

//		paint.setColor(Color.RED);
		// canvas.drawCircle(this.touchX, this.touchY, 4, this.paint);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar.onSizeChanged()");

		this.updateStartBounds();
		this.updateEndBounds();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	protected final void updateStartValueWithCallback(float newValue) {
		this.callback.onMinValueChange(this.tryStartValue(newValue));
	}

	protected final void updateEndValueWithCallback(final float newValue) {
		this.callback.onMaxValueChange(this.tryEndValue(newValue));
	}

	public final void updateStartValue(float newValue) {
		Log.d(PhotoCompassApplication.LOG_TAG,
				"DoubleSeekBar.updateStartValue()");
		this.setStartValue(newValue);
		this.startLabel = this.callback.getMinLabel();
		this.updateStartBounds();
	}

	public final void updateEndValue(final float newValue) {
		Log
				.d(PhotoCompassApplication.LOG_TAG,
						"DoubleSeekBar.updateEndValue()");
		this.setEndValue(newValue);
		this.endLabel = this.callback.getMaxLabel();
		this.updateEndBounds();
	}

	protected abstract void updateStartBounds();

	protected abstract void updateEndBounds();

	@Override
	public final boolean onTouchEvent(final MotionEvent event) {
		// TODO check GestureDetector
		final int action = event.getAction();
//    	Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar: onTouchEvent: action = "+action);
		final float touchX = event.getX();
		final float touchY = event.getY();
		final float newValue = convertToAbstract(getEventCoordinate(event));
		if (action == MotionEvent.ACTION_DOWN) {
			// ignore if distance to bar larger than tolerance constant
			if ((this.backgroundRect.left - touchX) > TOUCH_DOWN_TOLERANCE
					|| (touchX - this.backgroundRect.right) > TOUCH_DOWN_TOLERANCE
					|| (this.backgroundRect.top - touchY) > TOUCH_DOWN_TOLERANCE
					|| (touchY - this.backgroundRect.bottom) > TOUCH_DOWN_TOLERANCE) {
				this.thumbDown = NONE;
				return false;
			}
			// determine whether left or right thumb concerned
			if (Math.abs(newValue - this.startValue) < Math.abs(newValue
					- this.endValue)) {
				// distance to start is less than distance to end
				this.thumbDown = START;
				this.startThumb = this.startThumbActive;
				this.updateStartValueWithCallback(newValue);
			} else {
				// distance to end is less than to start
				this.thumbDown = END;
				this.endThumb = this.endThumbActive;
				this.updateEndValueWithCallback(newValue);
			}
			this.invalidate(); // TODO determine "dirty" region
		} else if (action == MotionEvent.ACTION_MOVE
				&& this.thumbDown != NONE) {
			if (this.thumbDown == START
					&& ((Math.abs(this.startValue - newValue) * this.size) > DoubleSeekBar.TOUCH_MOVE_TOLERANCE)) {
				this.updateStartValueWithCallback(newValue);
				this.invalidate();
			} else if (this.thumbDown == END
					&& (Math.abs(this.endValue - newValue) * this.size) > DoubleSeekBar.TOUCH_MOVE_TOLERANCE) {
				this.updateEndValueWithCallback(newValue);
				this.invalidate();
			}
		} else {
			if (action == MotionEvent.ACTION_UP && this.thumbDown != NONE) {
				if (this.thumbDown == START) {
					this.startThumb = this.startThumbNormal;
					this.updateStartValueWithCallback(newValue);
				} else {
					this.endThumb = this.endThumbNormal;
					this.updateEndValueWithCallback(newValue);
				}
				this.thumbDown = NONE;
				this.invalidate();
			} else {
				Log.w(PhotoCompassApplication.LOG_TAG,
						"DoubleSeekBar: Unexpected TouchEvent, action "
								+ action);
			}
			
        	// sleep to avoid event flooding
        	try {
//				Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar: sleep");
    			Thread.sleep(PhotoCompassApplication.SLEEP_AFTER_TOUCH_EVENT);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
		}
		return true;
	}

	/**
	 * @return the start value (left slider thumb), as a float from the range
	 *         [0,1].
	 */
	public final float getStartValue() {
		return this.startValue;
	}

	/**
	 * @return the end value (right slider thumb), as a float from the range
	 *         [0,1].
	 */
	public final float getEndValue() {
		return this.endValue;
	}

	protected final float setStartValue(float newValue) {
		return this.startValue = this.tryStartValue(newValue);
	}

	protected final float setEndValue(float newValue) {
		return this.endValue = this.tryEndValue(newValue);
	}

	private final float tryStartValue(float newValue) {
		return Math.max(0f, Math.min(newValue, this.endValue));
	}

	private final float tryEndValue(float newValue) {
		return Math.min(1f, Math.max(newValue, this.startValue));
	}

	protected abstract float getEventCoordinate(final MotionEvent event);

	protected abstract int convertToConcrete(final float abstractValue);

	protected abstract float convertToAbstract(final float concreteValue);

	public final void setCallback(IDoubleSeekBarCallback callback) {
		this.callback = callback;
	}
}
