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
 * @author tom
 *
 */
public class CompassLineView extends View {

	private int AVAILABLE_WIDTH;
	private int CENTER_HEIGHT;

	private SparseArray<Integer> _directionPositions;
	private final Paint _paint = new Paint();

	/**
	 * @param context
	 */
	public CompassLineView(Context context, final int availableWidth, final int centerHeight) {
		super(context);
		
		AVAILABLE_WIDTH = availableWidth;
		CENTER_HEIGHT = centerHeight;

		_paint.setColor(Color.WHITE);
		_paint.setStrokeWidth(2.1f);
	}
	
	public void update(final SparseArray<Integer> directionPositions) {
		_directionPositions = directionPositions;
		invalidate(); // TODO dirty rectangle
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		
		// draw horizon line
    	canvas.drawLine(0, CENTER_HEIGHT, AVAILABLE_WIDTH, CENTER_HEIGHT, _paint); 
    	
    	if (_directionPositions.size() == 0) return; // not yet updated

    	// draw direction lines
        for (int i = 0; i < _directionPositions.size(); i++) {
        	canvas.drawLine(_directionPositions.valueAt(i), CENTER_HEIGHT - CompassView.POSITION_MARKER_HALFHEIGHT,
        					_directionPositions.valueAt(i), CENTER_HEIGHT + CompassView.POSITION_MARKER_HALFHEIGHT, _paint);
        }
	}
}
