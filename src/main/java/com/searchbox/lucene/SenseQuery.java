package com.searchbox.lucene;

import com.searchbox.math.DoubleFullVector;
import com.searchbox.sense.CognitiveKnowledgeBase;
import com.searchbox.solr.SenseQParserPlugin;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Fields;


import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.search.Query;


import org.apache.lucene.queries.CustomScoreQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SenseQuery extends CustomScoreQuery {

    public static final Logger LOGGER = LoggerFactory.getLogger(SenseQuery.class);
    private String queryText;
    private String senseField;
    private Analyzer analyzer;
    private CognitiveKnowledgeBase ckb;
    private DoubleFullVector qvector;
    private double senseWeight = 1.0;

    public SenseQuery(final String queryText, String senseField, Analyzer analyzer, final Query luceneQuery) {
        super(luceneQuery);
        this.queryText = queryText;
        this.senseField = senseField;
        this.analyzer = analyzer;
        //TODO shoul be getting a CKB by some clever method
        this.ckb = SenseQParserPlugin.ckbByID.get("1");



        Map<String, Integer> termFreqMap = new HashMap<String, Integer>();
        try {
            TokenStream ts = analyzer.tokenStream("", new StringReader(queryText));

            int tokenCount = 0;
            // for every token
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                String word = termAtt.toString();
                tokenCount++;


                // increment frequency
                Integer cnt = termFreqMap.get(word);
                if (cnt == null) {
                    termFreqMap.put(word, new Integer(1));
                } else {
                    cnt += 1;
                }
            }
            ts.end();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SenseQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.qvector = ckb.getFullCkbVector(termFreqMap);
    }

    @Override
    protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
        System.out.println("Setting up custom score provider.");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Overriding ScoreProvider for IndexReader " + context);
        }
        return new SenseScoreProvider(context, senseField, ckb, qvector, senseWeight);
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getSenseField() {
        return senseField;
    }

    public void setSenseFields(String senseField) {
        this.senseField = senseField;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public String toString() {
        return "sense: " + this.queryText;
    }

    public void setSenseWeight(double senseWeight) {
        this.senseWeight = senseWeight;
    }
    
    public double getSenseWeight(){
        return this.senseWeight;
    }
}
//
//    public static final Logger LOGGER = LoggerFactory.getLogger(SenseQuery.class);
//    public static final int DEFAULT_COUNT_CUTOFF = 1000000;
//    private static final float BASIS = 10.0f;
////    private final CognitiveKnowledgeBase ckb;
////    private FullCkbVector queryCKBVector;
////    private RealTermFreqVector queryTfIdf;
////
//    public SenseQuery(final Fields relatedFields, final Query luceneQuery, final CognitiveKnowledgeBase ckb) {
//        super(luceneQuery);
//        this.ckb = ckb;
//
//        switch (ckb.getProfile().getScoringSchema()) {
//            case CKB:
//                this.queryCKBVector = ckb.getFullCkbVector(queryTermFreqVector).getUnitVector();
//                this.queryTfIdf = null;
//                break;
//            case TFIDF:
//                this.queryTfIdf = ckb.getTfIdfVector(queryTermFreqVector);
//                if (queryTfIdf == null) {
//                    LOGGER.warn("Query don't have content!");
//                }
//                this.queryTfIdf.buildIndex();
//                this.queryCKBVector = null;
//                break;
//            case CKB_TFIDF:
//                this.queryCKBVector = ckb.getFullCkbVector(queryTermFreqVector).getUnitVector();
//                this.queryTfIdf = ckb.getTfIdfVector(queryTermFreqVector);
//                if (queryTfIdf == null) {
//                    LOGGER.warn("Query don't have content!");
//                }
//                this.queryTfIdf.buildIndex();
//                break;
//            default:
//                throw new RuntimeException("Scoring schema [" + ckb.getProfile().getScoringSchema() + "] is not supported.");
//        }
//
//        if (LOGGER.isDebugEnabled()) {
//            LOGGER.debug("Created qvector for CKB - " + ckb.getProfile().getName() + " with score: " + queryCKBVector.getNorm());
//        }
//    }
//
//    @Override
//    protected CustomScoreProvider getCustomScoreProvider(final IndexReader reader) throws IOException {
//        if (LOGGER.isDebugEnabled()) {
//            LOGGER.debug("Overriding ScoreProvider for IndexReader " + reader);
//        }
//        return new SenseQuery.SenseScoreProvider(reader.clone(true));
//    }
//
//    class SenseScoreProvider extends CustomScoreProvider {
//
//        public SenseScoreProvider(final IndexReader reader) {
//            super(reader);
//        }
//
//        public float computeSenseScore(final int docNumber) {
//            if (reader.isDeleted(docNumber)) {
//                return 0.0f;
//            }
//
//            try {
//                final TermFreqVector tfv = reader.getTermFreqVector(docNumber, DocumentEntity.LUCENE_CONTENT_VALUE_FIELD);
//                switch (ckb.getProfile().getScoringSchema()) {
//                    case CKB: {
//                        final float ckbScore = getCkbScore(tfv);
//                        LOGGER.debug("score: " + ckbScore + " ckb: " + ckbScore);
//                        return BASIS - ckbScore;
//                    }
//                    case TFIDF: {
//                        final float tfidfScore = getTfidfScore(docNumber, tfv);
//                        LOGGER.debug("score: " + tfidfScore + " tfidf: " + tfidfScore);
//                        return BASIS - tfidfScore;
//                    }
//                    case CKB_TFIDF: {
//                        final double weightCkbSpace = ckb.getProfile().getWeightCkbSpace();
//                        final float ckbScore = getCkbScore(tfv);
//                        final float tfidfScore = getTfidfScore(docNumber, tfv);
//                        final float mixScore = BASIS - (float) (weightCkbSpace * ckbScore + (1 - weightCkbSpace) * tfidfScore);
//                        LOGGER.debug("score: " + mixScore + " ckb: " + ckbScore + " tfidf: " + tfidfScore);
//                        return mixScore;
//                    }
//                    default:
//                        throw new RuntimeException("Scoring Schema [" + ckb.getProfile().getScoringSchema() + "] is not supported.");
//                }
//            } catch (IOException e) {
//                LOGGER.error("Problem with calculation score.", e);
//                return 0.0f;
//            }
//        }
//
//        private float getCkbScore(final TermFreqVector tfv) {
//            final FullCkbVector dvector = ckb.getFullCkbVector(tfv);
//            return (float) dvector.getUnitVector().getDistance(queryCKBVector);
//        }
//
//        private float getTfidfScore(final int docNumber, final TermFreqVector tfv) {
//            final RealTermFreqVector dvector = ckb.getTfIdfVector(tfv);
//            if (dvector == null) {
//                LOGGER.warn("We have document [" + docNumber + "] without content!");
//                return queryTfIdf != null ? BASIS : 0.0f;
//            }
//            if (queryTfIdf == null) {
//                return BASIS;
//            }
//            return dvector.calculateDistance(queryTfIdf);
//        }
//
//        @Override
//        public float customScore(final int docNumber, final float subQueryScore, final float valSrcScore) throws IOException {
//            return computeSenseScore(docNumber);
//        }
//
//        @Override
//        public float customScore(final int doc, final float subQueryScore, final float valSrcScores[]) throws IOException {
//            if (valSrcScores.length == 0) {
//                return customScore(doc, subQueryScore, 1f);
//            }
//            if (valSrcScores.length == 1) {
//                return customScore(doc, subQueryScore, valSrcScores[0]);
//            }
//            return 1f;//subQueryScore * valSrcScore;
//        }
//    }
//}
