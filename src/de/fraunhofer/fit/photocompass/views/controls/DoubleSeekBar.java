package de.fraunhofer.fit.photocompass.views.controls;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;

public abstract class DoubleSeekBar extends View {

	/**
	 * Tolerance in pixels. Only MOVE events above this tolerance will be taken
	 * into account.
	 */
	private final static float TOUCH_TOLERANCE = 1f;

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
	protected Rect selectionRect;
	protected int halfAThumb = -1;

	protected int startLabelX = 0;
	protected int startLabelY = 0;
	protected int endLabelX = 0;
	protected int endLabelY = 0;
	protected String startLabel;
	protected String endLabel;
	
	private float touchX = -5f;
	private float touchY = -5f;

	private boolean startThumbDown = false;

	protected final Paint paint = new Paint();

	protected ApplicationModel model;

	public DoubleSeekBar(final Context context) {
		super(context);

		this.model = ApplicationModel.getInstance();

		this.selectionRect = new Rect();
		this.paint.setStyle(Style.FILL);
		this.paint.setAntiAlias(true);

		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar initialized");

	}

	protected void initialize() {
		this.startOffset = this.halfAThumb;
		this.endOffset = this.halfAThumb;
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		// this.updateAllBounds();

		super.onDraw(canvas);
		paint.setColor(Color.GRAY);
		canvas.drawRoundRect(this.backgroundRect, 5f, 5f, paint);
		paint.setColor(PhotoCompassApplication.ORANGE);
		canvas.drawRect(this.selectionRect, paint);

		startThumb.draw(canvas);
		endThumb.draw(canvas);

		paint.setColor(Color.WHITE);
		canvas.drawText(this.startLabel, this.startLabelX, this.startLabelY,
				paint);
		canvas.drawText(this.endLabel, this.endLabelX, this.endLabelY, this.paint);
		
		paint.setColor(Color.RED);
		canvas.drawCircle(this.touchX, this.touchY, 4, this.paint);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar.onSizeChanged()");

		this.updateStartBounds();
		this.updateEndBounds();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onFocusChanged(final boolean gainFocus, final int direction,
			final Rect previouslyFocusedRect) {
		Log
				.d(PhotoCompassApplication.LOG_TAG,
						"DoubleSeekBar.onFocusChanged()");
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	protected abstract void updateStartValue(float newValue);

	protected abstract void updateEndValue(float NewValue);

	protected abstract void updateStartBounds();

	protected abstract void updateEndBounds();

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		// TODO check GestureDetector
		this.touchX = event.getX();
		this.touchY = event.getY();
		float newValue = convertToAbstract(getEventCoordinate(event));
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// determine whether left or right thumb concerned
			if (Math.abs(newValue - this.startValue) < Math.abs(newValue
					- this.endValue)) {
				// distance to start is less than distance to end
				this.startThumbDown = true;
				this.startThumb = this.startThumbActive;
				this.updateStartValue(newValue);
			} else {
				// distance to end is less than to start
				this.startThumbDown = false;
				this.endThumb = this.endThumbActive;
				this.updateEndValue(newValue);
			}
			this.invalidate(); // TODO determine "dirty" region
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (this.startThumbDown
					&& ((Math.abs(this.startValue - newValue) * this.size) > DoubleSeekBar.TOUCH_TOLERANCE)) {
				this.updateStartValue(newValue);
				this.invalidate();
			} else if (!this.startThumbDown
					&& (Math.abs(this.endValue - newValue) * this.size) > DoubleSeekBar.TOUCH_TOLERANCE) {
				this.updateEndValue(newValue);
				this.invalidate();
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (this.startThumbDown) {
				this.startThumb = this.startThumbNormal;
				this.updateStartValue(newValue);
			} else {
				this.endThumb = this.endThumbNormal;
				this.updateEndValue(newValue);
			}
			this.invalidate();
		} else {
			Log.w(PhotoCompassApplication.LOG_TAG,
					"DoubleSeekBar: Unexpected TouchEvent, action "
							+ event.getAction());
		}

		return true;
	}

	/**
	 * @return the start value (left slider thumb), as a float from the range
	 *         [0,1].
	 */
	public float getStartValue() {
		return this.startValue;
	}

	/**
	 * @return the end value (right slider thumb), as a float from the range
	 *         [0,1].
	 */
	public float getEndValue() {
		return this.endValue;
	}

	public float setStartValue(float newValue) {
		return this.startValue = Math
				.max(0f, Math.min(newValue, this.endValue));
	}

	public float setEndValue(float newValue) {
		return this.endValue = Math
				.min(1f, Math.max(newValue, this.startValue));
	}

	protected abstract float getEventCoordinate(final MotionEvent event);

	protected abstract int convertToConcrete(final float abstractValue);

	protected abstract float convertToAbstract(final float concreteValue);

}
