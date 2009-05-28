package de.fraunhofer.fit.photocompass.model.data;

import java.util.Comparator;

public class PhotoMetricsComparator implements Comparator<PhotoMetrics> {

    public int compare(PhotoMetrics pm1, PhotoMetrics pm2) {

    	if (pm1.getHeight() > pm2.getHeight()) return 1;
    	return -1;
    }
}