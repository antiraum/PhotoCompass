/**
 * 
 */
package de.fraunhofer.fit.photocompass.model.util;

import java.util.Formatter;

import android.util.Log;
import de.fraunhofer.fit.photocompass.PhotoCompassApplication;

/**
 * This class provides static methods to format distance, age, and altitude offset values for display.
 */
public final class OutputFormatter {
	
	/**
	 * Formats a distance value for display.
	 * 
	 * @param distance Distance in meters.
	 * @return		   Formatted string.
	 */
	public static String formatDistance(final float distance) {
		if (Float.isNaN(distance)) return "";
		
//    	Log.d(PhotoCompassApplication.LOG_TAG, "OutputFormatter: formatDistance: distance = "+distance);

    	final StringBuilder stringBuilder = new StringBuilder();

        if (distance < 1000) {
        	stringBuilder.append(Math.round(distance));
        	stringBuilder.append(" m");
        } else {
        	final Formatter fmt = new Formatter();
        	stringBuilder.append(fmt.format("%.1f", distance / 1000)); 
        	stringBuilder.append(" km");
        }
        
        final String str = stringBuilder.toString();
//        Log.d(PhotoCompassApplication.LOG_TAG, "OutputFormatter: formatDistance: str = "+str);
        return str;
	}
	
	/**
	 * Formats an age value for display.
	 * 
	 * @param age Age in milliseconds.
	 * @return	  Formatted string.
	 */
	public static String formatAge(final long age) {

//        final Calendar cal = Calendar.getInstance();
//        cal.setTime(new Date(System.currentTimeMillis() - age));

    	final StringBuilder stringBuilder = new StringBuilder();
		
//		stringBuilder.append(cal.get(Calendar.DAY_OF_MONTH));
//		stringBuilder.append(" ");
//		stringBuilder.append(cal.get(Calendar.MONTH));
//		stringBuilder.append(" ");
//		stringBuilder.append(cal.get(Calendar.HOUR_OF_DAY));
//		stringBuilder.append(".");
//		stringBuilder.append(cal.get(Calendar.MINUTE));
    	//2592000000
		float ageF = Math.round(age / 60 * 1000); // to minutes
    	//43200.00000000000000000000
		final int min = Math.round(ageF % 60); //0
		ageF = Math.round(ageF / 60); // to hours
		// 720
		final int hours = Math.round(ageF % 24); //0
		final int days = Math.round(ageF / 24); //30
		
		if (days > 0) {
			stringBuilder.append(days);
			stringBuilder.append(" days ");
			stringBuilder.append(hours);
			stringBuilder.append(" hr");
		} else if (hours > 0) {
			stringBuilder.append(hours);
			stringBuilder.append(" hr ");
			stringBuilder.append(min);
			stringBuilder.append(" min");
		} else {
			stringBuilder.append(min);
			stringBuilder.append(" min");
		}
		
        return stringBuilder.toString();
	}
	
	/**
	 * Formats an altitude offset for display.
	 * 
	 * @param altOffset Altitude offset in meters.
	 * @return			Formatted string.
	 */
	public static String formatAltOffset(final double altOffset) {
		if (Double.isNaN(altOffset)) return "";

    	final StringBuilder stringBuilder = new StringBuilder();
		
        if (altOffset == 0) {
        	stringBuilder.append("same level");
        } else {
        	stringBuilder.append(Math.abs(Math.round(altOffset)));
        	stringBuilder.append(" m ");
        	stringBuilder.append((altOffset > 0) ? "higher" : "lower");
        }
        
        return stringBuilder.toString();
	}
}
