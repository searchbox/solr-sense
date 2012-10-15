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

//    public static double calculateNorm(final TermFreqVector v) {
//        return calculateNorm(v.getTermFrequencies());
//    }
    
    public static double calculateNorm(final double[] vector) {
        double norm = 0;
        for (int zz = 0; zz < vector.length; zz++) {
            norm += vector[zz] * vector[zz];
        }
        return Math.sqrt(norm);
    }
    
    public static double calculateNorm(final int[] v) {
        double norm = 0;
        for (int each : v) {
            norm += each * each;
        }
        return Math.sqrt(norm);
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
