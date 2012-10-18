/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gamars
 */
public class SolrUtils {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SolrUtils.class);

    private static String makeTempDocument(SolrServer server, String fieldName, String content) throws SolrServerException, IOException {
        SolrInputDocument doc = new SolrInputDocument();
        String id = System.currentTimeMillis() + "";
        doc.addField("id", id);
        doc.addField(fieldName, content);
        server.add(doc);
        server.commit();
        return id;
    }
    
    public static HashMap<String, Integer> getTermFrequencyMapForContent(SolrServer server, String fieldName, String content) throws SolrServerException, IOException {
        String id = makeTempDocument(server, fieldName, content);
        HashMap<String, Integer> vector = getTermFrequencyMapForQuery(server, fieldName, "id:"+id).get(id);
        server.deleteById(id);
        server.commit();
        return vector;
        
    }

    public static Map<String, HashMap<String, Integer>> getTermFrequencyMapForQuery(SolrServer server, String fieldName, String query) throws SolrServerException {

        Map<String, HashMap<String, Integer>> vectors = new HashMap<String, HashMap<String, Integer>>();

        LOGGER.info("Getting TF for [" + fieldName + "] with q=" + query);
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("qt", "/tvrh");
        params.set("tv.tf", "true");
        params.set("tv.fl", fieldName);
        params.set("q", query);
        QueryResponse response = server.query(params);
        NamedList termVectors = (NamedList) response.getResponse().get("termVectors");

        for (int i = 1; i < termVectors.size(); i++) {
            NamedList doc = (NamedList) termVectors.getVal(i);
            String id = (String) doc.get("uniqueKey");
            NamedList tf = (NamedList) doc.get(fieldName);
            HashMap<String, Integer> tfmap = new HashMap<String, Integer>();
            for (int tfi = 0; tfi < tf.size(); tfi++) {
                String term = tf.getName(tfi);
                Integer count = (Integer) ((NamedList) tf.getVal(tfi)).get("tf");
                tfmap.put(term, count);
            }
            vectors.put(id, tfmap);
        }

        return vectors;
    }
}
