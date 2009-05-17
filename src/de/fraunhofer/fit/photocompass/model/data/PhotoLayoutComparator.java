package de.fraunhofer.fit.photocompass.model.data;

import java.util.Comparator;

public class PhotoLayoutComparator implements Comparator<PhotoLayout> {

    public int compare(PhotoLayout pl1, PhotoLayout pl2) {

    	if (pl1.getHeight() > pl2.getHeight()) return 1;
    	return -1;
    }
}