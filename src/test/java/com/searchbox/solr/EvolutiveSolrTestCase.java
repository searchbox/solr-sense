/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;


/**
 *
 *
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
