/**
 * 
 */
package de.fraunhofer.fit.photocompass.model.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

/**
 * This class provides static methods to format distance, age, and altitude offset values for display.
 */
public final class OutputFormatter {
	
	private static final StringBuilder _stringBuilder = new StringBuilder();
	private static final Formatter _fmt = new Formatter();
	
	/**
	 * Formats a distance value for display.
	 * 
	 * @param distance Distance in meters.
	 * @return		   Formatted string.
	 */
	public static String formatDistance(final float distance) {
		
		_stringBuilder.setLength(0); // reset

        if (distance < 1000) {
        	_stringBuilder.append(Math.round(distance));
        	_stringBuilder.append(" m");
        } else {
        	_stringBuilder.append(_fmt.format("%.1f", distance / 1000)); 
        	_stringBuilder.append(" km");
        }
        
        return _stringBuilder.toString();
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
		
		_stringBuilder.setLength(0); // reset
		
//		_stringBuilder.append(cal.get(Calendar.DAY_OF_MONTH));
//		_stringBuilder.append(" ");
//		_stringBuilder.append(cal.get(Calendar.MONTH));
//		_stringBuilder.append(" ");
//		_stringBuilder.append(cal.get(Calendar.HOUR_OF_DAY));
//		_stringBuilder.append(".");
//		_stringBuilder.append(cal.get(Calendar.MINUTE));
     
		float ageF = Math.round(age / 60 * 1000); // to minutes
		final int min = (int) (ageF % 60);
		ageF = Math.round(ageF / 60); // to hours
		final int hours = (int) (ageF % 24);
		final int days = Math.round(ageF / 24);
		
		if (days > 0) {
			_stringBuilder.append(days);
			_stringBuilder.append(" days ");
			_stringBuilder.append(hours);
			_stringBuilder.append(" hours");
		} else if (hours > 0) {
			_stringBuilder.append(hours);
			_stringBuilder.append(" hours ");
			_stringBuilder.append(min);
			_stringBuilder.append(" min");
		} else {
			_stringBuilder.append(min);
			_stringBuilder.append(" min");
		}
		
        return _stringBuilder.toString();
	}
	
	/**
	 * Formats an altitude offset for display.
	 * 
	 * @param altOffset Altitude offset in meters.
	 * @return			Formatted string.
	 */
	public static String formatAltOffset(final double altOffset) {
		
		_stringBuilder.setLength(0); // reset
		
        if (altOffset == 0) {
        	_stringBuilder.append("same level");
        } else {
        	_stringBuilder.append(Math.abs(Math.round(altOffset)));
        	_stringBuilder.append(" m ");
        	_stringBuilder.append((altOffset > 0) ? "higher" : "lower");
        }
        
        return _stringBuilder.toString();
	}
}
