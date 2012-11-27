package com.searchbox.lucene;

import com.searchbox.math.DoubleFullVector;
import com.searchbox.math.RealTermFreqVector;
import com.searchbox.sense.CognitiveKnowledgeBase;
import com.searchbox.solr.SenseQParserPlugin;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SenseQuery extends CustomScoreQuery {
        public static final Logger LOGGER = LoggerFactory.getLogger(SenseQuery.class);
    private String senseField;
    private final RealTermFreqVector rtfv;
    private final CognitiveKnowledgeBase ckb;
    private final RealTermFreqVector qtfidf;
    private final DoubleFullVector qvector;
    private double senseWeight = 1.0;

    private static Query generateLuceneQuery(final String[] terms, final String senseField, final List<Query> filters) {

        BooleanQuery topLevelQuery = new BooleanQuery();
        if (terms != null) {
            for (String term : terms) {
                topLevelQuery.add(new BooleanClause(new TermQuery(new Term(senseField, term)), BooleanClause.Occur.SHOULD));
            }
        }

        if (filters != null) {
            for (Query filter : filters) {
                topLevelQuery.add(new BooleanClause(filter, BooleanClause.Occur.MUST));
            }
        }

        return topLevelQuery;
    }

    public static SenseQuery SenseQueryForDocument(RealTermFreqVector rtfv, final IndexReader ir, final String senseField, double senseWeight, final List<Query> filters) {
        return new SenseQuery(rtfv, senseField,
                generateLuceneQuery(rtfv.getTerms(), senseField, filters),
                senseWeight);

    }

    /*public static SenseQuery SenseQueryForDocument(final int id, final IndexReader ir, final String senseField, double senseWeight, final List<Query> filters) {

        final Fields vectors;
        final Terms vector;
        try {
            vectors = ir.getTermVectors(id);
            if (vectors != null) {
                vector = vectors.terms(senseField);
            } else {
                vector = null;
            }

            // field does not store term vector info
            if (vector == null) {
                throw new RuntimeException("No termVectorFrequency available for field: " + senseField);
            }

            RealTermFreqVector rtfv = SenseScoreProvider.getTermFreqmapfromTermsContainer(vector);


            return new SenseQuery(rtfv, senseField,
                    generateLuceneQuery(rtfv.getTerms(), senseField, filters),
                    senseWeight);
        } catch (IOException ex) {
            throw new RuntimeException("Could not generate SenseQuery for document[" + id + "]. Exception: " + ex.getMessage());
        }
    }*/

    public static Analyzer getAnalyzerForField(final IndexSchema indexSchema, final String fieldName) {
        //TODO somehow check that field exists.
        return indexSchema.getField(fieldName).getType().getAnalyzer();
    }
    
    public static Map<String, Float> RealTermFreqVectorFromText(final String queryText, Analyzer analyzer) {
        Map<String, Float> termFreqMap = new HashMap<String, Float>();

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
                Float cnt = termFreqMap.get(word);
                if (cnt == null) {
                    termFreqMap.put(word, new Float(1));
                } else {
                    termFreqMap.put(word, cnt + 1);
                }
            }

            ts.end();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(SenseQuery.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return termFreqMap;
    }
    
    public static SenseQuery SenseQueryForText(final String queryText, String senseField, Analyzer analyzer, double senseWeight, final List<Query> filters) {
        Map<String, Float> termFreqMap = RealTermFreqVectorFromText(queryText,analyzer);
        return new SenseQuery(new RealTermFreqVector(termFreqMap), senseField,
                generateLuceneQuery(termFreqMap.keySet().toArray(new String[0]), senseField, filters),
                senseWeight);
    }


    public SenseQuery(final RealTermFreqVector rtfv, String senseField, final Query luceneQuery, double senseWeight) {
        super(luceneQuery);
        this.senseField = senseField;
        this.senseWeight = senseWeight;
        //TODO shoul be getting a CKB by some clever method
        this.ckb = SenseQParserPlugin.ckbByID.get("1");
        this.rtfv = rtfv;

        //always compute these, even if senseWeight is 0 or 1 because we can change the value later and it will be null causing error
        this.qvector = ckb.getFullCkbVector(rtfv).getUnitVector();
        this.qtfidf = ckb.getTfIdfVector(rtfv).getUnitVector();

    }

    @Override
    protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
        System.out.println("Setting up custom score provider.");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Overriding ScoreProvider for IndexReader " + context);
        }
        return new SenseScoreProvider(context, senseField, ckb, qvector, qtfidf, senseWeight);
    }

    public String getSenseField() {
        return senseField;
    }

    public void setSenseFields(String senseField) {
        this.senseField = senseField;
    }

    @Override
    public String toString() {
        return "sense with TF size: " + this.rtfv.getSize();
    }

    public void setSenseWeight(double senseWeight) {
        this.senseWeight = senseWeight;
    }

    public double getSenseWeight() {
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
