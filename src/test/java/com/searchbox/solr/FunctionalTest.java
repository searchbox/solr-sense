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
    
    @Ignore
    public void testSenseWeightReturnsZeroForSelf() throws SolrServerException, IOException {


        SolrInputDocument doc1 = new SolrInputDocument();
        doc1.addField("id", System.currentTimeMillis() + "");
        doc1.addField("content_srch", "aasdf1 pheser ccpvdz ccpvdz snaploop snaploop snaploop preconnect preconnect preconnect preconnect poseinvari poseinvari poseinvari poseinvari poseinvari mulatta mulatta mulatta mulatta mulatta mulatta twopap twopap twopap twopap twopap twopap twopap flexibl flexibl flexibl flexibl flexibl flexibl flexibl flexibl fossil fossil fossil fossil fossil fossil fossil fossil fossil group group group group group group group group group group ");

        solrServer.add(doc1);
        solrServer.commit();
    
        SolrQuery query = new SolrQuery("aasdf1 pheser ccpvdz ccpvdz snaploop snaploop snaploop preconnect preconnect preconnect preconnect poseinvari poseinvari poseinvari poseinvari poseinvari mulatta mulatta mulatta mulatta mulatta mulatta twopap twopap twopap twopap twopap twopap twopap flexibl flexibl flexibl flexibl flexibl flexibl flexibl flexibl fossil fossil fossil fossil fossil fossil fossil fossil fossil group group group group group group group group group group ");
        query.setParam("defType", "sense");
        query.setParam(SenseParams.SENSE_WEIGHT, "0.1");
        query.setFields("*","score");
        QueryResponse response = solrServer.query(query);
        SolrDocumentList doclist = response.getResults();
        
        SolrDocument result = doclist.get(0);
        
        Double score = Double.parseDouble(result.getFieldValue("score").toString());
        LOGGER.info("Document score (Expecting 0): \t" +score );
        assertTrue("Query doesn't have 0 score!", score==0);
    }
    
    
    @Test
    public void testSenseWeightNotToSelf() throws SolrServerException, IOException {


        double[] weight = {.8, .1, 0, 1};
        double[] ground_truth = {1.373758274, 1.409156651, 1.414213562, 1.363644452};

        SolrInputDocument doc1 = new SolrInputDocument();
        doc1.addField("id", System.currentTimeMillis() + "");
        doc1.addField("content_srch", "aasdffffffffffffff pheser ccpvdz ccpvdz snaploop snaploop snaploop preconnect preconnect preconnect preconnect poseinvari poseinvari poseinvari poseinvari poseinvari mulatta mulatta mulatta mulatta mulatta mulatta twopap twopap twopap twopap twopap twopap twopap flexibl flexibl flexibl flexibl flexibl flexibl flexibl flexibl fossil fossil fossil fossil fossil fossil fossil fossil fossil group group group group group group group group group group ");

        solrServer.add(doc1);
        solrServer.commit();


        SolrQuery query = new SolrQuery("aasdffffffffffffff  httplglepflchteammwadaextensionsadaextensionshtml brainbodyrobot brainbodyrobot highand highand highand srecarg srecarg srecarg srecarg nontrap nontrap nontrap nontrap nontrap steamtocarbon steamtocarbon steamtocarbon steamtocarbon steamtocarbon steamtocarbon chote chote chote chote chote chote chote multiphononassist multiphononassist multiphononassist multiphononassist multiphononassist multiphononassist multiphononassist multiphononassist livetim livetim livetim livetim livetim livetim livetim livetim livetim macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron macroenviron ");
        query.setParam("defType", "sense");
        query.setFields("*", "score");

        for (int zz = 0; zz < weight.length; zz++) {
            query.setParam(SenseParams.SENSE_WEIGHT, ""+weight[zz]);
            QueryResponse response = solrServer.query(query);
            SolrDocumentList doclist = response.getResults();
            SolrDocument result = doclist.get(0);
            Double score = Double.parseDouble(result.getFieldValue("score").toString());
            LOGGER.info("Document score with senseWeight "+weight[zz]+" (Expecting "+ground_truth[zz]+"): \t" +score );
            assertTrue("Query has large difference from expected score! Expecting: "+ground_truth[zz]+"\tGot: "+score+"!", Math.abs(score-ground_truth[zz])<.0001);
        }
    }
    
    
}
