/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.lucene;

import com.searchbox.sense.CKBUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.UnicodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gamars
 */
public class SenseScoreProvider extends CustomScoreProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CKBUtils.class);

    SenseScoreProvider(AtomicReaderContext context) {
        super(context);
    }

    /**
     * Compute a custom score by the subQuery score and a number of
     * {@link org.apache.lucene.queries.function.FunctionQuery} scores. <p>
     * Subclasses can override this method to modify the custom score. <p> If
     * your custom scoring is different than the default herein you should
     * override at least one of the two customScore() methods. If the number of
     * ValueSourceQueries is always &lt; 2 it is sufficient to override the
     * other {@link #customScore(int, float, float) customScore()} method, which
     * is simpler. <p> The default computation herein is a multiplication of
     * given scores:
     * <pre>
     *     ModifiedScore = valSrcScore * valSrcScores[0] * valSrcScores[1] * ...
     * </pre>
     *
     * @param doc id of scored doc.
     * @param subQueryScore score of that doc by the subQuery.
     * @param valSrcScores scores of that doc by the ValueSourceQuery.
     * @return custom score.
     */
    public float customScore(int doc, float subQueryScore, float valSrcScores[]) throws IOException {
        Map<String, Integer> termFreqMap = new HashMap<String, Integer>();
        Terms terms = context.reader().getTermVector(doc, "content_srch");

        if (terms != null) {
            final TermsEnum termsEnum = terms.iterator(null);
            BytesRef text;
            final CharsRef spare = new CharsRef();
            while ((text = termsEnum.next()) != null) {
                UnicodeUtil.UTF8toUTF16(text, spare);
                final String term = spare.toString();

                final int freq = (int) termsEnum.totalTermFreq();

                // increment frequency
                Integer cnt = termFreqMap.get(term);
                if (cnt == null) {
                    cnt = new Integer(freq);
                    termFreqMap.put(term, cnt);
                } else {
                    cnt += freq;
                }
            }
        }
        if(LOGGER.isTraceEnabled()){
            for (String t : termFreqMap.keySet()) {
                LOGGER.trace("term: |" + t + "| -- frequ: " + termFreqMap.get(t));
            }
        }
       
        return 1000f;
    }

    /**
     * Compute a custom score by the subQuery score and the ValueSourceQuery
     * score. <p> Subclasses can override this method to modify the custom
     * score. <p> If your custom scoring is different than the default herein
     * you should override at least one of the two customScore() methods. If the
     * number of ValueSourceQueries is always &lt; 2 it is sufficient to
     * override this customScore() method, which is simpler. <p> The default
     * computation herein is a multiplication of the two scores:
     * <pre>
     *     ModifiedScore = subQueryScore * valSrcScore
     * </pre>
     *
     * @param doc id of scored doc.
     * @param subQueryScore score of that doc by the subQuery.
     * @param valSrcScore score of that doc by the ValueSourceQuery.
     * @return custom score.
     */
    public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
        System.out.println("Scoring document: " + doc);
        return 1000f;

    }

    /**
     * Explain the custom score. Whenever overriding
     * {@link #customScore(int, float, float[])}, this method should also be
     * overridden to provide the correct explanation for the part of the custom
     * scoring.
     *
     * @param doc doc being explained.
     * @param subQueryExpl explanation for the sub-query part.
     * @param valSrcExpls explanation for the value source part.
     * @return an explanation for the custom score
     */
    public Explanation customExplain(int doc, Explanation subQueryExpl, Explanation valSrcExpls[]) throws IOException {
        if (valSrcExpls.length == 1) {
            return customExplain(doc, subQueryExpl, valSrcExpls[0]);
        }
        if (valSrcExpls.length == 0) {
            return subQueryExpl;
        }
        float valSrcScore = 1;
        for (Explanation valSrcExpl : valSrcExpls) {
            valSrcScore *= valSrcExpl.getValue();
        }
        Explanation exp = new Explanation(valSrcScore * subQueryExpl.getValue(), "custom score: product of:");
        exp.addDetail(subQueryExpl);
        for (Explanation valSrcExpl : valSrcExpls) {
            exp.addDetail(valSrcExpl);
        }
        return exp;
    }

    /**
     * Explain the custom score. Whenever overriding
     * {@link #customScore(int, float, float)}, this method should also be
     * overridden to provide the correct explanation for the part of the custom
     * scoring.
     *
     * @param doc doc being explained.
     * @param subQueryExpl explanation for the sub-query part.
     * @param valSrcExpl explanation for the value source part.
     * @return an explanation for the custom score
     */
    public Explanation customExplain(int doc, Explanation subQueryExpl, Explanation valSrcExpl) throws IOException {
        float valSrcScore = 1;
        if (valSrcExpl != null) {
            valSrcScore *= valSrcExpl.getValue();
        }
        Explanation exp = new Explanation(valSrcScore * subQueryExpl.getValue(), "custom score: product of:");
        exp.addDetail(subQueryExpl);
        exp.addDetail(valSrcExpl);
        return exp;
    }
}
