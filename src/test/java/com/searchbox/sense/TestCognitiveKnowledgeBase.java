/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.commons.params.SenseParams;
import com.searchbox.lucene.SenseQuery;
import com.searchbox.lucene.SenseScoreProvider;
import com.searchbox.math.DoubleFullVector;
import com.searchbox.solr.EmptySolrTestCase;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
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

/**
 *
 * @author gamars
 */
public class TestCognitiveKnowledgeBase extends EmptySolrTestCase {

    private CognitiveKnowledgeBase ckb;
    private SenseQuery sensequery;
    private SenseScoreProvider sensescoreprovider;
    protected LocalRequestFactory lrf;

    
   public SolrQueryRequest req(String... q) {
    return lrf.makeRequest(q);
  }
    
    private static Analyzer getAnalyzer() {
        return new Analyzer() {
            protected TokenStreamComponents createComponents(final String fieldName,
                    final Reader reader) {
                return new TokenStreamComponents(new WhitespaceTokenizer(org.apache.lucene.util.Version.LUCENE_40, reader));
            }
        };
    }

    public void testLoadingCKB() {
        assertTrue("No terms has been loaded", ckb.getTerms().size() > 0);
        assertTrue("CKB has no dimensions", ckb.getDimentionality() > 0);
    }

    public void testTFcreation() throws SolrServerException, IOException {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", System.currentTimeMillis() + "");
        doc.addField("content_srch", "Hello World");
        solrServer.add(doc);
        solrServer.commit();

        
        
        
        SolrQuery query = new SolrQuery("hello");
        QueryResponse response = solrServer.query(query);
        assertTrue("Query has no results!", response.getResults().getNumFound() == 1);
        
        
        
        
        
        
        
        
        SolrQueryRequest req= new SolrQueryRequest();
        
        String queryText = "Hellow World";
        SolrIndexSearcher searcher = req.getSearcher();
        IndexReader reader = searcher.getAtomicReader();
        Terms terms = reader.getTermVector(0, "content_srch");

        
        
        Map<String, Integer> termFreqMapsq = sensequery.getTermFreqMapfromTokenStream(getAnalyzer().tokenStream("", new StringReader(queryText)));

        Map<String, Integer> termFreqMapssp = sensescoreprovider.getTermFreqmapfromTerms(terms);





        //do some assert here

    }

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
