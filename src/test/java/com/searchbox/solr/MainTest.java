/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;


import com.searchbox.sense.CognitiveKnowledgeBase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CoreContainer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 *
 * @author gamars
 */
public class MainTest extends DataSolrTestCase {
    SolrServer server;
    
    @Test
    public void testSenseQuery() throws SolrServerException {
        SolrQuery query = new SolrQuery("information");
        query.setParam("defType", "sense");
        QueryResponse response = solrServer.query(query);
        LOGGER.info("Content" + response.getResults().iterator().next().getFieldValue("content_srch"));
        //assertTrue("Query has no results!", response.getResults().getNumFound() == 45);
        
        
          
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("qt", "/admin/luke");
        params.set("fl", "*");
        params.set("numTerms", "20");
        
//        LukeRequest req = new LukeRequest();
//        req.setNumTerms(20);
        
        SolrResponse resp2 = solrServer.query(params);
        resp2.getResponse().get("index");
        System.out.println("INDEX: " + resp2.getResponse().get("index"));
        System.out.println("INDEX: " + ((NamedList)resp2.getResponse().get("fields")).get("_version_"));
    }
}
