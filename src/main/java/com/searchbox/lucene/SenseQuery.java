//package com.searchbox.lucene;
//
//import java.io.IOException;
//import org.apache.lucene.index.Fields;
//
//
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.search.Query;
//
//
//import org.apache.lucene.queries.CustomScoreQuery;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class SenseQuery extends CustomScoreQuery {
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