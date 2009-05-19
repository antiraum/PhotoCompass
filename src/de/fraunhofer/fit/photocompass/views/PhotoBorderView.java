package de.fraunhofer.fit.photocompass.views;

import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class PhotoBorderView extends View {

	private int _width;
	private int _height;
	private Paint _paint;
	
	public PhotoBorderView(Context context, int width, int height) {
		super(context);
		_width = width;
		_height = height;
		_paint = new Paint(); 
		_paint.setColor(Color.parseColor(PhotoCompassApplication.ORANGE)); 
		_paint.setStrokeWidth(2f);
	}
	
    @Override 
    protected void onDraw(Canvas canvas) { 
    	canvas.drawLine(0, 0, _width, 0, _paint); 
    	canvas.drawLine(_width, 0, _width, _height, _paint); 
    	canvas.drawLine(_width, _height, 0, _height, _paint); 
    	canvas.drawLine(0, _height, 0, 0, _paint); 
    } 
}
