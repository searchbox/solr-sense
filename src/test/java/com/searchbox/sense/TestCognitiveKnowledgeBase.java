/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.SolrUtils;
import com.searchbox.math.DoubleFullVector;
import com.searchbox.solr.EmptySolrTestCase;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author gamars
 */
public class TestCognitiveKnowledgeBase extends EmptySolrTestCase {

    private static CognitiveKnowledgeBase CKB;

    @BeforeClass
    public static void setUp() throws Exception {
        TestCognitiveKnowledgeBase.CKB = CognitiveKnowledgeBase.loadSparseCKB("test",
                "./src/test/resources/CKB/pubmed/", "pubmed.cache",
                "pubmed.idflog", "pubmed.tdic", 1.0, 1.0);
    }

    @Test
    public void testLoadingCKB() {
        assertNotNull(CKB);
        assertTrue("No terms has been loaded", CKB.getTerms().size() > 0);
        assertTrue("CKB has no dimensions", CKB.getDimentionality() > 0);
    }

    @Test
    public void testTFcreation() throws SolrServerException, IOException {
        HashMap<String, Integer> ground_truth = new HashMap<String, Integer>();
        ground_truth.put("dog", 1);
        ground_truth.put("fish", 4);
        ground_truth.put("happi", 9);
        ground_truth.put("internet", 1);
        ground_truth.put("larg", 1);
        ground_truth.put("parti", 1);
        ground_truth.put("sad", 1);
        ground_truth.put("small", 3);
        ground_truth.put("studi", 2);
        ground_truth.put("swam", 1);
        ground_truth.put("swim", 2);
        ground_truth.put("unknown", 1);
        ground_truth.put("jump", 3);


        SolrInputDocument doc = new SolrInputDocument();
        String id = System.currentTimeMillis() + "";
        doc.addField("id", id);
        doc.addField("content_srch", "jump jumping jumped swim swimming swam dog fish fish fish happy happy happy happy happy happy happy happy happy sad large small small small unknown party internet studying fish study and the ");
        solrServer.add(doc);
        solrServer.commit();
        
        LOGGER.info("Getting TF for ONE document (q=id:" + id + ")");
        Map<String, HashMap<String, Integer>> tfms = SolrUtils.getTermFrequencyMapForQuery(solrServer, "content_srch", "id:" + id);
        for (String sid : tfms.keySet()) {
            LOGGER.info("TF for document id#" + sid);
            for (Entry<String, Integer> tf : tfms.get(sid).entrySet()) {
                LOGGER.info("\t" + tf.getKey() + "\t" + tf.getValue());
                 assertTrue("TF stemming doesn't match ground truth! [ "+tf.getKey()+" ] ", ground_truth.get(tf.getKey()) == tf.getValue());
            }
        }
    }

    @Test
    public void testCKBVector() {
        Map<String, Integer> tf = new HashMap<String, Integer>();
        tf.put("hello", 1);
        tf.put("world", 1);
        DoubleFullVector vector = CKB.getFullCkbVector(tf);
        System.out.println("Got vector of dimension: " + vector.getDimension());
        assertTrue("Vector has null dimention", vector.getDimension() > 0);
        assertTrue("Vector has different dim than CKB", vector.getDimension() == CKB.getDimentionality());

        for (double d : vector.getData()) {
            System.out.print(d + ", ");
        }
        System.out.println();
    }
}
