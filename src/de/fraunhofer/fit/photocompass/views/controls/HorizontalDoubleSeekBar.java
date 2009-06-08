package de.fraunhofer.fit.photocompass.views.controls;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.MotionEvent;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;

public final class HorizontalDoubleSeekBar extends DoubleSeekBar {
	public HorizontalDoubleSeekBar(final Context context) {
		super(context);
		Resources res = this.getResources();
		this.startThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal);
		this.startThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed);
		this.endThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal);
		this.endThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed);
		this.startThumb = this.startThumbNormal;
		this.endThumb = this.endThumbNormal;
		this.halfAThumb = this.startThumb.getIntrinsicWidth() / 2;
		this.initialize();
		this.selectionRect.top = this.barPadding;
		this.selectionRect.bottom = this.barThickness + this.barPadding;
		this.paint.setTextAlign(Align.CENTER);

		this.startValue = this.model.getRelativeMinAge();
		this.startLabel = this.model.getFormattedMinAge();
		this.endValue = this.model.getRelativeMaxAge();
		this.endLabel = this.model.getFormattedMaxAge();
	
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
	protected void updateStartValue() {
		
		this.model.setRelativeMinDistance(this.startValue);
		this.startLabelX = 50;
		this.startLabelY = 0;
		this.startLabel = this.model.getFormattedMinDistance();
		
		int begin = convertToConcrete(startValue) - halfAThumb;
		this.startThumb.setBounds(begin, 0, begin
				+ this.startThumb.getIntrinsicHeight(), this.startThumb
				.getIntrinsicHeight());
		this.selectionRect.left = begin + halfAThumb;


	}

	@Override
	protected void updateEndValue() {
		this.model.setRelativeMaxAge(this.endValue);
		this.endLabelX = this.size - 50;
		this.endLabelY = 0;
		this.endLabel = this.model.getFormattedMaxAge();
		
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
		return event.getX();
	}

}
