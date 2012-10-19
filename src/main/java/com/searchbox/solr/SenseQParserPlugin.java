/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.commons.params.SenseParams;
import com.searchbox.lucene.SenseQuery;
import com.searchbox.sense.CognitiveKnowledgeBase;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.ExtendedDismaxQParserPlugin;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.util.SolrPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gamars
 */
public class SenseQParserPlugin extends ExtendedDismaxQParserPlugin {

    public static HashMap<String, CognitiveKnowledgeBase> ckbByID = new HashMap<String, CognitiveKnowledgeBase>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SenseQParserPlugin.class);
    public static final String NAME = "sense";
    public static final String DEFAULT_SENSE_FIELD = "content";
    
    private SolrParams defaults;

    @Override
    public QParser createParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest sqr) {
        QParser parentQparser = super.createParser(query, localParams, params, sqr);
       
        SenseQParser SQParser = new SenseQParser(parentQparser, query,  SolrParams.wrapDefaults(localParams, defaults), params, sqr);
        return SQParser;
    }

    public void init(NamedList nl) {
        super.init(nl);

        LOGGER.info("#### eagerly initializing CognitiveKnowledgeBases");
        List lst = nl.getAll("ckbs");
        for (Iterator<NamedList> it = lst.iterator(); it.hasNext();) {
            NamedList ckb = it.next();
            
            LOGGER.info("\tbuilding CKB#" + ckb.getName(0) + " with params: " + ckb.get(ckb.getName(0)));
            NamedList<String> params = (NamedList) ckb.get(ckb.getName(0));
            if (params.get("type").equals("SPARSE")) {
                CognitiveKnowledgeBase ckb_ = CognitiveKnowledgeBase.loadSparseCKB(
                        params.get("name"),
                        params.get("baseDirectory"), params.get("modelFile"),
                        //TODO shoudl be logical path
                        params.get("idfFile"), params.get("dictionaryFile"),
                        Double.parseDouble(params.get("certaintyValue")),
                        Double.parseDouble(params.get("maximumDistance")));
                ckbByID.put(ckb.getName(0), ckb_);
            }

        }
        
        Object o = nl.get("defaults");
        if (o != null && o instanceof NamedList) {
          defaults = SolrParams.toSolrParams((NamedList)o);
        }
        
        LOGGER.info("---- CKBs Initialization DONE");
    }
}

class SenseQParser extends QParser {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SenseQParserPlugin.class);
    private QParser parent;

    public SenseQParser(QParser parent, String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
        super(qstr, localParams, params, req);
        this.parent = parent;
    }

    private Analyzer getAnalyzerForField(final IndexSchema indexSchema, final String fieldName) {
        //TODO somehow check that field exists.
        return indexSchema.getField(fieldName).getType().getAnalyzer();
    }

    private Set<String> getQueryFields(final IndexSchema indexSchema, final SolrParams solrParams) {
        Map<String, Float> queryFields = SolrPluginUtils.parseFieldBoosts(solrParams.getParams(DisMaxParams.QF));
        if (queryFields.isEmpty()) {
            String df = QueryParsing.getDefaultField(indexSchema, solrParams.get(CommonParams.DF));
            if (df == null) {
                //TODO should fail elegantly
                df = SenseQParserPlugin.DEFAULT_SENSE_FIELD;
            }
            queryFields.put(df, 1.0f);
        }
        return queryFields.keySet();
    }

    private double getSenseWeight(final SolrParams localParams, final SolrParams solrParams){
        //TODO check if null then 1.0;
        String val;
        Double dval;
        if((val = solrParams.get(SenseParams.SENSE_WEIGHT))!=null)
            dval = Double.parseDouble(val);
        else if((val = localParams.get(SenseParams.SENSE_WEIGHT))!=null)
            dval = Double.parseDouble(val);
        else 
            dval = SenseParams.DEFAULT_SENSE_WEIGHT;
        
        LOGGER.info("%&%&%&%&%&%&%& Sense Weight: " + dval );
       
        return dval;
    }
    
    public Query parse() throws ParseException {
        Query q = parent.parse();
        SolrParams localParams = getLocalParams();
        SolrParams params = getParams();
        
        LOGGER.info("****** default: params: " + localParams);
        LOGGER.info("****** current: params: " + params);


        Set<String> fields = this.getQueryFields(req.getSchema(), SolrParams.wrapDefaults(localParams, params));
        
        
        //TODO assumption only one field for now. 
        String fieldName = fields.iterator().next();
        Analyzer analyzer = getAnalyzerForField(req.getSchema(), fieldName);
        
        
        
        SenseQuery query = new SenseQuery(this.qstr, fieldName, analyzer, q,getSenseWeight(localParams, params));
        return query;
    }
}
