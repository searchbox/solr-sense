///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.searchbox.lucene;
//
//import com.searchbox.solr.SenseQParserPlugin;
//import java.io.IOException;
//import java.io.StringReader;
//import java.util.Map;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.Terms;
//import org.apache.solr.SolrTestCaseJ4;
//import org.apache.solr.common.params.CommonParams;
//import org.apache.solr.core.SolrConfig;
//import org.apache.solr.search.SolrIndexSearcher;
//import org.apache.solr.search.QParser;
//import org.junit.BeforeClass;
//import org.apache.solr.util.TestHarness;
//import org.junit.Test;
//
///**
// *
// * @author gamars
// */
//public class TestSenseQuery extends SolrTestCaseJ4 {
//
//    private static SolrConfig config;
//
//    @BeforeClass
//    public static void loadSolrConfig() throws Exception {
//        //TestSenseQuery.config = TestHarness.createConfig("./src/test/resources/solr", "pubmed", "");
//        initCore("solrconfig.xml", "schema.xml", "./src/test/resources/solr","pubmed");
//    }
//
//    @Test
//    public void test() throws IOException {
//        lrf.args.put(CommonParams.VERSION, "2.2");
//        TestHarness.LocalRequestFactory l = h.getRequestFactory("sense", 0, 10);
//        l.makeRequest("*:*");
//        
//        
//        SenseQParserPlugin plugin = (SenseQParserPlugin) h.getCore().getQueryPlugin("sense");
//        
//        
//        
//        
//        QParser qp = plugin.createParser("*:*", null, null, lrf.makeRequest());
//        SenseQuery sq = (SenseQuery) qp.parse();
//        
//        sq.
//        
//        
//        l.makeRequest("*:*").getSearcher().
//        SolrIndexSearcher searcher = l.makeRequest("*:*").getSearcher();
//IndexReader reader = searcher.getAtomicReader();
//Terms terms = reader.getTermVector(0, "content_srch");
//SenseQuery qq = new SenseQuery(TEST_CODEC, TEST_CODEC, null, qq)
//l.makeRequest("*:*").
//Map<String, Integer> termFreqMapsq = sensequery.getTermFreqMapfromTokenStream(getAnalyzer().tokenStream("", new StringReader(queryText)));
//Map<String, Integer> termFreqMapssp = sensescoreprovider.getTermFreqmapfromTerms(terms);
//    }
//}
