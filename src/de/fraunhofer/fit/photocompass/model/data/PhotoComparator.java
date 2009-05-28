package de.fraunhofer.fit.photocompass.model.data;

import java.util.Comparator;

public class PhotoComparator implements Comparator<Photo> {

    public int compare(Photo p1, Photo p2) {

    	if (p1.getDistance() < p2.getDistance()) return 1;
    	return -1;
    }
}