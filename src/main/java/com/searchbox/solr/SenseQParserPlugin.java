/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.lucene.SenseQuery;
import com.searchbox.sense.CognitiveKnowledgeBase;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.ExtendedDismaxQParserPlugin;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gamars
 */
public class SenseQParserPlugin extends ExtendedDismaxQParserPlugin {

    private static HashMap<String, CognitiveKnowledgeBase> ckbByID = new HashMap<String, CognitiveKnowledgeBase>();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SenseQParserPlugin.class);
    public static final String NAME = "sense";

    @Override
    public QParser createParser(String query, SolrParams sp, SolrParams sp1, SolrQueryRequest sqr) {

        LOGGER.info("Here I get the query that I could Expand: " + query);

        LOGGER.info("Got CKB hash: " + ckbByID.get("1"));

        QParser parentQparser = super.createParser(query, sp1, sp1, sqr);

        SenseQParser qParser = new SenseQParser(parentQparser, query, sp1, sp1, sqr);


        LOGGER.info("Hey this is a searchbox query. Going to use sense!!!");
        return qParser;
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
        LOGGER.info("---- CKBs Initialization DONE");
    }
}
