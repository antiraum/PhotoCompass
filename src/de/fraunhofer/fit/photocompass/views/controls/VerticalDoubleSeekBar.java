package de.fraunhofer.fit.photocompass.views.controls;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Paint.Align;
import android.view.MotionEvent;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;

/**
 * SeekBar (slider) control with two thumbs for vertical display. Displays labels above the thumbs. Label values are
 * retrieved from the Callback.
 */
public final class VerticalDoubleSeekBar extends DoubleSeekBar {
    
    /**
     * Creates a new VerticalDoubleSeekBar using the application's Context and a custom callback that is used to
     * retrieve the labels as well as to notify the application model about changes.
     * 
     * @param context The application's context
     * @param callback The callback used for interaction with the model
     * @param lightBackground Whether the DoubleSeekBar is drawn on a light ( <code>true</code>) or a dark (
     *        <code>false</code>) background
     */
    public VerticalDoubleSeekBar(final Context context, final IDoubleSeekBarCallback callback,
                                 final boolean lightBackground) {

        super(context, callback, lightBackground);
        final Resources res = getResources();
        startThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal_vertical);
        startThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed_vertical);
        endThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal_vertical);
        endThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed_vertical);
        startThumb = startThumbNormal;
        endThumb = endThumbNormal;
        halfAThumb = startThumb.getIntrinsicHeight() / 2;
        initialize();
        selectionRect.left = barPadding;
        selectionRect.right = barThickness + barPadding;
        paint.setTextAlign(Align.LEFT);
        backgroundGradient = new LinearGradient(barPadding, 0, barPadding + barThickness / 2, 0,
                                                PhotoCompassApplication.GREY, PhotoCompassApplication.DARK_GREY,
                                                Shader.TileMode.MIRROR);
        selectionGradient = new LinearGradient(barPadding, 0, barPadding + barThickness / 2, 0,
                                               PhotoCompassApplication.ORANGE, PhotoCompassApplication.DARK_ORANGE,
                                               Shader.TileMode.MIRROR);
        
        // this.setStartValue(this.model.getRelativeMinDistance());
        // this.startLabel = this.model.getFormattedMinDistance();
        // this.setEndValue(this.model.getRelativeMaxDistance());
        // this.endLabel = this.model.getFormattedMaxDistance();
        startLabelX = barThickness + 3 * barPadding;
        endLabelX = barThickness + 3 * barPadding;
        
    }
    
    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {

        size = h - startOffset - endOffset;
        backgroundRect = new RectF(barPadding, 0f, barThickness + barPadding, h);
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    @Override
    protected void updateStartBounds() {

        final int begin = convertToConcrete(getStartValue()) - halfAThumb;
        startThumb.setBounds(0, begin, startThumb.getIntrinsicWidth(), begin + startThumb.getIntrinsicHeight());
        selectionRect.bottom = begin + halfAThumb;
        startLabelY = startThumb.getBounds().centerY() + 4;
    }
    
    @Override
    protected void updateEndBounds() {

        final int begin = convertToConcrete(getEndValue()) - halfAThumb;
        
        selectionRect.top = begin + halfAThumb;
        endThumb.setBounds(0, begin, startThumb.getIntrinsicWidth(), begin + startThumb.getIntrinsicHeight());
        endLabelY = endThumb.getBounds().centerY() + 4;
    }
    
    @Override
    protected int convertToConcrete(final float abstractValue) {

        return Math.round((1 - abstractValue) * size) + endOffset;
        
    }
    
    @Override
    protected float convertToAbstract(final float concreteValue) {

        return 1 - (concreteValue - endOffset) / size;
        
    }
    
    @Override
    protected float getEventCoordinate(final MotionEvent event) {

        return event.getY();
    }
    
    @Override
    protected void drawPhotoMarks(final Canvas canvas) {

        float pos;
        for (final float mark : _photoMarks) {
            pos = backgroundRect.top + backgroundRect.height() - endOffset - mark * size;
            canvas.drawLine(backgroundRect.left, pos, backgroundRect.right, pos, paint);
        }
    }
    
    @Override
    protected void drawLabels(final Canvas canvas) {

        if (thumbDown == START) {
            paint.setTextSize(labelSizeHighlight);
            canvas.drawText(startLabel, startLabelX, startLabelY + (labelSizeHighlight - labelSize) / 2, paint);
            paint.setTextSize(labelSize);
            canvas.drawText(endLabel, endLabelX, endLabelY, paint);
        } else {
            canvas.drawText(startLabel, startLabelX, startLabelY, paint);
            if (thumbDown == END) {
                paint.setTextSize(labelSizeHighlight);
                canvas.drawText(endLabel, endLabelX, endLabelY + (labelSizeHighlight - labelSize) / 2, paint);
                paint.setTextSize(labelSize);
            } else
                canvas.drawText(endLabel, endLabelX, endLabelY, paint);
        }
        
    }
    
}
