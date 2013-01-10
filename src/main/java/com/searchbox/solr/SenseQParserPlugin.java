/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.sense.CognitiveKnowledgeBase;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.ExtendedDismaxQParserPlugin;
import org.apache.solr.search.QParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gamars
 */
public class SenseQParserPlugin extends ExtendedDismaxQParserPlugin {

    private static HashMap<String, CognitiveKnowledgeBase> ckbByID = new HashMap<String, CognitiveKnowledgeBase>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SenseQParserPlugin.class);
    public static final String NAME = "sense";
    
    private SolrParams defaults;

    
    public static CognitiveKnowledgeBase getCKBbyID(String ckbID){
        CognitiveKnowledgeBase ckb=null;
        try{
             ckb=ckbByID.get(ckbID);
        }
        catch (Exception e){ // should be a more accurate exception catch
            LOGGER.error("Missing CKB with ckbID\t"+ckbID);
        }
        finally{
            return ckb;
        }
    }
    
    @Override
    public QParser createParser(String query, SolrParams localParams, SolrParams params, SolrQueryRequest sqr) {
        QParser parentQparser = super.createParser(query, localParams, params, sqr);
       return parentQparser;
    }

    @Override
    public void init(NamedList nl) {  //need this for init of CKBs
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
                        Float.parseFloat(params.get("certaintyValue")),
                        Float.parseFloat(params.get("maximumDistance")));
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