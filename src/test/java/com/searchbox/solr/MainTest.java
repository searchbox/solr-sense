/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.sense.CognitiveKnowledgeBase;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gamars
 */
public class MainTest extends TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);
    SolrServer server;

    public MainTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty("solr.solr.home", "./src/test/resources/solr_data");
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer coreContainer = initializer.initialize();
        this.server = new EmbeddedSolrServer(coreContainer, "pubmed");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        server.shutdown();
    }

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    public void testMain() throws SolrServerException {
        SolrQuery query = new SolrQuery("*");
        query.setParam("defType", "edismax");
        QueryResponse response = server.query(query);
        assertTrue("Query has no results!", response.getResults().getNumFound() == 45);
    }
    
    public void testSenseQuery() throws SolrServerException {
        SolrQuery query = new SolrQuery("information");
        query.setParam("defType", "sense");
        QueryResponse response = server.query(query);
        LOGGER.info("Content" + response.getResults().iterator().next().getFieldValue("content_srch"));
        assertTrue("Query has no results!", response.getResults().getNumFound() == 45);
        
    }
}
