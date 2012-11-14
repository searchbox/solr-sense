/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.utils;

/**
 *
 * @author andrew
 */
public class TermFreqContainer {
    String [] terms;
    float [] freqs;
    int size;
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

    public void setFreqs(float[] freqs) {
        this.freqs = freqs;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNextpos() {
        nextpos++;
        return nextpos-1;
    }

    public void setNextpos(int nextpos) {
        this.nextpos = nextpos;
    }
    
    
    
    public TermFreqContainer(int size) {
        terms= new String[size];
        freqs= new float[size];
        this.size=size;
        nextpos=0;
    }
    public void set(String term, float freq, int pos) {
        terms[pos]=term;
        freqs[pos]=freq;
    }
}
