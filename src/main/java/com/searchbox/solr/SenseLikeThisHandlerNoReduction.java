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
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SimpleFacets;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.*;
import org.apache.solr.util.SolrPluginUtils;

/**
 * Solr MoreLikeThis --
 *
 * Return similar documents either based on a single document or based on posted
 * text.
 *
 * @since solr 1.3
 */
public class SenseLikeThisHandlerNoReduction extends RequestHandlerBase {
    // Pattern is thread safe -- TODO? share this with general 'fl' param

    private static final Pattern splitList = Pattern.compile(",| ");

    @Override
    public void init(NamedList args) {
        super.init(args);
    }

    @Override
    public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
        SolrParams params = req.getParams();
        int docID;
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
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
        }

        SolrIndexSearcher searcher = req.getSearcher();
        SchemaField uniqueKeyField = searcher.getSchema().getUniqueKeyField();


        DocListAndSet sltDocs = null;

        // Parse Required Params
        // This will either have a single Reader or valid query
        Reader reader = null;
        try {
            if (q == null || q.trim().length() < 1) {
                Iterable<ContentStream> streams = req.getContentStreams();
                if (streams != null) {
                    Iterator<ContentStream> iter = streams.iterator();
                    if (iter.hasNext()) {
                        reader = iter.next().getReader();
                    }
                    if (iter.hasNext()) {
                        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                                "SenseLikeThis does not support multiple ContentStreams");
                    }
                }
            }

            int start = params.getInt(CommonParams.START, 0);
            int rows = params.getInt(CommonParams.ROWS, 10);

            // Find documents SenseLikeThis - either with a reader or a query
            // --------------------------------------------------------------------------------
            SenseQuery slt = null;
            if (reader != null) {
                throw new RuntimeException("SLT based on a reader is not yet implemented");
            } else if (q != null) {
                // Matching options
                boolean includeMatch = params.getBool(MoreLikeThisParams.MATCH_INCLUDE, true);
                int matchOffset = params.getInt(MoreLikeThisParams.MATCH_OFFSET, 0);
                // Find the base match


                DocList match = searcher.getDocList(query, null, null, matchOffset, 1, flags); // only get the first one...
                if (includeMatch) {
                    rsp.add("match", match);
                }


                // Get docID
                DocIterator iterator = match.iterator();
                docID = iterator.nextDoc();

                BooleanQuery bq = new BooleanQuery();
                Document doc = searcher.getIndexReader().document(docID);
                bq.add(new TermQuery(new Term(uniqueKeyField.getName(), uniqueKeyField.getType().storedToIndexed(doc.getField(uniqueKeyField.getName())))), BooleanClause.Occur.MUST_NOT);
                filters.add(bq);



            } else {
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                        "SenseLikeThis requires either a query (?q=) or text to find similar documents.");
            }
            
            String CKBid = params.get(SenseParams.SENSE_CKB,SenseParams.SENSE_CKB_DEFAULT);

            String senseField = params.get(SenseParams.SENSE_FIELD, SenseParams.DEFAULT_SENSE_FIELD);
            slt = new SenseQuery(new RealTermFreqVector(docID, searcher.getIndexReader(), senseField), senseField, CKBid,
                    params.getFloat(SenseParams.SENSE_WEIGHT, SenseParams.DEFAULT_SENSE_WEIGHT), null);


            //Execute the SLT query
            //DocSet filtered = searcher.getDocSet(filters);
            //System.out.println("Number of documents to search:\t" + filtered.size());
            //sltDocs = searcher.getDocListAndSet(slt, filtered, Sort.RELEVANCE, start, rows, flags);
            sltDocs = searcher.getDocListAndSet(slt, filters, Sort.RELEVANCE, start, rows, flags);

        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (sltDocs == null) {
            sltDocs = new DocListAndSet(); // avoid NPE
        }
        rsp.add("response", sltDocs.docList);

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
        // Copied from StandardRequestHandler... perhaps it should be added to doStandardDebug?
        if (dbg == true) {
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
        }
    }

    //////////////////////// SolrInfoMBeans methods //////////////////////
    @Override
    public String getDescription() {
        return "Searchbox SenseLikeThis";
    }

    @Override
    public String getSource() {
        return "$URL: https://svn.apache.org/repos/asf/lucene/dev/branches/lucene_solr_4_0/solr/core/src/java/org/apache/solr/handler/MoreLikeThisHandler.java $";
    }

    @Override
    public URL[] getDocs() {
        try {
            return new URL[]{new URL("http://wiki.apache.org/solr/MoreLikeThis")};
        } catch (MalformedURLException ex) {
            return null;
        }
    }
}
