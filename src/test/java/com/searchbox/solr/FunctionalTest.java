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
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;

import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;


/**
 *
 * @author gamars
 */
public class FunctionalTest extends EmptySolrTestCase {

   
    @Ignore
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


        SolrInputDocument doc1 = new SolrInputDocument();
        doc1.addField("id", System.currentTimeMillis() + "");
        doc1.addField("content_srch", "aasdf1 pheser ccpvdz ccpvdz snaploop snaploop snaploop preconnect preconnect preconnect preconnect poseinvari poseinvari poseinvari poseinvari poseinvari mulatta mulatta mulatta mulatta mulatta mulatta twopap twopap twopap twopap twopap twopap twopap flexibl flexibl flexibl flexibl flexibl flexibl flexibl flexibl fossil fossil fossil fossil fossil fossil fossil fossil fossil group group group group group group group group group group ");

        solrServer.add(doc1);
        solrServer.commit();

        SolrQuery query = new SolrQuery("aasdf1  httplglepflchteammwadaextensionsadaextensionshtml brainbodyrobot brainbodyrobot highand highand highand srecarg srecarg srecarg srecarg nontrap nontrap nontrap nontrap nontrap steamtocarbon steamtocarbon steamtocarbon steamtocarbon steamtocarbon steamtocarbon chote chote chote chote chote chote chote multiphononassist multiphononassist multiphononassist multiphononassist multiphononassist multiphononassist multiphononassist multiphononassist livetim livetim livetim livetim livetim livetim livetim livetim livetim macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron ");
        query.setParam("defType", "sense");
        query.setParam(SenseParams.SENSE_WEIGHT, "0.1");
        query.setFields("*","score");
        QueryResponse response = solrServer.query(query);
        SolrDocumentList doclist = response.getResults();
        for(int i =0; i<doclist.getNumFound(); i++){
            SolrDocument result = doclist.get(i);
            LOGGER.info("Document score: " + result.getFieldValue("score"));
        }
        assertTrue("Query has no results!", response.getResults().getNumFound() == 1);
    }
}
