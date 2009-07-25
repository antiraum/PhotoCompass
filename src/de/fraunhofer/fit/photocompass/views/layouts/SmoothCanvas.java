package de.fraunhofer.fit.photocompass.views.layouts;

import javax.microedition.khronos.opengles.GL;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * Canvas for the {@link RotateView} that smooths the rotated bitmap. Taken from
 * com.example.android.apis.view.MapViewCompassDemo.
 */
public final class SmoothCanvas extends Canvas {
    
    Canvas delegate;
    
    private final Paint _smooth = new Paint(Paint.FILTER_BITMAP_FLAG);
    
    @Override
    public void setBitmap(final Bitmap bitmap) {

        delegate.setBitmap(bitmap);
    }
    
    @Override
    public void setViewport(final int width, final int height) {

        delegate.setViewport(width, height);
    }
    
    @Override
    public boolean isOpaque() {

        return delegate.isOpaque();
    }
    
    @Override
    public int getWidth() {

        return delegate.getWidth();
    }
    
    @Override
    public int getHeight() {

        return delegate.getHeight();
    }
    
    @Override
    public int save() {

        return delegate.save();
    }
    
    @Override
    public int save(final int saveFlags) {

        return delegate.save(saveFlags);
    }
    
    @Override
    public int saveLayer(final RectF bounds, final Paint paint, final int saveFlags) {

        return delegate.saveLayer(bounds, paint, saveFlags);
    }
    
    @Override
    public int saveLayer(final float left, final float top, final float right, final float bottom, final Paint paint,
                         final int saveFlags) {

        return delegate.saveLayer(left, top, right, bottom, paint, saveFlags);
    }
    
    @Override
    public int saveLayerAlpha(final RectF bounds, final int alpha, final int saveFlags) {

        return delegate.saveLayerAlpha(bounds, alpha, saveFlags);
    }
    
    @Override
    public int saveLayerAlpha(final float left, final float top, final float right, final float bottom,
                              final int alpha, final int saveFlags) {

        return delegate.saveLayerAlpha(left, top, right, bottom, alpha, saveFlags);
    }
    
    @Override
    public void restore() {

        delegate.restore();
    }
    
    @Override
    public int getSaveCount() {

        return delegate.getSaveCount();
    }
    
    @Override
    public void restoreToCount(final int saveCount) {

        delegate.restoreToCount(saveCount);
    }
    
    @Override
    public void translate(final float dx, final float dy) {

        delegate.translate(dx, dy);
    }
    
    @Override
    public void scale(final float sx, final float sy) {

        delegate.scale(sx, sy);
    }
    
    @Override
    public void rotate(final float degrees) {

        delegate.rotate(degrees);
    }
    
    @Override
    public void skew(final float sx, final float sy) {

        delegate.skew(sx, sy);
    }
    
    @Override
    public void concat(final Matrix matrix) {

        delegate.concat(matrix);
    }
    
    @Override
    public void setMatrix(final Matrix matrix) {

        delegate.setMatrix(matrix);
    }
    
    @Override
    public void getMatrix(final Matrix ctm) {

        delegate.getMatrix(ctm);
    }
    
    @Override
    public boolean clipRect(final RectF rect, final Region.Op op) {

        return delegate.clipRect(rect, op);
    }
    
    @Override
    public boolean clipRect(final Rect rect, final Region.Op op) {

        return delegate.clipRect(rect, op);
    }
    
    @Override
    public boolean clipRect(final RectF rect) {

        return delegate.clipRect(rect);
    }
    
    @Override
    public boolean clipRect(final Rect rect) {

        return delegate.clipRect(rect);
    }
    
    @Override
    public boolean clipRect(final float left, final float top, final float right, final float bottom, final Region.Op op) {

        return delegate.clipRect(left, top, right, bottom, op);
    }
    
    @Override
    public boolean clipRect(final float left, final float top, final float right, final float bottom) {

        return delegate.clipRect(left, top, right, bottom);
    }
    
    @Override
    public boolean clipRect(final int left, final int top, final int right, final int bottom) {

        return delegate.clipRect(left, top, right, bottom);
    }
    
