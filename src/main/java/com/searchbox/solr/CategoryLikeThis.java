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

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;
import com.searchbox.commons.params.SenseParams;
import com.searchbox.lucene.CategoryQuery;
import com.searchbox.math.RealTermFreqVector;
import com.searchbox.sense.CategorizationBase;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SimpleFacets;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.*;

/**
 * Solr MoreLikeThis --
 *
 * Return similar documents either based on a single document or based on posted
 * text.
 *
 * @since solr 1.3
 */
public class CategoryLikeThis extends RequestHandlerBase {
       volatile long numRequests;
    volatile long numFiltered;
    volatile long totalTime;
    volatile long numErrors;
    volatile long numEmpty;
    volatile long numSubset;
    // Pattern is thread safe -- TODO? share this with general 'fl' param

    private static final Pattern splitList = Pattern.compile(",| ");

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
        String senseField = params.get(SenseParams.SENSE_FIELD, SenseParams.DEFAULT_SENSE_FIELD);
        BooleanQuery catfilter = new BooleanQuery();
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
        List<Query> filters = new LinkedList<Query>();
        List<RealTermFreqVector> prototypetfs = new LinkedList<RealTermFreqVector>();

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

        SolrIndexSearcher searcher = req.getSearcher();
        DocListAndSet cltDocs = null;


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
                        numErrors++;
                        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                                "SenseLikeThis does not support multiple ContentStreams");
                    }
                }
            }

            int start = params.getInt(CommonParams.START, 0);
            int rows = params.getInt(CommonParams.ROWS, 10);

            // Find documents SenseLikeThis - either with a reader or a query
            // --------------------------------------------------------------------------------
            if (reader != null) {
                numErrors++;
                throw new RuntimeException("SLT based on a reader is not yet implemented");
            } else if (q != null) {

                System.out.println("Query for category:\t" + query);
                DocList match = searcher.getDocList(query, null, null, 0, 10, flags); // get first 10
                if (match.size() == 0) { // no docs to make prototype!
                    System.out.println("No documents found for prototype!");
                    rsp.add("response", new DocListAndSet());
                    return;
                }


                HashMap<String, Float> overallFreqMap = new HashMap<String, Float>();
                // Create the TF of blah blah blah
                DocIterator iterator = match.iterator();
                while (iterator.hasNext()) {
                    // do a MoreLikeThis query for each document in results
                    int id = iterator.nextDoc();
                    System.out.println("Working on doc:\t" + id);
                    RealTermFreqVector rtv = new RealTermFreqVector(id,searcher.getIndexReader(),senseField);
                    for (int zz=0;zz<rtv.getSize();zz++){
                        Float prev= overallFreqMap.get(rtv.getTerms()[zz]);
                        if(prev==null)
                            prev=0f;
                        overallFreqMap.put(rtv.getTerms()[zz], rtv.getFreqs()[zz]+prev);
                    }
                    prototypetfs.add(rtv);
                }
                
                List<String> sortedKeys = Ordering.natural().onResultOf(Functions.forMap(overallFreqMap)).immutableSortedCopy(overallFreqMap.keySet());
                int keyiter = Math.min(sortedKeys.size() - 1, BooleanQuery.getMaxClauseCount() - 1);
                System.out.println("I have this many terms:\t" + sortedKeys.size());
                System.out.println("And i'm going to use this many:\t" + keyiter);
                for (; keyiter >= 0; keyiter--) {
                    TermQuery tq = new TermQuery(new Term(senseField, sortedKeys.get(keyiter)));
                    catfilter.add(tq, BooleanClause.Occur.SHOULD);
                }
                

            } else {
                numErrors++;
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                        "CategoryLikeThis requires either a query (?q=) or text to find similar documents.");
            }

            System.out.println(
                    "document filter is: \t" + catfilter);
            CategorizationBase model = new CategorizationBase(prototypetfs);
            CategoryQuery clt = CategoryQuery.CategoryQueryForDocument(catfilter, model, searcher.getIndexReader(), senseField);
            DocSet filtered = searcher.getDocSet(filters);
            cltDocs = searcher.getDocListAndSet(clt, filtered, Sort.RELEVANCE, start, rows, flags);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        if (cltDocs == null) {
            numEmpty++;
            cltDocs = new DocListAndSet(); // avoid NPE
        }
        rsp.add("response", cltDocs.docList);

        // maybe facet the results
        if (params.getBool(FacetParams.FACET, false)) {
            if (cltDocs.docSet == null) {
                rsp.add("facet_counts", null);
            } else {
                SimpleFacets f = new SimpleFacets(req, cltDocs.docSet, params);
                rsp.add("facet_counts", f.getFacetCounts());
            }
        }
      } catch (Exception e) {
            numErrors++;
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
    public NamedList<Object> getStatistics() {
        
        NamedList all = new SimpleOrderedMap<Object>();
        all.add("requests",""+ numRequests);
        all.add("errors",""+ numErrors);
        all.add("totalTime(ms)",""+ totalTime);
        all.add("empty",""+ numEmpty);
        
        if (numRequests != 0) {
            all.add("averageFiltered",""+ numFiltered / numRequests);
            all.add("averageSubset",""+ numSubset / numRequests);
            all.add("avgTimePerRequest",""+ totalTime / numRequests);
            all.add("avgRequestsPerSecond",""+ numRequests / (totalTime*0.001));
        } else {
            all.add("averageFiltered",""+ 0);
            all.add("averageSubset",""+ 0);
            all.add("avgTimePerRequest",""+ 0);
            all.add("avgRequestsPerSecond",""+ 0);
        }

        return all;
    }
    
}
