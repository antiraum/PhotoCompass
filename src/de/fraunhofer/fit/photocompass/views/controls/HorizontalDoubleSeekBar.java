package de.fraunhofer.fit.photocompass.views.controls;

import android.content.Context;
import android.graphics.RectF;
import android.view.MotionEvent;
import de.fraunhofer.fit.photocompass.R;

public final class HorizontalDoubleSeekBar extends DoubleSeekBar {
	public HorizontalDoubleSeekBar(final Context context) {
		super(context);
		this.startThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal);
		this.endThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal);
		this.halfAThumb = this.startThumb.getIntrinsicWidth() / 2;
		this.initialize();
		this.selectionRect.top = this.barPadding;
		this.selectionRect.bottom = this.barThickness + this.barPadding;
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		this.size = w - this.startOffset - this.endOffset;
		this.backgroundRect = new RectF(0f, barPadding, w, barThickness
				+ barPadding);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void updateStartBounds() {
		int begin = convertToConcrete(startValue) - halfAThumb;
		this.startThumb.setBounds(begin, 0, begin
				+ this.startThumb.getIntrinsicHeight(), this.startThumb
				.getIntrinsicHeight());
		this.selectionRect.left = begin + halfAThumb;

	}

	@Override
	protected void updateEndBounds() {
		int begin = convertToConcrete(endValue) - halfAThumb;
		this.endThumb.setBounds(begin, 0, begin
				+ this.startThumb.getIntrinsicHeight(), this.startThumb
				.getIntrinsicHeight());
		this.selectionRect.right = begin + halfAThumb;

	}

	@Override
	protected int convertToConcrete(final float abstractValue) {

		return Math.round(abstractValue * this.size) + this.startOffset;

	}

	@Override
	protected float convertToAbstract(final float concreteValue) {
		return (float) (concreteValue - this.startOffset) / this.size;

	}

	@Override
	protected float getEventCoordinate(final MotionEvent event) {
		return event.getY();
	}

	@Override
	protected void setEndThumbActive() {
		this.endThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_pressed);
	}

	@Override
	protected void setEndThumbNormal() {

		this.endThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal);
	}

	@Override
	protected void setStartThumbActive() {
		this.startThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_pressed);
	}

	@Override
	protected void setStartThumbNormal() {

		this.startThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal);
	}

}
