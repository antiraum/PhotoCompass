package de.fraunhofer.fit.photocompass.views.controls;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.view.MotionEvent;
import de.fraunhofer.fit.photocompass.R;

public final class HorizontalDoubleSeekBar extends DoubleSeekBar {
	private final int topPadding = 13;

	public HorizontalDoubleSeekBar(final Context context, final IDoubleSeekBarCallback callback) {
		super(context, callback);
		Resources res = this.getResources();
		this.startThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal);
		this.startThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed);
		this.endThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal);
		this.endThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed);
		this.startThumb = this.startThumbNormal;
		this.endThumb = this.endThumbNormal;
		this.halfAThumb = this.startThumb.getIntrinsicWidth() / 2;
		this.initialize();
		this.selectionRect.top = this.barPadding + this.topPadding;
		this.selectionRect.bottom = this.barThickness + this.barPadding
				+ this.topPadding;
		this.paint.setTextAlign(Align.CENTER);
		
		this.startLabelY = 9;
		this.endLabelY = 9;
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
			final int oldh) {
		this.size = w - this.startOffset - this.endOffset;
		this.backgroundRect = new RectF(0f, topPadding + barPadding, w,
				barThickness + barPadding + topPadding);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	protected void updateStartBounds() {
		int begin = convertToConcrete(this.getStartValue()) - halfAThumb;
		this.startThumb.setBounds(begin, topPadding, begin
				+ this.startThumb.getIntrinsicWidth(), this.startThumb
				.getIntrinsicHeight()
				+ topPadding);
		this.selectionRect.left = begin + halfAThumb;
		this.startLabelX = this.startThumb.getBounds().centerX();
	}
	
	protected void updateEndBounds() {
		int begin = convertToConcrete(this.getEndValue()) - halfAThumb;
		this.endThumb.setBounds(begin, topPadding, begin
				+ this.startThumb.getIntrinsicWidth(), this.startThumb
				.getIntrinsicHeight()
				+ topPadding);
		this.selectionRect.right = begin + halfAThumb;
		this.endLabelX = this.endThumb.getBounds().centerX();
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
		return event.getX();
	}

}
