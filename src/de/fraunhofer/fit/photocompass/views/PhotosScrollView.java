package de.fraunhofer.fit.photocompass.views;

//import java.util.LinkedList;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.widget.HorizontalScrollView;
//import de.fraunhofer.fit.photocompass.model.data.Photo;
//
//public class PhotosScrollView extends HorizontalScrollView {
//
//	private float[] _mValues = null;
//	private PhotosView photosView;
//	
//	public PhotosScrollView(Context context, LinkedList<Photo> photos) {
//		super(context);
//        photosView = new PhotosView(context, photos);
//        addView(photosView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT));
//	}
//	
//	public void setMValues(float[] mValues) {
//		_mValues = mValues;
//    	int scrollX = (photosView.getMeasuredWidth() / 360) * (int)_mValues[3];
//    	scrollTo(scrollX, 0);
//	}
//}
