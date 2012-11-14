/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.math;

import java.util.Map;

/**
 *
 * @author serge
 */
public class RealTermFreqVector {

    private String[] terms;
    private float[] freqs;
    int nextpos;

    public String[] getTerms() {
        return terms;
    }

    public void setTerms(String[] terms) {
        this.terms = terms;
    }

    public float[] getFreqs() {
        return freqs;
    }

    public int getSize() {
        return terms.length;
    }
    public void setFreqs(float[] freqs) {
        this.freqs = freqs;
    }

    public RealTermFreqVector(int size) {
        terms= new String[size];
        freqs= new float[size];
        nextpos=0;
    }
    
    public RealTermFreqVector(String[] terms, float[] freqs) {
        this.terms = terms;
        this.freqs = freqs;
        
    }
    
    public int getNextpos() {
        nextpos++;
        return nextpos-1;
    }

    public float getDistance(final RealTermFreqVector other) {
        float distance = 0;
        float tempval = 0;
        int greaterorless;
        String terms_right[] = other.getTerms();
        float freqs_right[] = other.getFreqs();
        int spotleft = terms.length == 0 ? java.lang.Integer.MAX_VALUE : 0;
        int spotright = terms_right.length == 0 ? java.lang.Integer.MAX_VALUE : 0;

        while (true) {
            if (spotleft >= terms.length) {
                // finish right side
                for (; spotright < terms_right.length; spotright++) {
                    distance += freqs_right[spotright] * freqs_right[spotright];
                }
                break;
            }

            if (spotright >= terms_right.length) {
                for (; spotleft < terms.length; spotleft++) {
                    distance += freqs[spotleft] * freqs[spotleft];
                }

                break;
            }

            greaterorless = terms[spotleft].compareTo(terms_right[spotright]);
            if (greaterorless > 0) {
                distance += freqs[spotleft] * freqs[spotleft];
                spotleft++;
            } else if (greaterorless < 0) {
                distance += freqs_right[spotright] * freqs_right[spotright];
                spotright++;
            } else { // right and left the same
                tempval = freqs[spotleft] - freqs_right[spotright];
                distance += tempval * tempval;
                spotleft++;
                spotright++;
            }


        }
        return distance;
    }

    public RealTermFreqVector getUnitVector() {
        float norm = 0;

        for (int zz = 0; zz < freqs.length; zz++) {
            norm += freqs[zz] * freqs[zz];
        }

        norm = (float) Math.sqrt(norm);

        float[] newfreqs = freqs;
        for (int zz = 0; zz < freqs.length; zz++) {
            newfreqs[zz] = freqs[zz] / norm;
        }

        return new RealTermFreqVector(terms, newfreqs);
    }
    
    
    public RealTermFreqVector(Map<String, Float> termFreqMap) {
        terms = termFreqMap.keySet().toArray(new String[0]);
        java.util.Arrays.sort(terms);
        
        freqs = new float[terms.length];
        for (int zz = 0; zz < terms.length; zz++) {
            freqs[zz] = (float) termFreqMap.get(terms[zz]);
        }
        nextpos = terms.length;
    }
    
    public void set(String term, float freq, int pos) {
        terms[pos]=term;
        freqs[pos]=freq;
    }
    
}
