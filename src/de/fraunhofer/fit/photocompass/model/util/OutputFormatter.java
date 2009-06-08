/**
 * 
 */
package de.fraunhofer.fit.photocompass.model.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

/**
 * @author tom
 *
 */
public final class OutputFormatter {
	
	private static final StringBuilder _stringBuilder = new StringBuilder();
	private static final Formatter _fmt = new Formatter();
	
	public static String formatDistance(float distance) {
		
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
	 * Formats age.
	 * 
	 * @param age Age in milliseconds.
	 * @return	  
	 */
	public static String formatAge(long age) {
		
        final Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(System.currentTimeMillis() - age));
		
		_stringBuilder.setLength(0); // reset
		
		_stringBuilder.append(cal.get(Calendar.DAY_OF_MONTH));
		_stringBuilder.append(" ");
		_stringBuilder.append(cal.get(Calendar.MONTH));
		_stringBuilder.append(" ");
		_stringBuilder.append(cal.get(Calendar.HOUR_OF_DAY));
		_stringBuilder.append(".");
		_stringBuilder.append(cal.get(Calendar.MINUTE));
        
        return _stringBuilder.toString();
	}
	
	public static String formatAltOffset(double altOffset) {
		
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
