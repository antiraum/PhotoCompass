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
import de.fraunhofer.fit.photocompass.R;

public abstract class DoubleSeekBar extends View {
//	public final static int HORIZONTAL = 0;
//	public final static int VERTICAL = 1;

	protected final int barThickness = 22;
	protected final int barPadding = 4;

//	private int orientation;

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

	private boolean startThumbDown = false;

	public DoubleSeekBar(final Context context) {
		super(context);
//		if (orientation != DoubleSeekBar.HORIZONTAL
//				&& orientation != DoubleSeekBar.VERTICAL) {
//			// throw new
//			// IllegalArgumentException("Parameter orientation must be one of the static class members");
//			orientation = DoubleSeekBar.HORIZONTAL;
//		}
//		this.orientation = orientation;
//		if (orientation == HORIZONTAL) {

//		} else { // VERTICAL
//		}
		this.selectionRect = new Rect();
//		if (orientation == HORIZONTAL) {
//		} else { // VERTICAL
//		}

		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar initialized");

	}

	protected void initialize() {
		this.startOffset = this.halfAThumb;
		this.endOffset = this.halfAThumb;
	}
	
	@Override
	protected void onDraw(final Canvas canvas) {
		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar.onDraw()");
		this.updateAllBounds();

		super.onDraw(canvas);
		Paint p = new Paint();
		p.setColor(Color.GRAY);
		p.setStyle(Style.FILL);
		canvas.drawRoundRect(this.backgroundRect, 5f, 5f, p);
		p.setColor(Color.YELLOW);
		canvas.drawRect(this.selectionRect, p);
		startThumb.draw(canvas);
		endThumb.draw(canvas);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar.onSizeChanged()");

		this.updateAllBounds();
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

	private void updateAllBounds() {
		this.updateStartBounds();
		this.updateEndBounds();
	}

	protected abstract void updateStartBounds(); 

	protected abstract void updateEndBounds();

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		// TODO check GestureDetector
		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar.onTouchEvent(): MotionEvent action "
				+ event.getAction());
		float newValue = convertToAbstract(getEventCoordinate(event));
		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar.onTouchEvent(): Got new value " + newValue);		
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// determine whether left or right thumb concerned
			if (Math.abs(newValue - this.startValue) < Math.abs(newValue
					- this.endValue)) {
				// distance to left is less than distance to right
				this.startThumbDown = true;
				this.startValue = newValue;
				this.startThumb = this.startThumbActive;
				this.updateStartBounds();
			} else {
				// distance to right is less than to left
				this.startThumbDown = false;
				this.endValue = newValue;
				this.endThumb = this.endThumbActive;
				this.updateEndBounds();
			}
			this.invalidate(); // TODO determine "dirty" region
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (this.startThumbDown) {
				this.startValue = newValue;
				this.updateStartBounds();
			} else {
				this.endValue = newValue;
				this.updateEndBounds();
			}
			this.invalidate();
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (this.startThumbDown) {
				this.startValue = newValue;
				this.startThumb = this.startThumbNormal;
				this.updateStartBounds();
			} else {
				this.endValue = newValue;
				this.endThumb = this.endThumbNormal;
				this.updateEndBounds();
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

//	protected abstract void setStartThumbActive();
//	protected abstract void setStartThumbNormal();
//	protected abstract void setEndThumbActive();
//	protected abstract void setEndThumbNormal();
//	
	protected abstract float getEventCoordinate(final MotionEvent event);
	
	protected abstract int convertToConcrete(final float abstractValue);

	protected abstract float convertToAbstract(final float concreteValue);
}
