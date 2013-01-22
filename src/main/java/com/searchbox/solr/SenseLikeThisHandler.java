/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.searchbox.solr;

import com.searchbox.commons.params.SenseParams;
import com.searchbox.lucene.SenseQuery;
import com.searchbox.math.RealTermFreqVector;
import com.searchbox.lucene.QueryReductionFilter;
import com.searchbox.utils.SolrCacheKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SimpleFacets;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.*;
import org.apache.solr.util.SolrPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Solr MoreLikeThis --
 *
 * Return similar documents either based on a single document or based on posted
 * text.
 *
 * @since solr 1.3
 */
public class SenseLikeThisHandler extends RequestHandlerBase {
    // Pattern is thread safe -- TODO? share this with general 'fl' param

    private static Logger LOGGER = LoggerFactory.getLogger(SenseLikeThisHandler.class);
    volatile long numRequests;
    volatile long numFiltered;
    volatile long totalTime;
    volatile long numErrors;
    volatile long numEmpty;
    volatile long numSubset;
    volatile long numTermsConsidered;
    volatile long numTermsUsed;
    private static final Pattern splitList = Pattern.compile(",| ");

    @Override
    public void init(NamedList args) {
        super.init(args);
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        NamedList<Object> timinginfo = new NamedList<Object>();
        numRequests++;
        long startTime = System.currentTimeMillis();
        long lstartTime = System.currentTimeMillis();

        boolean fromcache = false;


        try {
            SolrParams params = req.getParams();
            int start = params.getInt(CommonParams.START, 0);
            int rows = params.getInt(CommonParams.ROWS, 10);

            HashSet<String> toIgnore = (new HashSet<String>());
            toIgnore.add("start");
            toIgnore.add("rows");
            toIgnore.add("fl");
            toIgnore.add("wt");
            toIgnore.add("indent");



            SolrCacheKey key = new SolrCacheKey(params, toIgnore);

            // Set field flags
            ReturnFields returnFields = new ReturnFields(req);
            rsp.setReturnFields(returnFields);
            int flags = 0;
            if (returnFields.wantsScore()) {
                flags |= SolrIndexSearcher.GET_SCORES;
            }

            String defType = params.get(QueryParsing.DEFTYPE, QParserPlugin.DEFAULT_QTYPE);
            String q = params.get(CommonParams.Q);
            Query query = null;
            QueryReductionFilter qr = null;
            SortSpec sortSpec = null;
            List<Query> filters = new ArrayList<Query>();

            try {
                if (q != null) {
                    QParser parser = QParser.getParser(q, defType, req);
                    query = parser.getQuery();
                    sortSpec = parser.getSort(true);
                }

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

            timinginfo.add("Parse Query time", System.currentTimeMillis() - lstartTime);
            LOGGER.debug("Parsed Query Time:\t" + (System.currentTimeMillis() - lstartTime));
            lstartTime = System.currentTimeMillis();

            SolrIndexSearcher searcher = req.getSearcher();
            SchemaField uniqueKeyField = searcher.getSchema().getUniqueKeyField();



            // Parse Required Params
            // This will either have a single Reader or valid query



            // Find documents SenseLikeThis - either with a reader or a query
            // --------------------------------------------------------------------------------
            SenseQuery slt = null;
            if (q == null) {
                numErrors++;
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                        "SenseLikeThis requires either a query (?q=) or text to find similar documents.");

            }
            // Matching options
            boolean includeMatch = params.getBool(MoreLikeThisParams.MATCH_INCLUDE, true);
            int matchOffset = params.getInt(MoreLikeThisParams.MATCH_OFFSET, 0);
            // Find the base match


            DocList match = searcher.getDocList(query, null, null, matchOffset, 1, flags); // only get the first one...
            if (includeMatch) {
                rsp.add("match", match);
            }

            DocIterator iterator = match.iterator();
            if (!iterator.hasNext()) {
                numErrors++;
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                        "SenseLikeThis no document found matching request.");
            }
            int id = iterator.nextDoc();

            timinginfo.add("Find Query Doc", System.currentTimeMillis() - lstartTime);
            LOGGER.debug("Find Query Doc:\t" + (System.currentTimeMillis() - lstartTime));
            lstartTime = System.currentTimeMillis();

            SolrCache sc = searcher.getCache("com.searchbox.sltcache");
            DocListAndSet sltDocs = null;
            if (sc != null) {
                //try to get from cache
                sltDocs = (DocListAndSet) sc.get(key.getSet());
            } else {
                LOGGER.error("com.searchbox.sltcache not defined, can't cache slt queries");
            }

            sltDocs = (DocListAndSet) sc.get(key.getSet());
            if (start + rows > 1000 || sltDocs == null || !params.getBool(CommonParams.CACHE, true)) { //not in cache, need to do search
                BooleanQuery bq = new BooleanQuery();
                Document doc = searcher.getIndexReader().document(id);
                bq.add(new TermQuery(new Term(uniqueKeyField.getName(), uniqueKeyField.getType().storedToIndexed(doc.getField(uniqueKeyField.getName())))), BooleanClause.Occur.MUST_NOT);
                filters.add(bq);

                String senseField = SenseParams.DEFAULT_SENSE_FIELD;
                String[] senseFields = splitList.split(params.get(SenseParams.SENSE_FIELD, SenseParams.DEFAULT_SENSE_FIELD));

                //TODO more intelligent handling of multiple fields , can probably do a boolean junction of multiple sensequeries, but this will be slow
                long maxlength = -1;
                for (String possibleField : senseFields) {
                    long flength = doc.getField(possibleField).stringValue().length();
                    if (flength > maxlength) {
                        senseField = possibleField;
                        maxlength = flength;
                    }
                }

                LOGGER.debug("Using sense field :\t" + (senseField));



                String CKBid = params.get(SenseParams.SENSE_CKB, SenseParams.SENSE_CKB_DEFAULT);

                RealTermFreqVector rtv = new RealTermFreqVector(id, searcher.getIndexReader(), senseField);
                timinginfo.add("Make real term freq vector", System.currentTimeMillis() - lstartTime);
                lstartTime = System.currentTimeMillis();

                qr = new QueryReductionFilter(rtv, CKBid, searcher, senseField);
                qr.setNumtermstouse(params.getInt(SenseParams.SENSE_QR_NTU, SenseParams.SENSE_QR_NTU_DEFAULT));
                qr.setThreshold(params.getInt(SenseParams.SENSE_QR_THRESH, SenseParams.SENSE_QR_THRESH_DEFAULT));
                qr.setMaxDocSubSet(params.getInt(SenseParams.SENSE_QR_MAXDOC, SenseParams.SENSE_QR_MAXDOC_DEFAULT));
                qr.setMinDocSetSizeForFilter(params.getInt(SenseParams.SENSE_MINDOC4QR, SenseParams.SENSE_MINDOC4QR_DEFAULT));

                numTermsUsed += qr.getNumtermstouse();
                numTermsConsidered += rtv.getSize();

                timinginfo.add("Setup SLT query", System.currentTimeMillis() - lstartTime);
                LOGGER.debug("Setup SLT query:\t" + (System.currentTimeMillis() - lstartTime));
                lstartTime = System.currentTimeMillis();

                DocList subFiltered = qr.getSubSetToSearchIn(filters);
                timinginfo.add("Do Query Redux", System.currentTimeMillis() - lstartTime);
                LOGGER.debug("Do query redux:\t" + (System.currentTimeMillis() - lstartTime));
                lstartTime = System.currentTimeMillis();

                numFiltered += qr.getFiltered().docList.size();
                numSubset += subFiltered.size();
                LOGGER.info("Number of documents to search:\t" + subFiltered.size());

                slt = new SenseQuery(rtv, senseField, CKBid, params.getFloat(SenseParams.SENSE_WEIGHT, SenseParams.DEFAULT_SENSE_WEIGHT), null);
                LOGGER.debug("Setup sense query:\t" + (System.currentTimeMillis() - lstartTime));
                timinginfo.add("Setup sense query", System.currentTimeMillis() - lstartTime);
                lstartTime = System.currentTimeMillis();

                sltDocs = searcher.getDocListAndSet(slt, subFiltered, Sort.RELEVANCE, 0, 1000, flags);
                timinginfo.add("Do sense query", System.currentTimeMillis() - lstartTime);
                lstartTime = System.currentTimeMillis();

                LOGGER.debug("Adding this keyto cache:\t" + key.getSet().toString());
                searcher.getCache("com.searchbox.sltcache").put(key.getSet(), sltDocs);



            } else {
                fromcache = true;
                timinginfo.add("Getting from cache", System.currentTimeMillis() - lstartTime);
                LOGGER.debug("Got result from cache");
                lstartTime = System.currentTimeMillis();
            }



            if (sltDocs == null) {
                numEmpty++;
                sltDocs = new DocListAndSet(); // avoid NPE
            }
            rsp.add("response", sltDocs.docList.subset(start, rows));


            // maybe facet the results
            if (params.getBool(FacetParams.FACET, false)) {
                if (sltDocs.docSet == null) {
                    rsp.add("facet_counts", null);
                } else {
                    SimpleFacets f = new SimpleFacets(req, sltDocs.docSet, params);
                    rsp.add("facet_counts", f.getFacetCounts());
                }
            }
            timinginfo.add("Facet parts", System.currentTimeMillis() - lstartTime);
            LOGGER.debug("Facet parts:\t" + (System.currentTimeMillis() - lstartTime));




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
            // Copied from StandardRequestHandler... perhaps it should be added to doStandardDebug?
            if (dbg == true) {
                try {
                    lstartTime = System.currentTimeMillis();
                    NamedList<Object> dbgInfo = SolrPluginUtils.doStandardDebug(req, q, slt, sltDocs.docList.subset(start, rows), dbgQuery, dbgResults);
                    dbgInfo.add("Query freqs", slt.getAllTermsasString());
                    if (null != dbgInfo) {
                        if (null != filters) {
                            dbgInfo.add("filter_queries", req.getParams().getParams(CommonParams.FQ));
                            List<String> fqs = new ArrayList<String>(filters.size());
                            for (Query fq : filters) {
                                fqs.add(QueryParsing.toString(fq, req.getSchema()));
                            }
                            dbgInfo.add("parsed_filter_queries", fqs);
                        }
                        if (null != qr) {
                            dbgInfo.add("QueryReduction", qr.getDbgInfo());
                        }
                        if (null != slt) {
                            dbgInfo.add("SLT", slt.getDbgInfo());

                        }

                        dbgInfo.add("fromcache", fromcache);
                        rsp.add("debug", dbgInfo);
                        timinginfo.add("Debugging parts", System.currentTimeMillis() - lstartTime);
                        dbgInfo.add("timings", timinginfo);
                    }
                } catch (Exception e) {
                    SolrException.log(SolrCore.log, "Exception during debug", e);
                    rsp.add("exception_during_debug", SolrException.toStr(e));
                }
            }
        } catch (Exception e) {
            numErrors++;
            e.printStackTrace();
        } finally {
            totalTime += System.currentTimeMillis() - startTime;
        }

    }

    //////////////////////// SolrInfoMBeans methods //////////////////////
    @Override
    public String getDescription() {
        return "Searchbox SenseLikeThis";
    }

    @Override
    public String getVersion() {
        return "1.0";
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
            all.add("averageFiltered", "" + (float) numFiltered / numRequests);
            all.add("averageSubset", "" + (float) numSubset / numRequests);


            all.add("totalTermsConsidered", numTermsConsidered);
            all.add("avgTermsConsidered", (float) numTermsConsidered / numRequests);

            all.add("totalTermsUsed", numTermsConsidered);
            all.add("avgTermsUsed", (float) numTermsUsed / numRequests);



            all.add("avgTimePerRequest", "" + (float) totalTime / numRequests);
            all.add("avgRequestsPerSecond", "" + (float) numRequests / (totalTime * 0.001));
        } else {
            all.add("averageFiltered", "" + 0);
            all.add("averageSubset", "" + 0);
            all.add("avgTimePerRequest", "" + 0);
            all.add("avgRequestsPerSecond", "" + 0);
        }

        return all;
    }
}
