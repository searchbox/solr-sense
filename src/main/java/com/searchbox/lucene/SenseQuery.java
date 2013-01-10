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
import org.apache.solr.schema.FieldType;
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
    private float senseWeight = .8f;

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
        FieldType type=indexSchema.getField(fieldName).getType();
        LOGGER.debug("Using this type:\t"+type.getTypeName());
        return type.getAnalyzer();
    }


    public SenseQuery(final RealTermFreqVector rtfv, final String senseField, final String ckbID,float senseWeight, final List<Query> filters ) {
        super(generateLuceneQuery(rtfv.getTerms(), senseField, filters));
        this.senseField = senseField;
        this.senseWeight = senseWeight;
        //TODO shoul be getting a CKB by some clever method
        this.ckb = SenseQParserPlugin.getCKBbyID(ckbID);
        this.rtfv = rtfv;

        //always compute these, even if senseWeight is 0 or 1 because we can change the value later and it will be null causing error
        this.qvector = ckb.getFullCkbVector(rtfv).getUnitVector();
        this.qtfidf = ckb.getTfIdfVector(rtfv).getUnitVector();

    }
    
    @Override
    protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
        LOGGER.debug("Setting up custom score provider.");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Overriding ScoreProvider for IndexReader " + context);
        }
        LOGGER.debug("Using senseWeight:\t"+senseWeight);
        return new SenseScoreProvider(context, senseField, ckb, qvector, qtfidf,(float) senseWeight);
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

    public void setSenseWeight(float senseWeight) {
        this.senseWeight = senseWeight;
    }

    public float getSenseWeight() {
        return this.senseWeight;
    }
}