    @Override
    public boolean clipPath(final Path path, final Region.Op op) {

        return delegate.clipPath(path, op);
    }
    
    @Override
    public boolean clipPath(final Path path) {

        return delegate.clipPath(path);
    }
    
    @Override
    public boolean clipRegion(final Region region, final Region.Op op) {

        return delegate.clipRegion(region, op);
    }
    
    @Override
    public boolean clipRegion(final Region region) {

        return delegate.clipRegion(region);
    }
    
    @Override
    public DrawFilter getDrawFilter() {

        return delegate.getDrawFilter();
    }
    
    @Override
    public void setDrawFilter(final DrawFilter filter) {

        delegate.setDrawFilter(filter);
    }
    
    @Override
    public GL getGL() {

        return delegate.getGL();
    }
    
    @Override
    public boolean quickReject(final RectF rect, final EdgeType type) {

        return delegate.quickReject(rect, type);
    }
    
    @Override
    public boolean quickReject(final Path path, final EdgeType type) {

        return delegate.quickReject(path, type);
    }
    
    @Override
    public boolean quickReject(final float left, final float top, final float right, final float bottom,
                               final EdgeType type) {

        return delegate.quickReject(left, top, right, bottom, type);
    }
    
    @Override
    public boolean getClipBounds(final Rect bounds) {

        return delegate.getClipBounds(bounds);
    }
    
    @Override
    public void drawRGB(final int r, final int g, final int b) {

        delegate.drawRGB(r, g, b);
    }
    
    @Override
    public void drawARGB(final int a, final int r, final int g, final int b) {

        delegate.drawARGB(a, r, g, b);
    }
    
    @Override
    public void drawColor(final int color) {

        delegate.drawColor(color);
    }
    
    @Override
    public void drawColor(final int color, final PorterDuff.Mode mode) {

        delegate.drawColor(color, mode);
    }
    
    @Override
    public void drawPaint(final Paint paint) {

        delegate.drawPaint(paint);
    }
    
    @Override
    public void drawPoints(final float[] pts, final int offset, final int count, final Paint paint) {

        delegate.drawPoints(pts, offset, count, paint);
    }
    
    @Override
    public void drawPoints(final float[] pts, final Paint paint) {

        delegate.drawPoints(pts, paint);
    }
    
    @Override
    public void drawPoint(final float x, final float y, final Paint paint) {

        delegate.drawPoint(x, y, paint);
    }
    
    @Override
    public void drawLine(final float startX, final float startY, final float stopX, final float stopY, final Paint paint) {

        delegate.drawLine(startX, startY, stopX, stopY, paint);
    }
    
    @Override
    public void drawLines(final float[] pts, final int offset, final int count, final Paint paint) {

        delegate.drawLines(pts, offset, count, paint);
    }
    
    @Override
    public void drawLines(final float[] pts, final Paint paint) {

        delegate.drawLines(pts, paint);
    }
    
    @Override
    public void drawRect(final RectF rect, final Paint paint) {

        delegate.drawRect(rect, paint);
    }
    
    @Override
    public void drawRect(final Rect r, final Paint paint) {

        delegate.drawRect(r, paint);
    }
    
    @Override
    public void drawRect(final float left, final float top, final float right, final float bottom, final Paint paint) {

        delegate.drawRect(left, top, right, bottom, paint);
    }
    
    @Override
    public void drawOval(final RectF oval, final Paint paint) {

        delegate.drawOval(oval, paint);
    }
    
    @Override
    public void drawCircle(final float cx, final float cy, final float radius, final Paint paint) {

        delegate.drawCircle(cx, cy, radius, paint);
    }
    
    @Override
    public void drawArc(final RectF oval, final float startAngle, final float sweepAngle, final boolean useCenter,
                        final Paint paint) {

        delegate.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
    }
    
    @Override
    public void drawRoundRect(final RectF rect, final float rx, final float ry, final Paint paint) {

        delegate.drawRoundRect(rect, rx, ry, paint);
    }
    
    @Override
    public void drawPath(final Path path, final Paint paint) {

        delegate.drawPath(path, paint);
    }
    
