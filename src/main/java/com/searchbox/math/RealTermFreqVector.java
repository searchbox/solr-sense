/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.math;

import com.searchbox.utils.TermFreqContainer;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author serge
 */
public class RealTermFreqVector {

    private Map<String, Float> index;

    public Map<String, Float> getData() {
        return index;
    }

    public void setIndex(Map<String, Float> index) {
        this.index = index;
    }

    public RealTermFreqVector(Map<String, Float> termFreqMap) {
        this.index = termFreqMap;
    }

    public RealTermFreqVector(TermFreqContainer tfc) {
        this.index = new HashMap<String, Float>();
        for (int zz = 0; zz < tfc.getSize(); zz++) {
            index.put(tfc.getTerms()[zz], tfc.getFreqs()[zz]);
        }

    }

    public float getDistance(final RealTermFreqVector other) {
        float distance = 0;
        float ldiff = 0;
        Collection<String> totalterms = new HashSet<String>();

        totalterms.addAll(this.index.keySet());
        totalterms.addAll(other.index.keySet());

        for (String term : totalterms) {
            Float v1 = index.get(term);
            if (v1 == null) {
                v1 = 0.0f;
            }

            Float v2 = other.index.get(term);
            if (v2 == null) {
                v2 = 0.0f;
            }
            ldiff = v1 - v2;
            distance += ldiff * ldiff;
        }
        return (float) Math.sqrt(distance);
    }

    public RealTermFreqVector getUnitVector() {
        Map<String, Float> termFreqMap = new HashMap<String, Float>(index);
        float norm = 0;
        float val = 0;

        for (Entry<String, Float> term : index.entrySet()) {
            val = term.getValue();
            norm += val * val;
        }
        norm = (float) Math.sqrt(norm);

        for (Entry<String, Float> term : termFreqMap.entrySet()) {
            val = term.getValue();
            val = val / norm;
            termFreqMap.put(term.getKey(), val);
        }

        return new RealTermFreqVector(termFreqMap);
    }
}
