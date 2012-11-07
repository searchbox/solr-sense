/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.math;

import java.util.*;
import java.util.Map.Entry;


/**
 *
 * @author serge
 */
public class RealTermFreqVector {

    private Map<String, Double> index;

    public Map<String, Double> getData() {
        return index;
    }
     public void setIndex(Map<String, Double> index) {
        this.index = index;
    }
     
    public RealTermFreqVector(Map<String, Double> termFreqMap) {
        this.index=termFreqMap;
    }

    
    public float getDistance(final RealTermFreqVector other) {
        double distance = 0;
        double ldiff = 0;
        Collection<String> totalterms = new HashSet<String>();

        totalterms.addAll(this.index.keySet());
        totalterms.addAll(other.index.keySet());

        for (String term : totalterms) {
            Double v1 = index.get(term);
            if (v1 == null) {
                v1 = 0.0;
            }

            Double v2 = other.index.get(term);
            if (v2 == null) {
                v2 = 0.0;
            }
            ldiff = v1 - v2;
            distance += ldiff * ldiff;
        }
        return (float) Math.sqrt(distance);
    }

    public RealTermFreqVector getUnitVector() {
        Map<String, Double> termFreqMap= new HashMap<String, Double>(index);
        double norm=0;
        double val=0;
        
        for (Entry<String, Double> term : index.entrySet()) {
            val=term.getValue();
            norm+=val*val;
        }
        norm=Math.sqrt(norm);
     
        for (Entry<String, Double> term : termFreqMap.entrySet()) {
            val=term.getValue();
            val=val/norm;
            termFreqMap.put(term.getKey(), val);
        }
        
        return new RealTermFreqVector(termFreqMap);
    }

    
}
