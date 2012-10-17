/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

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
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        server.deleteByQuery("*:*");
        server.commit();
        server.shutdown();
    }

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    public void testMain() throws SolrServerException, IOException {


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
}
