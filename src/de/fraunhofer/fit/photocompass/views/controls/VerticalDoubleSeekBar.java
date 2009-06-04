package de.fraunhofer.fit.photocompass.views.controls;

import de.fraunhofer.fit.photocompass.R;
import android.content.Context;
import android.graphics.RectF;
import android.view.MotionEvent;

public class VerticalDoubleSeekBar extends DoubleSeekBar {

	public VerticalDoubleSeekBar(Context context) {
		super(context);
		this.startThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal_vertical);
		this.endThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal_vertical);
		this.halfAThumb = this.startThumb.getIntrinsicHeight() / 2;
		this.selectionRect.left = this.barPadding;
		this.selectionRect.right = this.barThickness + this.barPadding;

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		this.size = h - this.startOffset - this.endOffset;
		this.backgroundRect = new RectF(barPadding, 0f, barThickness
				+ barPadding, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void updateStartBounds() {
		int begin = convertToConcrete(startValue) - halfAThumb;

		this.startThumb.setBounds(0, begin, this.startThumb
				.getIntrinsicHeight(), begin
				+ this.startThumb.getIntrinsicWidth());
		this.selectionRect.top = begin + halfAThumb;
	}

	@Override
	protected void updateEndBounds() {
		int begin = convertToConcrete(endValue) - halfAThumb;

		this.endThumb.setBounds(0, begin, this.startThumb.getIntrinsicHeight(),
				begin + this.startThumb.getIntrinsicWidth());
		this.selectionRect.bottom = begin + halfAThumb;
	}

	@Override
	protected int convertToConcrete(float abstractValue) {

		return Math.round((1 - abstractValue) * this.size) + this.endOffset;

	}
	@Override
	protected float convertToAbstract(float concreteValue) {

			return 1 - (float) (concreteValue - this.endOffset) / this.size;

	}
	
	@Override
	protected float getEventCoordinate(MotionEvent event) {
		return event.getX();
	}

	@Override
	protected void setEndThumbActive() {

		this.endThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_pressed_vertical);
	}

	@Override
	protected void setEndThumbNormal() {

		this.endThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal_vertical);
	}

	@Override
	protected void setStartThumbActive() {
		this.startThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_pressed_vertical);
	}

	@Override
	protected void setStartThumbNormal() {

		this.startThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal_vertical);
	}
	
}
