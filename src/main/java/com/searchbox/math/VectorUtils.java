/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.math;

/**
 *
 * @author gamars
 */
public class VectorUtils {

//    public static float calculateNorm(final TermFreqVector v) {
//        return calculateNorm(v.getTermFrequencies());
//    }
    
    public static float calculateNorm(final float[] vector) {
        float norm = 0;
        for (int zz = 0; zz < vector.length; zz++) {
            norm += vector[zz] * vector[zz];
        }
        if (norm == 0.0) {
            return Float.MAX_VALUE;
        } else {
            return (float) Math.sqrt(norm);
        }
    }
    
    public static float calculateNorm(final int[] v) {
        float norm = 0;
        for (int each : v) {
            norm += each * each;
        }
        return (float)Math.sqrt(norm);
    }
    
//    public static long calculateSum(final TermFreqVector vector) {
//        return calculateSum(vector.getTermFrequencies());
//    }
    
    public static long calculateSum(final int[] v) {
        long sum = 0;
        for (int each : v) {
            sum += each;
        }
        return sum;
    }    
}
