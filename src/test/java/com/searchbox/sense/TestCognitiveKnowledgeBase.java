/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.SolrUtils;
import com.searchbox.commons.params.SenseParams;
import com.searchbox.lucene.SenseQuery;
import com.searchbox.lucene.SenseScoreProvider;
import com.searchbox.math.DoubleFullVector;
import com.searchbox.solr.EmptySolrTestCase;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.store.Directory;
import org.apache.solr.analysis.SolrAnalyzer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.SolrIndexSearcher;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author gamars
 */
public class TestCognitiveKnowledgeBase extends EmptySolrTestCase {

    private CognitiveKnowledgeBase ckb;
    private SenseQuery sensequery;
    private SenseScoreProvider sensescoreprovider;
    

    @Ignore
    public void testLoadingCKB() {
        assertTrue("No terms has been loaded", ckb.getTerms().size() > 0);
        assertTrue("CKB has no dimensions", ckb.getDimentionality() > 0);
    }

    @Test
    public void testTFcreation() throws SolrServerException, IOException {
        SolrInputDocument doc = new SolrInputDocument();
        String id = System.currentTimeMillis() + "";
        doc.addField("id", id);
        doc.addField("content_srch", "Hello World");
        solrServer.add(doc);
        solrServer.commit();
        
        doc = new SolrInputDocument();
        id = System.currentTimeMillis() + "";
        doc.addField("id", id);
        doc.addField("content_srch", "Goodby night");
        solrServer.add(doc);
        solrServer.commit();

        LOGGER.info("Getting TF for all document (q=*:*)");
        Map<String, HashMap<String, Integer>> tfms = SolrUtils.getTermFrequencyMapForQuery(solrServer, "content_srch", "*:*");
        for(String sid:tfms.keySet()){
            LOGGER.info("TF for document id#"+sid);
            for(Entry<String, Integer> tf:tfms.get(sid).entrySet()){
                LOGGER.info("\t"+tf.getKey()+"\t"+tf.getValue());
            }
        }
        
        
        LOGGER.info("Getting TF for ONE document (q=id:"+id+")");
        tfms = SolrUtils.getTermFrequencyMapForQuery(solrServer, "content_srch", "id:"+id);
        for(String sid:tfms.keySet()){
            LOGGER.info("TF for document id#"+sid);
            for(Entry<String, Integer> tf:tfms.get(sid).entrySet()){
                LOGGER.info("\t"+tf.getKey()+"\t"+tf.getValue());
            }
        }
        
        LOGGER.info("TF for raw content for a CONTENT_SRCH field in schema");
        HashMap<String, Integer> tfs = SolrUtils.getTermFrequencyMapForContent(solrServer, "content_srch", "I like this method very much and will do!");
        for(Entry<String, Integer> tf:tfs.entrySet()){
            LOGGER.info("\t"+tf.getKey()+"\t"+tf.getValue());
        }
        
        LOGGER.info("TF for raw content for a CATEGORY field in schema");
        tfs = SolrUtils.getTermFrequencyMapForContent(solrServer, "category", "I like this method very much!");
        for(Entry<String, Integer> tf:tfs.entrySet()){
            LOGGER.info("\t"+tf.getKey()+"\t"+tf.getValue());
        }
        
        LOGGER.info("TF for raw content for a BODY field in schema");
        tfs = SolrUtils.getTermFrequencyMapForContent(solrServer, "body", "I like this method very much!");
        for(Entry<String, Integer> tf:tfs.entrySet()){
            LOGGER.info("\t"+tf.getKey()+"\t"+tf.getValue());
        }
        
        
        
        
//        
//        SolrQuery query = new SolrQuery("hello");
//        QueryResponse response = solrServer.query(query);
//        assertTrue("Query has no results!", response.getResults().getNumFound() == 1);
//        
//        
//        
//        
//        
//        
//        
//        
//        SolrQueryRequest req= new SolrQueryRequest();
//        
//        String queryText = "Hellow World";
//        SolrIndexSearcher searcher = req.getSearcher();
//        IndexReader reader = searcher.getAtomicReader();
//        Terms terms = reader.getTermVector(0, "content_srch");
//
//        
//        
//        Map<String, Integer> termFreqMapsq = sensequery.getTermFreqMapfromTokenStream(getAnalyzer().tokenStream("", new StringReader(queryText)));
//
//        Map<String, Integer> termFreqMapssp = sensescoreprovider.getTermFreqmapfromTerms(terms);
//
//
//


        //do some assert here

    }

    @Ignore
    public void testCKBVector() {
        Map<String, Integer> tf = new HashMap<String, Integer>();
        tf.put("hello", 1);
        tf.put("world", 1);
        DoubleFullVector vector = ckb.getFullCkbVector(tf);
        System.out.println("Got vector of dimension: " + vector.getDimension());
        assertTrue("Vector has null dimention", vector.getDimension() > 0);
        assertTrue("Vector has different dim than CKB", vector.getDimension() == ckb.getDimentionality());

        for (double d : vector.getData()) {
            System.out.print(d + ", ");
        }
        System.out.println();
    }
}
