/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.SolrUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class TestSolrConfig extends EmptySolrTestCase {

  @Test
  public void testStopWords() throws SolrServerException, IOException {
    LOGGER.info("TF for raw content for a BODY field in schema");
    HashMap<String, Integer> tfs = SolrUtils.getTermFrequencyMapForContent(solrServer, "content_srch", "and");
    for (Map.Entry<String, Integer> tf : tfs.entrySet()) {
      LOGGER.info("\t" + tf.getKey() + "\t" + tf.getValue());
    }
    assertTrue("We shoudl no have a single word in tf", tfs.size() == 0);
  }

  @Test
  public void testStopWords2() throws SolrServerException, IOException {
    LOGGER.info("TF for raw content for a BODY field in schema");
    HashMap<String, Integer> tfs = SolrUtils.getTermFrequencyMapForContent(solrServer, "content_srch", "love and sun");
    for (Map.Entry<String, Integer> tf : tfs.entrySet()) {
      LOGGER.info("\t" + tf.getKey() + "\t" + tf.getValue());
    }
    assertTrue("We shoudl no have a single word in tf", tfs.size() == (2));
  }

}
