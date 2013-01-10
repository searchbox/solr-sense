/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.lucene;

import com.searchbox.math.DoubleFullVector;
import com.searchbox.math.RealTermFreqVector;
import com.searchbox.sense.CognitiveKnowledgeBase;
import java.io.IOException;
import java.util.HashMap;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.search.Explanation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gamars
 */
public class SenseScoreProvider extends CustomScoreProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SenseScoreProvider.class);
    
    private final CognitiveKnowledgeBase ckb;
    private final DoubleFullVector qvector;
    private final RealTermFreqVector qtfidf;
    private final float senseWeight;
    private final String senseField;
    private HashMap <Integer,Float> scoreCache= new HashMap();
    
    
    SenseScoreProvider(AtomicReaderContext context, String senseField,
            CognitiveKnowledgeBase ckb, DoubleFullVector qvector, RealTermFreqVector qtfidf, float ckbWeight) {
        super(context);
        this.ckb = ckb;
        this.qvector = qvector;
        this.senseWeight = ckbWeight;
        this.senseField = senseField;
        this.qtfidf =qtfidf;
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

    @Override
    public float customScore(int doc, float subQueryScore, float valSrcScores[]) throws IOException {
        
        
        Float finalscore = scoreCache.get(doc);
        //System.out.println("Custom score on:\t"+doc);
        if(finalscore!=null) {
          //  System.out.println("Custom score on:\t"+doc+"\tfrom cache!");
            return finalscore;
        }
        Terms terms = context.reader().getTermVector(doc, this.senseField);
        RealTermFreqVector rtfv= new RealTermFreqVector(terms);

        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("Evaluating Document with TF size: " + rtfv.getSize());
        }
        
        if(LOGGER.isTraceEnabled()){
            for (int zz=0;zz<rtfv.getSize();zz++) {
                LOGGER.trace("term: |" + rtfv.getTerms()[zz] + "| -- frequ: " + rtfv.getFreqs()[zz]);
            }
        }
        
        
        double ckbscore=0;
        double idfscore=0;
        
        if (senseWeight != 0.0) {
            DoubleFullVector dvector = ckb.getFullCkbVector(rtfv).getUnitVector();
            ckbscore = dvector.getDistance(qvector);
             if(LOGGER.isTraceEnabled())
                LOGGER.trace("ckbscore: " + ckbscore);
        }
        if (senseWeight != 1.0) {
            RealTermFreqVector dtfidf =ckb.getTfIdfVector(rtfv).getUnitVector();
            idfscore= dtfidf.getDistance(qtfidf);
             if(LOGGER.isTraceEnabled())
                LOGGER.trace("idfscore: " + idfscore);
        }
        
         finalscore=(float) (senseWeight*(2-ckbscore)+(1-senseWeight)*(2-idfscore));
         if(LOGGER.isTraceEnabled())
            LOGGER.trace("Final score "+ finalscore);
        scoreCache.put(doc, finalscore);
        return finalscore; 
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
    @Override
    public float customScore(int doc, float subQueryScore, float valSrcScore) throws IOException {
        LOGGER.trace("Scoring document.....?! returnung 1k " + doc);
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
    @Override
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
    @Override
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
