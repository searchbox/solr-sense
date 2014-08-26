/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.math;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;


/**
 * @author serge
 */
public class RealTermFreqVector {

  private static final Logger LOGGER = LoggerFactory.getLogger(RealTermFreqVector.class);
  int size;
  int nextpos;
  private String[] terms;
  private float[] freqs;
  private float norm = -1;

  public RealTermFreqVector(int size) {
    terms = new String[size];
    freqs = new float[size];
    nextpos = 0;
    this.size = size;
  }

  public RealTermFreqVector(String[] terms, float[] freqs, int size) {
    this.terms = terms;
    this.freqs = freqs;
    this.size = size;
  }

  //---- various constructors
  public RealTermFreqVector(Map<String, Float> termFreqMap) {
    terms = termFreqMap.keySet().toArray(new String[0]);
    java.util.Arrays.sort(terms);

    freqs = new float[terms.length];
    for (int zz = 0; zz < terms.length; zz++) {
      freqs[zz] = (float) termFreqMap.get(terms[zz]);
    }
    nextpos = terms.length;
    size = terms.length;
  }

  public RealTermFreqVector(int docID, IndexReader reader, String senseField) throws IOException {
    this(reader.getTermVector(docID, senseField));
        /*        if (terms == null) {
         throw new RuntimeException("No termVectorFrequency available for field: " + senseField);
         }
         */
  }

  public RealTermFreqVector(Terms terms) {
    this(termFreqMapTermsTotermFreqMap(terms));
  }

  public RealTermFreqVector(final String queryText, Analyzer analyzer) {
    this(quertTextToTermFreqmap(queryText, analyzer));
  }

  private static Map<String, Float> termFreqMapTermsTotermFreqMap(Terms terms) {
    Map<String, Float> termFreqMap = new HashMap<String, Float>();
    try {
      TermsEnum termsEnum = terms.iterator(null);
      BytesRef text;
      while ((text = termsEnum.next()) != null) {
        String term = text.utf8ToString();

        int freq = (int) termsEnum.totalTermFreq();
        Float prevFreq = termFreqMap.get(term);
        if (prevFreq == null) {
          termFreqMap.put(term, (float) freq);
        } else {
          termFreqMap.put(term, (float) freq + prevFreq);
        }
      }

    } catch (IOException ex) {
      LOGGER.error(ex.toString());
    }

    return termFreqMap;
  }

  private static Map<String, Float> quertTextToTermFreqmap(final String queryText, Analyzer analyzer) {
    Map<String, Float> termFreqMap = new HashMap<String, Float>();

    try {
      TokenStream ts = analyzer.tokenStream("", new StringReader(queryText));
      int tokenCount = 0;
      // for every token
      CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
      ts.reset();

      while (ts.incrementToken()) {
        String word = termAtt.toString();
        LOGGER.debug("\tAdding Term:\t" + word);
        tokenCount++;
        // increment frequency
        Float cnt = termFreqMap.get(word);
        if (cnt == null) {
          termFreqMap.put(word, new Float(1));
        } else {
          termFreqMap.put(word, cnt + 1);
        }
      }

      ts.end();
    } catch (IOException ex) {
      LOGGER.error(ex.toString());
    }

    return termFreqMap;
  }

  public float getNorm() {
    if (norm == -1) {
      norm = 0;
      for (int zz = 0; zz < size; zz++) {
        norm += freqs[zz] * freqs[zz];
      }
      norm = (float) Math.sqrt(norm);
    }
    return norm;
  }

  public void set(String term, float freq, int pos) {
    terms[pos] = term;
    freqs[pos] = freq;
  }

  public int getSize() {
    return this.size;
  }

  public void setSize(int size) {
    this.size = size;
  }

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

  public int getNextpos() {
    nextpos++;
    return nextpos - 1;
  }

  public float getDistance(final RealTermFreqVector other) {
    float distance = 0;
    float tempval = 0;
    int greaterorless;
    String terms_right[] = other.getTerms();
    float freqs_right[] = other.getFreqs();
    int size_right = other.getSize();
    int spotleft = terms.length == 0 ? java.lang.Integer.MAX_VALUE : 0;
    int spotright = terms_right.length == 0 ? java.lang.Integer.MAX_VALUE : 0;

    while (true) {
      if (spotleft >= size) {
        // finish right side
        for (; spotright < terms_right.length; spotright++) {
          distance += freqs_right[spotright] * freqs_right[spotright];
        }
        break;
      }

      if (spotright >= size_right) {
        for (; spotleft < terms.length; spotleft++) {
          distance += freqs[spotleft] * freqs[spotleft];
        }

        break;
      }

      greaterorless = terms[spotleft].compareTo(terms_right[spotright]);
      if (greaterorless < 0) {
        distance += freqs[spotleft] * freqs[spotleft];
        spotleft++;
      } else if (greaterorless > 0) {
        distance += freqs_right[spotright] * freqs_right[spotright];
        spotright++;
      } else { // right and left the same
        tempval = freqs[spotleft] - freqs_right[spotright];
        distance += tempval * tempval;
        spotleft++;
        spotright++;
      }


    }
    return (float) Math.sqrt(distance);
  }

  public RealTermFreqVector getUnitVector() {

    float lnorm = getNorm();
    float[] newfreqs = freqs;
    for (int zz = 0; zz < size; zz++) {
      newfreqs[zz] = freqs[zz] / lnorm;
    }

    return new RealTermFreqVector(terms, newfreqs, size);
  }


}