    @Override
    public void drawBitmap(final Bitmap bitmap, final float left, final float top, Paint paint) {

        if (paint == null)
            paint = _smooth;
        else
            paint.setFilterBitmap(true);
        delegate.drawBitmap(bitmap, left, top, paint);
    }
    
    @Override
    public void drawBitmap(final Bitmap bitmap, final Rect src, final RectF dst, Paint paint) {

        if (paint == null)
            paint = _smooth;
        else
            paint.setFilterBitmap(true);
        delegate.drawBitmap(bitmap, src, dst, paint);
    }
    
    @Override
    public void drawBitmap(final Bitmap bitmap, final Rect src, final Rect dst, Paint paint) {

        if (paint == null)
            paint = _smooth;
        else
            paint.setFilterBitmap(true);
        delegate.drawBitmap(bitmap, src, dst, paint);
    }
    
    @Override
    public void drawBitmap(final int[] colors, final int offset, final int stride, final int x, final int y,
                           final int width, final int height, final boolean hasAlpha, Paint paint) {

        if (paint == null)
            paint = _smooth;
        else
            paint.setFilterBitmap(true);
        delegate.drawBitmap(colors, offset, stride, x, y, width, height, hasAlpha, paint);
    }
    
    @Override
    public void drawBitmap(final Bitmap bitmap, final Matrix matrix, Paint paint) {

        if (paint == null)
            paint = _smooth;
        else
            paint.setFilterBitmap(true);
        delegate.drawBitmap(bitmap, matrix, paint);
    }
    
    @Override
    public void drawBitmapMesh(final Bitmap bitmap, final int meshWidth, final int meshHeight, final float[] verts,
                               final int vertOffset, final int[] colors, final int colorOffset, final Paint paint) {

        delegate.drawBitmapMesh(bitmap, meshWidth, meshHeight, verts, vertOffset, colors, colorOffset, paint);
    }
    
    @Override
    public void drawVertices(final VertexMode mode, final int vertexCount, final float[] verts, final int vertOffset,
                             final float[] texs, final int texOffset, final int[] colors, final int colorOffset,
                             final short[] indices, final int indexOffset, final int indexCount, final Paint paint) {

        delegate.drawVertices(mode, vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices,
                              indexOffset, indexCount, paint);
    }
    
    @Override
    public void drawText(final char[] text, final int index, final int count, final float x, final float y,
                         final Paint paint) {

        delegate.drawText(text, index, count, x, y, paint);
    }
    
    @Override
    public void drawText(final String text, final float x, final float y, final Paint paint) {

        delegate.drawText(text, x, y, paint);
    }
    
    @Override
    public void drawText(final String text, final int start, final int end, final float x, final float y,
                         final Paint paint) {

        delegate.drawText(text, start, end, x, y, paint);
    }
    
    @Override
    public void drawText(final CharSequence text, final int start, final int end, final float x, final float y,
                         final Paint paint) {

        delegate.drawText(text, start, end, x, y, paint);
    }
    
    @Override
    public void drawPosText(final char[] text, final int index, final int count, final float[] pos, final Paint paint) {

        delegate.drawPosText(text, index, count, pos, paint);
    }
    
    @Override
    public void drawPosText(final String text, final float[] pos, final Paint paint) {

        delegate.drawPosText(text, pos, paint);
    }
    
    @Override
    public void drawTextOnPath(final char[] text, final int index, final int count, final Path path,
                               final float hOffset, final float vOffset, final Paint paint) {

        delegate.drawTextOnPath(text, index, count, path, hOffset, vOffset, paint);
    }
    
    @Override
    public void drawTextOnPath(final String text, final Path path, final float hOffset, final float vOffset,
                               final Paint paint) {

        delegate.drawTextOnPath(text, path, hOffset, vOffset, paint);
    }
    
    @Override
    public void drawPicture(final Picture picture) {

        delegate.drawPicture(picture);
    }
    
    @Override
    public void drawPicture(final Picture picture, final RectF dst) {

        delegate.drawPicture(picture, dst);
    }
    
    @Override
    public void drawPicture(final Picture picture, final Rect dst) {

        delegate.drawPicture(picture, dst);
    }
}