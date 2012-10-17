/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import java.io.IOException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import junit.framework.TestCase;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.core.CoreContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;
import org.junit.Before;


/**
 *
 * @author gamars
 */
public abstract class EvolutiveSolrTestCase {

    protected static final Logger LOGGER = LoggerFactory.getLogger(EvolutiveSolrTestCase.class);
    protected static SolrServer solrServer;

    @BeforeClass
    public static void solrDataTestSetup() throws IOException, ParserConfigurationException, SAXException, SolrServerException, InterruptedException {
        LOGGER.info("Setting up Solr Server");
        System.setProperty("solr.solr.home", "./src/test/resources/solr");
        CoreContainer.Initializer initializer = new CoreContainer.Initializer();
        CoreContainer coreContainer = initializer.initialize();
        solrServer = new EmbeddedSolrServer(coreContainer, "pubmed");
        
        solrServer.deleteByQuery("*:*");
        solrServer.commit();
    }
    
    @AfterClass
    public static void solrDataTestTeardown() throws SolrServerException, IOException {
        solrServer.deleteByQuery("*:*");
        solrServer.commit();
        solrServer.shutdown();
        solrServer = null;
    }
}
