/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.core.CoreContainer;

/**
 *
 * @author gamars
 */
public class ExportTest extends TestCase {

    SolrServer server;

    public ExportTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        System.setProperty("solr.solr.home", "./src/test/resources");
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
        query.setParam("defType", "sense");
        QueryResponse response = server.query(query);
        assertTrue("Query has no results!", response.getResults().getNumFound() == 45);
    }
    
    public void testSenseQuery() throws SolrServerException {
        SolrQuery query = new SolrQuery("information");
        query.setParam("defType", "sense");
        QueryResponse response = server.query(query);
        assertTrue("Query has no results!", response.getResults().getNumFound() == 45);
    }
}
