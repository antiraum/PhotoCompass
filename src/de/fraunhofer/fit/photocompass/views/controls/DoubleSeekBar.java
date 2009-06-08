package de.fraunhofer.fit.photocompass.views.controls;

import java.util.Formatter;

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
import android.widget.TextView;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import de.fraunhofer.fit.photocompass.model.ApplicationModel;

public abstract class DoubleSeekBar extends View {
	// public final static int HORIZONTAL = 0;
	// public final static int VERTICAL = 1;

	protected int barThickness = 22;
	protected int barPadding = 4;

	// private int orientation;

	protected float startValue = 0f;
	protected float endValue = 1f;
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
//		this.updateAllBounds();

		super.onDraw(canvas);
		paint.setColor(Color.GRAY);
		canvas.drawRoundRect(this.backgroundRect, 5f, 5f, paint);
		paint.setColor(PhotoCompassApplication.ORANGE);
		canvas.drawRect(this.selectionRect, paint);
		Log.d(PhotoCompassApplication.LOG_TAG,
				"DoubleSeekBar.onDraw(): selectionRect " + this.selectionRect.toString());

		
		startThumb.draw(canvas);
		endThumb.draw(canvas);
		Log.d(PhotoCompassApplication.LOG_TAG,
				"DoubleSeekBar.onDraw(): startValue " + this.startValue
						+ ", endValue " + this.endValue);

		paint.setColor(Color.WHITE);
		canvas.drawText(this.startLabel, this.startLabelX, this.startLabelY,
				paint);
		canvas.drawText(this.endLabel, this.endLabelX, this.endLabelY, paint);
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
		Log.d(PhotoCompassApplication.LOG_TAG,
				"DoubleSeekBar.onTouchEvent(): MotionEvent action "
						+ event.getAction());
		float newValue = convertToAbstract(getEventCoordinate(event));
		Log.d(PhotoCompassApplication.LOG_TAG,
				"DoubleSeekBar.onTouchEvent(): Got new value " + newValue);
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// determine whether left or right thumb concerned
			if (Math.abs(newValue - this.startValue) < Math.abs(newValue
					- this.endValue)) {
				// distance to left is less than distance to right
				this.startThumbDown = true;
//				this.startValue = newValue;
				this.startThumb = this.startThumbActive;
				this.updateStartValue(newValue);
			} else {
				// distance to right is less than to left
				this.startThumbDown = false;
//				this.endValue = newValue;
				this.endThumb = this.endThumbActive;
				this.updateEndValue(newValue);
			}
			this.invalidate(); // TODO determine "dirty" region
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (this.startThumbDown) {
//				this.startValue = newValue;
				this.updateStartValue(newValue);
			} else {
//				this.endValue = newValue;
				this.updateEndValue(newValue);
			}
			this.invalidate();
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (this.startThumbDown) {
//				this.startValue = newValue;
				this.startThumb = this.startThumbNormal;
				this.updateStartValue(newValue);
			} else {
//				this.endValue = newValue;
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

	protected abstract float getEventCoordinate(final MotionEvent event);

	protected abstract int convertToConcrete(final float abstractValue);

	protected abstract float convertToAbstract(final float concreteValue);

}
