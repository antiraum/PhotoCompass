package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.Gravity;
import android.widget.TextView;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.views.layouts.SimpleAbsoluteLayout;

/**
 * This view is used by the {@link de.fraunhofer.fit.photocompass.activities.FinderActivity} and displays the compass "horizon".
 * Displays a horizontal line in the middle of the {@link #AVAILABLE_WIDTH}. Every 45 degrees a marker is drawn with a text label
 * of the compass direction.
 */
public final class CompassView extends SimpleAbsoluteLayout {
	
	static final int POSITION_MARKER_HALFHEIGHT = 7;
	static final int LABEL_WIDTH = 25;
	static final int LABEL_HEIGHT = 20;
	
	private static final int[] _directionDegrees = {0, 45, 90, 135, 180, 225, 270, 315}; // directions to mark
	private final SparseArray<TextView> _directionLabels = new SparseArray<TextView>(); // text views for the labels of the marks
	private final SparseArray<Integer> _directionPositions = new SparseArray<Integer>(); // x positions of the marks
	private CompassLineView _lineView;
	
	private int AVAILABLE_WIDTH;
	private int CENTER_HEIGHT;
	
	private static float DEGREE_WIDTH; // width of one degree direction  
	
	private float _direction;

	/**
	 * Constructor.
	 * Initializes the line and label views. Populates {@link #_directionLabels}.
	 * 
	 * @param context
	 */
	public CompassView(final Context context, final int availableWidth, final int availableHeight) {
		super(context);
        
        AVAILABLE_WIDTH = availableWidth;
        CENTER_HEIGHT = availableHeight / 2;
        DEGREE_WIDTH = AVAILABLE_WIDTH / PhotoCompassApplication.CAMERA_HDEGREES;
		
		// add line view
		_lineView = new CompassLineView(context, AVAILABLE_WIDTH, CENTER_HEIGHT);
		addView(_lineView);
        
        // populate and add direction labels
        TextView tv;
        for (int deg : _directionDegrees) {
        	tv = new TextView(context);
            tv.setTextColor(Color.WHITE);
            tv.setWidth(LABEL_WIDTH);
            tv.setHeight(LABEL_HEIGHT);
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
            _directionLabels.put(deg, tv);
            addView(tv);
        }
        _directionLabels.get(0).setText("N");
        _directionLabels.get(45).setText("NE");
        _directionLabels.get(90).setText("E");
        _directionLabels.get(135).setText("SE");
        _directionLabels.get(180).setText("S");
        _directionLabels.get(225).setText("SW");
        _directionLabels.get(270).setText("W");
        _directionLabels.get(315).setText("NW");
	}
	
	/**
	 * Updates the view based on the current viewing direction.
	 * Re-calculates the x positions of the direction markers. 
	 * 
	 * @param direction Current viewing direction in degrees (0 - 360: 0 = North, 90 = East, 180 = South, 270 = West).
	 */
	public void update(final float direction) {
//    	Log.d(PhotoCompassApplication.LOG_TAG, "CompassView: update: direction = "+direction);
    	
    	// update variable
		_direction = direction;
		
		// update direction positions and place labels
		LayoutParams layoutParams;
		TextView dirLabel;
        for (int deg : _directionDegrees) {
        	_directionPositions.put(deg, (int) Math.round(AVAILABLE_WIDTH / 2 + (deg - _direction) * DEGREE_WIDTH));
        	
        	layoutParams = new LayoutParams(LABEL_WIDTH, LABEL_HEIGHT, _directionPositions.get(deg) - LABEL_WIDTH / 2,
					  						CENTER_HEIGHT + POSITION_MARKER_HALFHEIGHT);
        	
        	// skip if label has layout parameters, and is not and will not be visible on screen
        	dirLabel = _directionLabels.get(deg);
        	if (dirLabel.getLayoutParams() != null &&
        		(dirLabel.getRight() < 0 && layoutParams.x + LABEL_WIDTH < 0) || // left of screen
        		(dirLabel.getLeft() > AVAILABLE_WIDTH && layoutParams.x > AVAILABLE_WIDTH)) // right of screen
        		continue;
        	
//        	Log.d(PhotoCompassApplication.LOG_TAG, "CompassView: update: deg = "+deg+", x = "+layoutParams.x+", y = "+layoutParams.y+", width = "+layoutParams.width+", height = "+layoutParams.height);
        	
        	dirLabel.setLayoutParams(layoutParams);
        }
		
		// update lineView
		_lineView.update(_directionPositions);
	}
}
