/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.math;

/**
 *
 * @author gamars
 */
public class DoubleFullVector {
       private double[] vector;

    public DoubleFullVector(double[] vector) {
        this.vector = vector;
    }

    public boolean isEmpty() {
        return vector.length == 0;
    }

    public double getNorm() {
        return VectorUtils.calculateNorm(vector);
    }

    public double getDistance(final DoubleFullVector vector) {
        return getDistance(vector.getData());
    }

    public double getDistance(double vright[]) {
        double result = 0;
        double half;
        for (int zz = 0; zz < vector.length; zz++) {
            half = vector[zz] - vright[zz];
            result += half * half;
        }
        return Math.sqrt(result);
    }

    public int getDimension() {
        return vector.length;
    }

    public double[] getData() {
        return vector;
    }

    public double dotProduct(final double[] weights) {
        double result = 0;
        for (int zz = 0; zz < vector.length; zz++) {
            result += vector[zz] * weights[zz];
        }
        return result;
    }

    public DoubleFullVector getSubVector(int start, int end) {
        double out[] = new double[end - start];
        for (int zz = start; zz < end; zz++) {
            out[zz - start] = vector[zz];
        }
        return new DoubleFullVector(out);
    }

    public DoubleFullVector getUnitVector() {
        double norm = getNorm();
        double[] out = new double[vector.length];
        for (int zz = 0; zz < vector.length; zz++) {
            out[zz] = vector[zz] * 1.0 / norm;
        }
        return new DoubleFullVector(out);
    }
}
