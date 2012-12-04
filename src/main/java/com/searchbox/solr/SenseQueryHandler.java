/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.commons.params.SenseParams;
import com.searchbox.lucene.SenseQuery;
import com.searchbox.math.RealTermFreqVector;
import com.searchbox.lucene.QueryReductionFilter;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SimpleFacets;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.QParser;
import org.apache.solr.search.ReturnFields;
import org.apache.solr.search.SolrIndexSearcher;

/**
 *
 * @author gamars
 */
public class SenseQueryHandler extends RequestHandlerBase {

    volatile long numRequests;
    volatile long numFiltered;
    volatile long totalTime;
    volatile long numErrors;
    volatile long numEmpty;
    volatile long numSubset;

    @Override
    public void init(NamedList args) {
        super.init(args);
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        numRequests++;
        long startTime = System.currentTimeMillis();

        try {
            SolrParams params = req.getParams();

            // Set field flags
            ReturnFields returnFields = new ReturnFields(req);
            rsp.setReturnFields(returnFields);
            int flags = 0;
            if (returnFields.wantsScore()) {
                flags |= SolrIndexSearcher.GET_SCORES;
            }

            String q = params.get(CommonParams.Q);
            List<Query> filters = new ArrayList<Query>();

            try {
                String[] fqs = req.getParams().getParams(CommonParams.FQ);
                if (fqs != null && fqs.length != 0) {
                    for (String fq : fqs) {
                        if (fq != null && fq.trim().length() != 0) {
                            QParser fqp = QParser.getParser(fq, null, req);
                            filters.add(fqp.getQuery());
                        }
                    }
                }
            } catch (ParseException e) {
                numErrors++;
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
            }




            int start = params.getInt(CommonParams.START, 0);
            int rows = params.getInt(CommonParams.ROWS, 10);


            if (q == null) {
                numErrors++;
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                        "SenseLikeThis requires either a query (?q=) or text to find similar documents.");
            }



            SolrIndexSearcher searcher = req.getSearcher();

            String CKBid = "1"; //TODO need to support different CKBs here or below?
            String senseField = params.get(SenseParams.SENSE_FIELD, SenseParams.DEFAULT_SENSE_FIELD);
            RealTermFreqVector rtv = new RealTermFreqVector(q, SenseQuery.getAnalyzerForField(req.getSchema(), senseField));

            QueryReductionFilter qr = new QueryReductionFilter(rtv, CKBid, searcher, senseField);
            qr.setNumtermstouse(params.getInt(SenseParams.SENSE_QR_NTU, SenseParams.SENSE_QR_NTU_DEFAULT));
            qr.setThreshold(params.getInt(SenseParams.SENSE_QR_THRESH, SenseParams.SENSE_QR_THRESH_DEFAULT));
            qr.setMaxDocSubSet(params.getInt(SenseParams.SENSE_QR_MAXDOC, SenseParams.SENSE_QR_MAXDOC_DEFAULT));

            DocList subFiltered = qr.getSubSetToSearchIn(filters);


            numFiltered += qr.getFiltered().docList.size();
            numSubset += subFiltered.size();
            System.out.println("Number of documents to search:\t" + subFiltered.size());
            SenseQuery slt = new SenseQuery(rtv, senseField, params.getFloat(SenseParams.SENSE_WEIGHT, SenseParams.DEFAULT_SENSE_WEIGHT), null);
            DocListAndSet sltDocs = searcher.getDocListAndSet(slt, subFiltered, Sort.RELEVANCE, start, rows, flags);


            if (sltDocs == null) {
                numEmpty++;
                sltDocs = new DocListAndSet(); // avoid NPE
            }
            rsp.add("response", sltDocs.docList);

            // --------- OLD CODE BELOW
            // maybe facet the results
            if (params.getBool(FacetParams.FACET, false)) {
                if (sltDocs.docSet == null) {
                    rsp.add("facet_counts", null);
                } else {
                    SimpleFacets f = new SimpleFacets(req, sltDocs.docSet, params);
                    rsp.add("facet_counts", f.getFacetCounts());
                }
            }

            // Debug info, not doing it for the moment. 
            boolean dbg = req.getParams().getBool(CommonParams.DEBUG_QUERY, false);

            boolean dbgQuery = false, dbgResults = false;
            if (dbg == false) {//if it's true, we are doing everything anyway.
                String[] dbgParams = req.getParams().getParams(CommonParams.DEBUG);
                if (dbgParams != null) {
                    for (int i = 0; i < dbgParams.length; i++) {
                        if (dbgParams[i].equals(CommonParams.QUERY)) {
                            dbgQuery = true;
                        } else if (dbgParams[i].equals(CommonParams.RESULTS)) {
                            dbgResults = true;
                        }
                    }
                }
            } else {
                dbgQuery = true;
                dbgResults = true;
            }
        } catch (Exception e) {
            numErrors++;
        } finally {
            totalTime += System.currentTimeMillis() - startTime;
        }
        // Copied from StandardRequestHandler... perhaps it should be added to doStandardDebug?
        /*if (dbg == true) {     // tricky tricky, no longer have a "query" value per say
         try {

         NamedList<Object> dbgInfo = SolrPluginUtils.doStandardDebug(req, q, query, sltDocs.docList, dbgQuery, dbgResults);
         if (null != dbgInfo) {
         if (null != filters) {
         dbgInfo.add("filter_queries", req.getParams().getParams(CommonParams.FQ));
         List<String> fqs = new ArrayList<String>(filters.size());
         for (Query fq : filters) {
         fqs.add(QueryParsing.toString(fq, req.getSchema()));
         }
         dbgInfo.add("parsed_filter_queries", fqs);
         }
         rsp.add("debug", dbgInfo);
         }
         } catch (Exception e) {
         SolrException.log(SolrCore.log, "Exception during debug", e);
         rsp.add("exception_during_debug", SolrException.toStr(e));
         }
         }*/
    }

    @Override
    public String getName() {
        return "SenseQueryHandler";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getDescription() {
        return "Searchbox handler based on latent semantics";
    }

    @Override
    public String getSource() {
        return "";
    }

    @Override
    public NamedList<Object> getStatistics() {

        NamedList all = new SimpleOrderedMap<Object>();
        all.add("requests", "" + numRequests);
        all.add("errors", "" + numErrors);
        all.add("totalTime(ms)", "" + totalTime);
        all.add("empty", "" + numEmpty);

        if (numRequests != 0) {
            all.add("averageFiltered", "" + numFiltered / numRequests);
            all.add("averageSubset", "" + numSubset / numRequests);
            all.add("avgTimePerRequest", "" + totalTime / numRequests);
            all.add("avgRequestsPerSecond", "" + numRequests / (totalTime * 0.001));
        } else {
            all.add("averageFiltered", "" + 0);
            all.add("averageSubset", "" + 0);
            all.add("avgTimePerRequest", "" + 0);
            all.add("avgRequestsPerSecond", "" + 0);
        }

        return all;
    }
}
