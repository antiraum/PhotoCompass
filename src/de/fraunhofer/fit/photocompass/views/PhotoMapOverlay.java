package de.fraunhofer.fit.photocompass.views;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class PhotoMapOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> _overlays = new ArrayList<OverlayItem>();
	
	public PhotoMapOverlay(Drawable marker) {
        super(boundCenterBottom(marker));
	}
	
	public void addOverlay(OverlayItem overlay) {
		_overlays.add(overlay);
	    populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return _overlays.get(i);
	}
	
	@Override
	public int size() {
		return _overlays.size();
	}
}
