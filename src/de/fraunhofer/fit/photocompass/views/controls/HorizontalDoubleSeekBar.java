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
 * SeekBar (slider) control with two thumbs for horizontal display. Displays labels above the thumbs. Label values are
 * retrieved from the Callback.
 */
public final class HorizontalDoubleSeekBar extends DoubleSeekBar {
    
    private final int topPadding = 29;
    private static final int LABEL_PADDING = 5;
    
    /**
     * Creates a new HorizontalDoubleSeekBar using the application's Context and a custom callback that is used to
     * retrieve the labels as well as to notify the application model about changes.
     * 
     * @param context The application's context
     * @param callback The callback used for interaction with the model
     * @param lightBackground Whether the DoubleSeekBar is drawn on a light ( <code>true</code>) or a dark (
     *        <code>false</code>) background.
     */
    public HorizontalDoubleSeekBar(final Context context, final IDoubleSeekBarCallback callback,
                                   final boolean lightBackground) {

        super(context, callback, lightBackground);
        final Resources res = getResources();
        startThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal);
        startThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed);
        endThumbNormal = res.getDrawable(R.drawable.seek_thumb_normal);
        endThumbActive = res.getDrawable(R.drawable.seek_thumb_pressed);
        startThumb = startThumbNormal;
        endThumb = endThumbNormal;
        halfAThumb = startThumb.getIntrinsicWidth() / 2;
        initialize();
        selectionRect.top = barPadding + topPadding;
        selectionRect.bottom = barThickness + barPadding + topPadding;
        paint.setTextAlign(Align.CENTER);
        backgroundGradient = new LinearGradient(0, topPadding + barPadding, 0, topPadding + barPadding + barThickness /
                                                                               2, PhotoCompassApplication.GREY,
                                                PhotoCompassApplication.DARK_GREY, Shader.TileMode.MIRROR);
        selectionGradient = new LinearGradient(0, topPadding + barPadding, 0, topPadding + barPadding + barThickness /
                                                                              2, PhotoCompassApplication.ORANGE,
                                               PhotoCompassApplication.DARK_ORANGE, Shader.TileMode.MIRROR);
        
        startLabelY = topPadding - 4;
        endLabelY = topPadding - 4;
    }
    
    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {

        size = w - startOffset - endOffset;
        backgroundRect = new RectF(0f, topPadding + barPadding, w, barThickness + barPadding + topPadding);
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
    @Override
    protected void updateStartBounds() {

        final int begin = convertToConcrete(getStartValue()) - halfAThumb;
        startThumb.setBounds(begin, topPadding, begin + startThumb.getIntrinsicWidth(),
                             startThumb.getIntrinsicHeight() + topPadding);
        selectionRect.left = begin + halfAThumb;
        startLabelX = startThumb.getBounds().centerX();
    }
    
    @Override
    protected void updateEndBounds() {

        final int begin = convertToConcrete(getEndValue()) - halfAThumb;
        endThumb.setBounds(begin, topPadding, begin + startThumb.getIntrinsicWidth(), startThumb.getIntrinsicHeight() +
                                                                                      topPadding);
        selectionRect.right = begin + halfAThumb;
        endLabelX = endThumb.getBounds().centerX();
    }
    
    @Override
    protected int convertToConcrete(final float abstractValue) {

        return Math.round(abstractValue * size) + startOffset;
        
    }
    
    @Override
    protected float convertToAbstract(final float concreteValue) {

        return (concreteValue - startOffset) / size;
        
    }
    
    @Override
    protected float getEventCoordinate(final MotionEvent event) {

        return event.getX();
    }
    
    @Override
    protected void drawPhotoMarks(final Canvas canvas) {

        float pos;
        for (final float mark : _photoMarks) {
            pos = backgroundRect.left + startOffset + mark * size;
            canvas.drawLine(pos, backgroundRect.top, pos, backgroundRect.bottom, paint);
        }
    }
    
    @Override
    protected void drawLabels(final Canvas canvas) {

        // float slWidth2 = this.paint.measureText(this.startLabel) / 2;
        // float elWidth2 = this.paint.measureText(this.endLabel) / 2;
        // float slX = this.startLabelX;
        // float elX = this.endLabelX;
        float slWidth2;
        float elWidth2;
        float slX = startLabelX;
        float elX = endLabelX;
        
        if (thumbDown == START) {
            paint.setTextSize(labelSizeHighlight);
            slWidth2 = paint.measureText(startLabel) / 2;
            
            paint.setTextSize(labelSize);
            elWidth2 = paint.measureText(endLabel) / 2;
        } else {
            slWidth2 = paint.measureText(startLabel) / 2;
            
            if (thumbDown == END) {
                paint.setTextSize(labelSizeHighlight);
                elWidth2 = paint.measureText(endLabel) / 2;
                paint.setTextSize(labelSize);
            } else
                elWidth2 = paint.measureText(endLabel) / 2;
        }
        
        if (2 * slWidth2 + 2 * elWidth2 + LABEL_PADDING <= getWidth()) {
            if ((elX - elWidth2) - (slX + slWidth2) < LABEL_PADDING) {
                final float offset = ((slWidth2 + elWidth2 + LABEL_PADDING) - (elX - slX)) / 2;
                float startOffset = offset;
                float endOffset = offset;
                if (offset > slWidth2 - (float) MINIMUM_THUMB_OFFSET / 2 + (float) LABEL_PADDING / 2) {
                    startOffset = slWidth2 - (float) MINIMUM_THUMB_OFFSET / 2 + (float) LABEL_PADDING / 2;
                    endOffset = offset + (offset - startOffset);
                } else if (offset > elWidth2 - (float) MINIMUM_THUMB_OFFSET / 2 + (float) LABEL_PADDING / 2) {
                    endOffset = elWidth2 - (float) MINIMUM_THUMB_OFFSET / 2 + (float) LABEL_PADDING / 2;
                    startOffset = offset + (offset - endOffset);
                }
                slX -= startOffset;
                elX += endOffset;
            }
            if (slX - slWidth2 < 0) {
                slX = slWidth2;
                elX = Math.max(elX, slX + slWidth2 + elWidth2 + LABEL_PADDING);
            }
            if (elX + elWidth2 > getWidth()) {
                elX = getWidth() - elWidth2;
                slX = Math.min(slX, elX - elWidth2 - slWidth2 - LABEL_PADDING);
            }
        } else {
            // TODO Labels too big for screen - should not really happen...
        }
        
        if (thumbDown == START) {
            paint.setTextSize(labelSizeHighlight);
            canvas.drawText(startLabel, slX, startLabelY - (labelSizeHighlight - labelSize) / 2, paint);
            paint.setTextSize(labelSize);
            canvas.drawText(endLabel, elX, endLabelY, paint);
        } else {
            canvas.drawText(startLabel, slX, startLabelY, paint);
            if (thumbDown == END) {
                paint.setTextSize(labelSizeHighlight);
                canvas.drawText(endLabel, elX, endLabelY - (labelSizeHighlight - labelSize) / 2, paint);
                paint.setTextSize(labelSize);
            } else
                canvas.drawText(endLabel, elX, endLabelY, paint);
        }
        
    }
    
}
