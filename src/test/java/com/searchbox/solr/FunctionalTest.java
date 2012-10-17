/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.commons.params.SenseParams;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

/**
 *
 * @author gamars
 */
public class FunctionalTest extends TestCase {

    SolrServer server;

    public FunctionalTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty("solr.solr.home", "./src/test/resources/solr_empty");
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer coreContainer = initializer.initialize();
        this.server = new EmbeddedSolrServer(coreContainer, "pubmed");
        server.deleteByQuery("*:*");
        server.commit();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        server.deleteByQuery("*:*");
        server.commit();
        server.shutdown();
    }

    public void testSimpleSense() throws SolrServerException, IOException {


        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", System.currentTimeMillis() + "");
        doc.addField("content_srch", "Hello World");

        server.add(doc);
        server.commit();

        SolrQuery query = new SolrQuery("hello");
        query.setParam("defType", "sense");
        QueryResponse response = server.query(query);
        
        assertTrue("Query has no results!", response.getResults().getNumFound() == 1);
    }
    
    public void testSenseWeight() throws SolrServerException, IOException {


        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", System.currentTimeMillis() + "");
        doc.addField("content_srch", "Hello World");

        server.add(doc);
        server.commit();

        SolrQuery query = new SolrQuery("hello");
        query.setParam("defType", "sense");
        query.setParam(SenseParams.SENSE_WEIGHT, "0.1");
        QueryResponse response = server.query(query);
        
        assertTrue("Query has no results!", response.getResults().getNumFound() == 1);
    }
}
