package de.fraunhofer.fit.photocompass.util;

import java.util.ArrayList;

/**
 * @author tom
 *
 */
public final class ListArrayConversions {
    
    /**
     * Converts a primitive array of integers to an {@link ArrayList} of {@link Integer}s.
     * 
     * @param arr Array of primitive integers.
     * @return ArrayList of Integers.
     */
    public static ArrayList<Integer> intArrayToList(final int[] arr) {
        
        final ArrayList<Integer> list = new ArrayList<Integer>();
        for (final int i : arr) {
            list.add(i);
        }
        return list;
    }
    
    /**
     * Converts an {@link ArrayList} of {@link Integer}s to a primitive array of integers.
     * 
     * @param list ArrayList of Integers.
     * @return Array of primitive integers.
     */
    public static int[] intListToPrimitives(final ArrayList<Integer> list) {
        
        final int size = list.size();
        final int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }
        return array;
    }
    
    /**
     * Converts an {@link ArrayList} of {@link Float}s to a primitive array of floats.
     * 
     * @param list ArrayList of Floats.
     * @return Array of primitive floats.
     */
    public static float[] floatListToPrimitives(final ArrayList<Float> list) {
        
        final int size = list.size();
        final float[] array = new float[size];
        for (int i = 0; i < size; i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
