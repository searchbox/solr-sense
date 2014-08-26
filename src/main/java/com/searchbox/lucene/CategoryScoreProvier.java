/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.lucene;

import com.searchbox.math.RealTermFreqVector;
import com.searchbox.sense.CategorizationBase;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author andrew
 */
public class CategoryScoreProvier extends CustomScoreProvider {


  private static final Logger LOGGER = LoggerFactory.getLogger(SenseScoreProvider.class);

  private final CategorizationBase model;
  private final String senseField;

  CategoryScoreProvier(AtomicReaderContext context, CategorizationBase model, String senseField) {
    super(context);
    this.model = model;
    this.senseField = senseField;
  }

  public static RealTermFreqVector getTermFreqmapfromTermsContainer(Terms terms) throws IOException {
    if (terms != null) {
      RealTermFreqVector rtfv = new RealTermFreqVector((int) terms.size());
      final TermsEnum termsEnum = terms.iterator(null);
      BytesRef text;
      while ((text = termsEnum.next()) != null) {
        final String term = text.utf8ToString();
        final int freq = (int) termsEnum.totalTermFreq();
        rtfv.set(term, freq, rtfv.getNextpos());
      }
      return rtfv;
    } else {
      return new RealTermFreqVector(0);
    }
  }


  @Override
  public float customScore(int doc, float subQueryScore, float valSrcScores[]) throws IOException {

    Terms terms = context.reader().getTermVector(doc, this.senseField);
    RealTermFreqVector rtfv = getTermFreqmapfromTermsContainer(terms);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Evaluating Document with TF size: " + rtfv.getSize());
    }

    if (LOGGER.isTraceEnabled()) {
      for (int zz = 0; zz < rtfv.getSize(); zz++) {
        LOGGER.trace("term: |" + rtfv.getTerms()[zz] + "| -- frequ: " + rtfv.getFreqs()[zz]);
      }
    }

    return 2 - (float) model.categorize(rtfv);
  }

}
