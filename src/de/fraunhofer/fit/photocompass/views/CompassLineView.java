/**
 * 
 */
package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.SparseArray;
import android.view.View;

/**
 * This view is used by the {@link CompassView}. Displays the horizontal line and the marker lines.
 */
public final class CompassLineView extends View {
	
	private static final float LINE_WIDTH = 2.1f;

	private int AVAILABLE_WIDTH;
	private int CENTER_HEIGHT;

	private SparseArray<Integer> _directionPositions; // x positions of the marks
	private final Paint _paint = new Paint();

	/**
	 * Constructor.
	 * Initializes class members.
	 * 
	 * @param context
	 */
	public CompassLineView(Context context, final int availableWidth, final int centerHeight) {
		super(context);
		
		AVAILABLE_WIDTH = availableWidth;
		CENTER_HEIGHT = centerHeight;

		_paint.setColor(Color.WHITE);
		_paint.setStrokeWidth(LINE_WIDTH);
	}
	
	/**
	 * Updates the x positions of the markers to draw.
	 * 
	 * @param directionPositions X positions of the markers.
	 */
	public void update(final SparseArray<Integer> directionPositions) {
		_directionPositions = directionPositions;
		invalidate(); // TODO dirty rectangle
	}

	/**
	 * Draws the lines.
	 */
	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		
		// draw horizon line
    	canvas.drawLine(0, CENTER_HEIGHT, AVAILABLE_WIDTH, CENTER_HEIGHT, _paint); 
    	
    	if (_directionPositions.size() == 0) return; // not yet updated

    	// draw markers
    	final int numPositions = _directionPositions.size();
    	int dirPos;
        for (int i = 0; i < numPositions; i++) {
        	dirPos = _directionPositions.valueAt(i);
        	if (dirPos + LINE_WIDTH / 2 < 0 || dirPos - LINE_WIDTH / 2 > AVAILABLE_WIDTH) continue; // not visible
        	canvas.drawLine(dirPos, CENTER_HEIGHT - CompassView.POSITION_MARKER_HALFHEIGHT,
        					dirPos, CENTER_HEIGHT + CompassView.POSITION_MARKER_HALFHEIGHT, _paint);
        }
	}
}
