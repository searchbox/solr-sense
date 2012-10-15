/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;

/**
 *
 * @author gamars
 */
public class Test {
  
  public static void main(String... args) throws IOException, ParserConfigurationException, SAXException, SolrServerException{
  
  
  System.setProperty("solr.solr.home", "/opt/solr/");
  CoreContainer.Initializer initializer = new CoreContainer.Initializer();
  CoreContainer coreContainer = initializer.initialize();
  EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "pubmed");
  
  SolrQuery query = new SolrQuery("information");
  query.setParam("defType", "sense");
  
  SolrResponse response = server.query(query);  
  
    
  System.out.println("DOOOONE");
  
  server.shutdown();
    System.exit(-1);
    
  }
  
}
