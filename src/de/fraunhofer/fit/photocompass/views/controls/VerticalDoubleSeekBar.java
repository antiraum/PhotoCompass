package de.fraunhofer.fit.photocompass.views.controls;

import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.MotionEvent;

public class VerticalDoubleSeekBar extends DoubleSeekBar {

	public VerticalDoubleSeekBar(final Context context) {
		super(context);
		Resources res = this.getResources();
		this.startThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal_vertical);
		this.startThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed_vertical);
		this.endThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal_vertical);
		this.endThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed_vertical);
		this.startThumb = this.startThumbNormal;
		this.endThumb = this.endThumbNormal;
		this.halfAThumb = this.startThumb.getIntrinsicHeight() / 2;
		this.initialize();
		this.selectionRect.left = this.barPadding;
		this.selectionRect.right = this.barThickness + this.barPadding;
		this.paint.setTextAlign(Align.LEFT);
		
		this.startValue = this.model.getRelativeMinDistance();
		this.startLabel = this.model.getFormattedMinDistance();
		this.endValue = this.model.getRelativeMaxDistance();
		this.endLabel = this.model.getFormattedMaxDistance();
		
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		this.size = h - this.startOffset - this.endOffset;
		this.backgroundRect = new RectF(barPadding, 0f, barThickness
				+ barPadding, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void updateStartValue() {
		this.model.setRelativeMinDistance(this.startValue);
		this.startLabelX = this.barThickness + 3 * this.barPadding;
		this.startLabelY = 0;
		this.startLabel = this.model.getFormattedMinDistance();
		
		int begin = convertToConcrete(startValue) - halfAThumb;

		this.startThumb.setBounds(0, begin, this.startThumb
				.getIntrinsicHeight(), begin
				+ this.startThumb.getIntrinsicWidth());
		this.selectionRect.top = begin + halfAThumb;

	}

	@Override
	protected void updateEndValue() {
		this.model.setRelativeMaxDistance(this.endValue);
		this.endLabelX = this.barThickness + 3 * this.barPadding;
		this.endLabelY = this.size;
		this.endLabel = this.model.getFormattedMaxDistance();

		
		int begin = convertToConcrete(endValue) - halfAThumb;

		this.endThumb.setBounds(0, begin, this.startThumb.getIntrinsicHeight(),
				begin + this.startThumb.getIntrinsicWidth());
		this.selectionRect.bottom = begin + halfAThumb;

	}

	@Override
	protected int convertToConcrete(final float abstractValue) {

		return Math.round((1 - abstractValue) * this.size) + this.endOffset;

	}
	@Override
	protected float convertToAbstract(final float concreteValue) {
			return 1 - (float) (concreteValue - this.endOffset) / this.size;

	}
	
	@Override
	protected float getEventCoordinate(final MotionEvent event) {
		return event.getY();
	}

}
