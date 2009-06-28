package de.fraunhofer.fit.photocompass.model.data;

import de.fraunhofer.fit.photocompass.views.layouts.SimpleAbsoluteLayout;
import de.fraunhofer.fit.photocompass.views.layouts.SimpleAbsoluteLayout.LayoutParams;

/**
 * This class is a custom data type for photo metrics.
 * It is used by views to store the current position and dimension of photo related views.
 */
public final class PhotoMetrics {

	public static final int MINIMIZED_PHOTO_HEIGHT = 30; // height of a minimized photo in the PhotosView 
	public static final int MAPS_MINIMIZED_PHOTO_HEIGHT = 20; // height of a minimized photo in the PhotosOverlay
	
	public int left = 0; // Left position
	public int top = 0; // Top position
	public int width = 0;
	public int height = 0;
	private LayoutParams _layoutParams; // layout parameters for SimpleAbsoluteLayout
	private LayoutParams _minimizedLayoutParams; // layout parameters for SimpleAbsoluteLayout

	/**
	 * @param left Left position.
	 */
	public void setLeft(final int value) {
		left = value;
		_setLayoutParams();
		_setMinimizedLayoutParams();
	}

	/**
	 * @param top Top position.
	 */
	public void setTop(final int value) {
		top = value;
		_setLayoutParams();
	}

	/**
	 * @param width
	 */
	public void setWidth(final int value) {
		width = value;
		_setLayoutParams();
		_setMinimizedLayoutParams();
	}

	/**
	 * @param height
	 */
	public void setHeight(final int value) {
		height = value;
		_setLayoutParams();
	}

	/**
	 * @return Right position.
	 */
	public int getRight() {
		return left + width;
	}

	/**
	 * @return Bottom position.
	 */
	public int getBottom() {
		return top + height;
	}
	
	private void _setLayoutParams() {
		_layoutParams = new LayoutParams(width, height, left, top);
	}
	
	/**
	 * @return {@link LayoutParams} for an {@link SimpleAbsoluteLayout}.
	 */
	public LayoutParams getLayoutParams() {
		return _layoutParams;
	}
	
	private void _setMinimizedLayoutParams() {
    	// minimized photos are displayed with unchanged width and MINIMIZED_PHOTO_HEIGHT
		_minimizedLayoutParams = new LayoutParams(width, MINIMIZED_PHOTO_HEIGHT, left, 0);
	}

	/**
	 * @param availableHeight Height at which bottom the minimized photos should be displayed.
	 * @return {@link LayoutParams} for an {@link SimpleAbsoluteLayout} for a photo in minimized state.
	 */
	public LayoutParams getMinimizedLayoutParams(final int availableHeight) {
		_minimizedLayoutParams.y = availableHeight - MINIMIZED_PHOTO_HEIGHT;
		return _minimizedLayoutParams;
	}
}
