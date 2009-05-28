package de.fraunhofer.fit.photocompass.views;

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
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;

public class DoubleSeekBar extends View {
	public final static int HORIZONTAL = 0;
	public final static int VERTICAL = 1;

	private final int barThickness = 22;
	private final int barPadding = 4;

	private int orientation;

	private float startValue = 0f;
	private float endValue = 1f;
	private int startOffset;
	private int endOffset;
	private int size;

	private Drawable startThumb;
	private Drawable endThumb;
	private Rect selectionRect;
	private int halfAThumb;

	public DoubleSeekBar(Context context, int orientation) {
		super(context);
		if (orientation != DoubleSeekBar.HORIZONTAL
				&& orientation != DoubleSeekBar.VERTICAL) {
			// throw new
			// IllegalArgumentException("Parameter orientation must be one of the static class members");
			orientation = DoubleSeekBar.HORIZONTAL;
		}
		this.orientation = orientation;
		if (orientation == HORIZONTAL) {
			this.startThumb = this.getResources().getDrawable(
					R.drawable.seek_thumb_normal);
			this.endThumb = this.getResources().getDrawable(
					R.drawable.seek_thumb_normal);
			this.halfAThumb = this.startThumb.getIntrinsicWidth() / 2;
		} else { // VERTICAL
			this.startThumb = this.getResources().getDrawable(
					R.drawable.seek_thumb_normal_vertical);
			this.endThumb = this.getResources().getDrawable(
					R.drawable.seek_thumb_normal_vertical);
			this.halfAThumb = this.startThumb.getIntrinsicHeight() / 2;
		}
		this.startOffset = this.halfAThumb;
		this.endOffset = this.halfAThumb;
		this.selectionRect = new Rect();
		if (orientation == HORIZONTAL) {
			this.selectionRect.top = this.barPadding;
			this.selectionRect.bottom = this.barThickness + this.barPadding;
		} else { // VERTICAL
			this.selectionRect.left = this.barPadding;
			this.selectionRect.right = this.barThickness + this.barPadding;
		}

		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar initialized");

	}

	@Override
	protected void onDraw(Canvas canvas) {
		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar.onDraw()");
		this.updateAllBounds();

		super.onDraw(canvas);
		Paint p = new Paint();
		p.setColor(Color.GRAY);
		p.setStyle(Style.FILL);
		if (this.orientation == HORIZONTAL) {
			canvas.drawRoundRect(new RectF(0f, barPadding, this.getWidth(),
					barThickness + barPadding), 5f, 5f, p);
		} else { // VERTICAL
			canvas.drawRoundRect(new RectF(barPadding, 0f, barThickness
					+ barPadding, this.getHeight()), 5f, 5f, p);
		}
		p.setColor(Color.YELLOW);
		canvas.drawRect(this.selectionRect, p);
		startThumb.draw(canvas);
		endThumb.draw(canvas);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d(PhotoCompassApplication.LOG_TAG, "DoubleSeekBar.onSizeChanged()");

		if (this.orientation == HORIZONTAL) {
			this.size = w - this.startOffset - this.endOffset;
		} else { // VERTICAL
			this.size = h - this.startOffset - this.endOffset;
		}
		this.updateAllBounds();
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,
			Rect previouslyFocusedRect) {
		Log
				.d(PhotoCompassApplication.LOG_TAG,
						"DoubleSeekBar.onFocusChanged()");
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	private void updateAllBounds() {
		this.updateStartBounds();
		this.updateEndBounds();
	}

	private void updateStartBounds() {
		int begin = convertToConcrete(startValue) - halfAThumb;
		if (orientation == HORIZONTAL) {
			this.startThumb.setBounds(begin, 0, begin
					+ this.startThumb.getIntrinsicHeight(), this.startThumb
					.getIntrinsicHeight());
			this.selectionRect.left = begin + halfAThumb;
		} else { // VERTICAL
			this.startThumb.setBounds(0, begin, this.startThumb
					.getIntrinsicHeight(), begin
					+ this.startThumb.getIntrinsicWidth());
			this.selectionRect.top = begin + halfAThumb;
		}
	}

	private void updateEndBounds() {
		int begin = convertToConcrete(endValue) - halfAThumb;
		if (orientation == HORIZONTAL) {
			this.endThumb.setBounds(begin, 0, begin
					+ this.startThumb.getIntrinsicHeight(), this.startThumb
					.getIntrinsicHeight());
			this.selectionRect.right = begin + halfAThumb;
		} else { // VERTICAL
			this.endThumb.setBounds(0, begin, this.startThumb
					.getIntrinsicHeight(), begin
					+ this.startThumb.getIntrinsicWidth());
			this.selectionRect.bottom = begin + halfAThumb;
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO check GestureDetector
		Log.d(PhotoCompassApplication.LOG_TAG, "MotionEvent action "
				+ event.getAction());
		float newValue;
		if (this.orientation == HORIZONTAL) {
			newValue = convertToAbstract(event.getX());
		} else { // VERTICAL
			newValue = convertToAbstract(event.getY());
		}

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// determine whether left or right thumb concerned
			if (Math.abs(newValue - this.startValue) < Math.abs(newValue
					- this.endValue)) {
				// distance to left is less than distance to right
				this.startValue = newValue;
				this.updateStartBounds();
			} else {
				// distance to right is less than to left
				this.endValue = newValue;
				this.updateEndBounds();
			}
			this.invalidate(); // TODO determine "dirty" region
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

	private int convertToConcrete(float abstractValue) {
		if (this.orientation == HORIZONTAL) {
			return Math.round(abstractValue * this.size) + this.startOffset;
		} else { // VERTICAL
			return Math.round((1 - abstractValue) * this.size) + this.endOffset;
		}
	}

	private float convertToAbstract(float concreteValue) {
		if (this.orientation == HORIZONTAL) {
			return (float) (concreteValue - this.startOffset) / this.size;
		} else { // VERTICAL
			return 1 - (float) (concreteValue - this.endOffset) / this.size;
		}
	}

}
