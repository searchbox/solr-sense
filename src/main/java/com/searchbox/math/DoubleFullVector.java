/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.math;

/**
 *
 *
 */
public class DoubleFullVector {
  private float[] vector;
  private float norm = -1;

  public DoubleFullVector(float[] vector) {
    this.vector = vector;
  }

  public boolean isEmpty() {
    return vector.length == 0;
  }

  public float getNorm() {
    if (norm == -1) {
      norm = VectorUtils.calculateNorm(vector);
    }
    return norm;
  }

  public float getDistance(final DoubleFullVector vector) {
    return getDistance(vector.getData());
  }

  public float getDistance(float vright[]) {
    float result = 0;
    float half;
    for (int zz = 0; zz < vector.length; zz++) {
      half = vector[zz] - vright[zz];
      result += half * half;
    }
    return (float) Math.sqrt(result);
  }

  public int getDimension() {
    return vector.length;
  }

  public float[] getData() {
    return vector;
  }

  public float dotProduct(final float[] weights) {
    float result = 0;
    for (int zz = 0; zz < vector.length; zz++) {
      result += vector[zz] * weights[zz];
    }
    return result;
  }

  public DoubleFullVector getSubVector(int start, int end) {
    float out[] = new float[end - start];
    for (int zz = start; zz < end; zz++) {
      out[zz - start] = vector[zz];
    }
    return new DoubleFullVector(out);
  }

  public DoubleFullVector getUnitVector() {
    float lnorm = getNorm();
    float[] out = new float[vector.length];
    for (int zz = 0; zz < vector.length; zz++) {
      out[zz] = vector[zz] * 1.0f / lnorm;
    }
    return new DoubleFullVector(out);
  }
}
