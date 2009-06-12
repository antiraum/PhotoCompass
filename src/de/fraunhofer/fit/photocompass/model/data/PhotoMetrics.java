package de.fraunhofer.fit.photocompass.model.data;

import android.widget.AbsoluteLayout.LayoutParams;

/**
 * This class is a custom data type for photo metrics.
 * It is used by views to store the current position and dimension of photo related views.
 */
public final class PhotoMetrics {

	public static final int MINIMIZED_PHOTO_HEIGHT = 30; // height of a minimized photo in the PhotosView 
	public static final int MAPS_MINIMIZED_PHOTO_HEIGHT = 20; // height of a minimized photo in the PhotosOverlay
	
	private int _x;
	private int _y = 0;
	private int _width = 0;
	private int _height = 0;
	private LayoutParams _layoutParams; // layout parameters for AbsoluteLayout
	private LayoutParams _minimizedLayoutParams; // layout parameters for AbsoluteLayout

	/**
	 * @param left Left position.
	 */
	public void setLeft(final int left) {
		_x = left;
		_setLayoutParams();
		_setMinimizedLayoutParams();
	}

	/**
	 * @return Left position.
	 */
	public int getLeft() {
		return _x;
	}

	/**
	 * @param top Top position.
	 */
	public void setTop(final int top) {
		_y = top;
		_setLayoutParams();
		_setMinimizedLayoutParams();
	}

	/**
	 * @return Top position.
	 */
	public int getTop() {
		return _y;
	}

	/**
	 * @param width
	 */
	public void setWidth(final int width) {
		_width = width;
		_setLayoutParams();
		_setMinimizedLayoutParams();
	}

	/**
	 * @return Width
	 */
	public int getWidth() {
		return _width;
	}

	/**
	 * @param height
	 */
	public void setHeight(final int height) {
		_height = height;
		_setLayoutParams();
		_setMinimizedLayoutParams();
	}

	/**
	 * @return Height
	 */
	public int getHeight() {
		return _height;
	}

	/**
	 * @return Right position.
	 */
	public int getRight() {
		return _x + _width;
	}

	/**
	 * @return Bottom position.
	 */
	public int getBottom() {
		return _y + _height;
	}
	
	private void _setLayoutParams() {
		_layoutParams = new LayoutParams(_width, _height, _x, _y);
	}
	
	/**
	 * @return {@link LayoutParams} for an {@link android.widget.AbsoluteLayout}.
	 */
	public LayoutParams getLayoutParams() {
		return _layoutParams;
	}
	
	private void _setMinimizedLayoutParams() {
    	// minimized photos are displayed with MINIMIZED_PHOTO_HEIGHT at the same bottom position as unminimized
		_minimizedLayoutParams = new LayoutParams(_width, MINIMIZED_PHOTO_HEIGHT, _x, _y + _height - MINIMIZED_PHOTO_HEIGHT);
	}

	/**
	 * @return {@link LayoutParams} for an {@link android.widget.AbsoluteLayout} for a photo in minimized state.
	 */
	public LayoutParams getMinimizedLayoutParams() {
		return _minimizedLayoutParams;
	}
}
