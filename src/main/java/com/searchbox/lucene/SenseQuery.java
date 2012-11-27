package com.searchbox.lucene;

import com.searchbox.math.DoubleFullVector;
import com.searchbox.math.RealTermFreqVector;
import com.searchbox.sense.CognitiveKnowledgeBase;
import com.searchbox.solr.SenseQParserPlugin;
import java.io.IOException;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.AtomicReaderContext;
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

    public static Analyzer getAnalyzerForField(final IndexSchema indexSchema, final String fieldName) {
        //TODO somehow check that field exists.
        return indexSchema.getField(fieldName).getType().getAnalyzer();
    }


    public SenseQuery(final RealTermFreqVector rtfv, final String senseField, double senseWeight, final List<Query> filters ) {
        super(generateLuceneQuery(rtfv.getTerms(), senseField, filters));
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