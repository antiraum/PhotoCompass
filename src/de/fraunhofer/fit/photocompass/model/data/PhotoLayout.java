package de.fraunhofer.fit.photocompass.model.data;

import android.widget.AbsoluteLayout.LayoutParams;

public class PhotoLayout {

	private int _x;
	private int _y;
	private int _width;
	private int _height;
	
	public PhotoLayout(int x, int y, int width, int height) {
		_x = x;
		_y = y;
		_width = width;
		_height = height;
	}

	public int getX() {
		return _x;
	}

	public int getY() {
		return _y;
	}

	public int getWidth() {
		return _width;
	}

	public int getHeight() {
		return _height;
	}
	
	public LayoutParams getAbsoluteLayoutParams() {
		return new LayoutParams(_width, _height, _x, _y);
	}
}
