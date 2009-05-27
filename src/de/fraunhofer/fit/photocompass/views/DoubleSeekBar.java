package de.fraunhofer.fit.photocompass.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;
import de.fraunhofer.fit.photocompass.R;

public class DoubleSeekBar extends View {
	private float startValue = 0f;
	private float endValue = 1f;
	private int leftMargin;
	private int rightMargin;
	
	private Drawable leftThumb;
	private Drawable rightThumb;
	private Rect selectionRect;

	public DoubleSeekBar(Context context) {
		super(context);
		this.leftThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal);
		this.leftMargin = this.leftThumb.getIntrinsicWidth()/2;
		this.rightThumb = this.getResources().getDrawable(
				R.drawable.seek_thumb_normal);
		this.rightMargin = this.rightThumb.getIntrinsicWidth()/2;
		this.selectionRect = new Rect(this.leftMargin, 4, this.getMeasuredWidth() - this.rightMargin,
				26);
		// this.leftThumb.setImageResource(R.drawable.seek_thumb_normal);
		// this.addView(this.leftThumb);
		// this.rightThumb.setImageResource(R.drawable.seek_thumb_normal);
		// this.addView(this.rightThumb);
		// this.leftThumb.setLayoutParams(new LayoutParams(0, 0,
		// 32, 29));
		// this.leftThumb.setScaleType(ScaleType.CENTER);
		// this.rightThumb.setLayoutParams(new LayoutParams(this
		// .getWidth() - 32, 0, 32, 29));
		// this.rightThumb.setScaleType(ScaleType.CENTER);
		
	}

	@Override
	protected void onDraw(Canvas canvas) {
		this.updateBounds();

		super.onDraw(canvas);
		Paint p = new Paint();
		p.setColor(Color.GRAY);
		p.setStyle(Style.FILL);
		canvas
				.drawRoundRect(new RectF(0f, 4f, this.getWidth(), 26f), 5f, 5f,
						p);
		p.setColor(Color.YELLOW);
		canvas.drawRect(this.selectionRect, p);
		// Drawable thumb =
		// getResources().getDrawable(R.drawable.seek_thumb_normal);
		leftThumb.draw(canvas);
		// thumb.setBounds(this.getWidth() - 32, 0, this.getWidth(), 29);
		rightThumb.draw(canvas);
	}
//
//	@Override
//	protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
//
//	}

	private void updateBounds() {
//		Log.d(PhotoCompassApplication.LOG_TAG, "Width: " + this.getWidth());
		int left = Math.round(this.startValue
				* (this.getWidth() - this.leftThumb.getIntrinsicWidth()));
		this.leftThumb.setBounds(left, 0, left
				+ this.leftThumb.getIntrinsicWidth(), this.leftThumb
				.getIntrinsicHeight());
		this.selectionRect.left = left + this.leftThumb.getIntrinsicWidth() / 2;
//		System.out.println(this.leftThumb.getBounds());

		left = Math.round(this.endValue
				* (this.getWidth() - this.rightThumb.getIntrinsicWidth()));
		this.rightThumb.setBounds(left, 0, left
				+ this.rightThumb.getIntrinsicWidth(), this.rightThumb
				.getIntrinsicHeight());
		this.selectionRect.right = left + this.rightThumb.getIntrinsicWidth()
				/ 2;
//		System.out.println(this.rightThumb.getBounds());

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(PhotoCompassApplication.LOG_TAG, "MotionEvent action " + event.getAction());
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			// determine whether left or right thumb concerned
			if(Math.abs(event.getX()-this.leftThumb.getBounds().right) < Math.abs(event.getX()-this.rightThumb.getBounds().left)) {
				// distance to left is less than distance to right
				this.startValue = (event.getX() - this.leftMargin) / (this.getWidth() - this.leftMargin - this.rightMargin);
//				this.leftThumb.setBounds(Math.round(event.getX() - this.leftThumb.getIntrinsicWidth()/2), this.leftThumb.getBounds().top, Math.round(event.getX() + this.leftThumb.getIntrinsicWidth()/2), this.leftThumb.getBounds().bottom);
//				Log.d(PhotoCompassApplication.LOG_TAG, "Updated left bounds");
				this.updateBounds();
				this.invalidate();
			} else {
				// distance to right is less than to left
				this.endValue = (event.getX() - this.leftMargin) / (this.getWidth() - this.leftMargin - this.rightMargin);
//				this.leftThumb.setBounds(Math.round(event.getX() - this.leftThumb.getIntrinsicWidth()/2), this.leftThumb.getBounds().top, Math.round(event.getX() + this.leftThumb.getIntrinsicWidth()/2), this.leftThumb.getBounds().bottom);
//				Log.d(PhotoCompassApplication.LOG_TAG, "Updated left bounds");
				this.updateBounds();
				this.invalidate();
//				this.rightThumb.setBounds(Math.round(event.getX() - this.rightThumb.getIntrinsicWidth()/2), this.rightThumb.getBounds().top, Math.round(event.getX() + this.rightThumb.getIntrinsicWidth()/2), this.rightThumb.getBounds().bottom);
//				Log.d(PhotoCompassApplication.LOG_TAG, "Updated right bounds");
			}
		}
		return true;
	}
	
	/**
	 * @return the start value (left slider thumb), as a float from the range [0,1].
	 */
	public float getStartValue() {
		return this.startValue;
	}
	
	/**
	 * @return the end value (right slider thumb), as a float from the range [0,1].
	 */
	public float getEndValue() {
		return this.endValue;
	}
	
}
