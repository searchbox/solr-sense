/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.commons.params.SenseParams;
import java.io.IOException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 *
 * @author gamars
 */
public class FunctionalTest extends EmptySolrTestCase {

   
    @Test
    public void testSimpleSense() throws SolrServerException, IOException {


        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", System.currentTimeMillis() + "");
        doc.addField("content_srch", "Hello World");

        solrServer.add(doc);
        solrServer.commit();

        SolrQuery query = new SolrQuery("hello");
        query.setParam("defType", "sense");
        QueryResponse response = solrServer.query(query);
        
        assertTrue("Query has no results!", response.getResults().getNumFound() == 1);
    }
    
    @Test
    public void testSenseWeight() throws SolrServerException, IOException {


        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", System.currentTimeMillis() + "");
        doc.addField("content_srch", "Hello World");

        solrServer.add(doc);
        solrServer.commit();

        SolrQuery query = new SolrQuery("hello");
        query.setParam("defType", "sense");
        query.setParam(SenseParams.SENSE_WEIGHT, "0.1");
        QueryResponse response = solrServer.query(query);
        
        assertTrue("Query has no results!", response.getResults().getNumFound() == 1);
    }
}
