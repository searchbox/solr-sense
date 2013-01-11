package com.searchbox.lucene;

import com.searchbox.math.RealTermFreqVector;
import com.searchbox.sense.CognitiveKnowledgeBase;
import com.searchbox.solr.SenseQParserPlugin;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.DocSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class QueryReductionFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryReductionFilter.class);
    private final CognitiveKnowledgeBase ckb;
    private RealTermFreqVector rtv;
    private SolrIndexSearcher searcher;
    private String senseField;
    private int threshold = 500;
    private int numtermstouse = -1;
    private int maxDocSubSet = 5000;
    private int minDocSetSizeForFilter = 10000;
    private BooleanQuery filterQR;
    private HashMap<TreeSet<Integer>, Long> subQuerycache = new HashMap<TreeSet<Integer>, Long>();
    private HashSet<TreeSet<Integer>> outterQuery = new HashSet<TreeSet<Integer>>();
    private DocListAndSet filtered;
    private SimpleOrderedMap<Object> dbginfo = new SimpleOrderedMap<Object>();

    public DocListAndSet getFiltered() {
        return filtered;
    }

    public int getMinDocSetSizeForFilter() {
        return minDocSetSizeForFilter;
    }

    public void setMinDocSetSizeForFilter(int minDocSetSizeForFilter) {
        this.minDocSetSizeForFilter = minDocSetSizeForFilter;
    }

    public BooleanQuery getFilterQR() {
        return filterQR;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getNumtermstouse() {
        return numtermstouse;
    }

    public void setNumtermstouse(int numtermstouse) {
        this.numtermstouse = numtermstouse;
    }

    public void setMaxDocSubSet(int maxDocSubSet) {
        this.maxDocSubSet = maxDocSubSet;
    }

    public int getMaxDocSubSet() {
        return maxDocSubSet;
    }

    public QueryReductionFilter(RealTermFreqVector rtv, String CKBid, SolrIndexSearcher searcher, String senseField) {
        this.ckb = ((CognitiveKnowledgeBase) SenseQParserPlugin.getCKBbyID(CKBid));
        dbginfo.add("ckb", ckb.getName());
        this.rtv = rtv;
        dbginfo.add("tf size", this.rtv.getSize());

        this.searcher = searcher;

        this.senseField = senseField;
        dbginfo.add("senseField", senseField);
    }

    public BooleanQuery getFiltersForQueryRedux(DocSet otherFilterDocSet) throws IOException {
        filterQR = new BooleanQuery();
        int numterms = this.rtv.getSize();
        if (numtermstouse == -1) {
            numtermstouse = (int) Math.max(Math.round(numterms * 0.2), 5);
        }

        if (numtermstouse > numterms) {
            numtermstouse = numterms;
        }

        LOGGER.debug("Numtermstouse\t" + numtermstouse);
        dbginfo.add("numtermstouse", numtermstouse);

        RealTermFreqVector rtvn = rtv.getUnitVector();


        Holder[] hq = new Holder[numterms];
        SimpleOrderedMap<Object> hqmp = new SimpleOrderedMap<Object>();

        for (int zz = 0; zz < numterms; zz++) {
            Holder lhq = new Holder();
            lhq.spot = zz;
            lhq.value = this.ckb.getFullCkbVector(rtvn.getTerms()[zz], rtvn.getFreqs()[zz]).getNorm();
            hqmp.add(rtvn.getTerms()[zz], lhq.value);
            hq[zz] = lhq;
        }

        dbginfo.add("term_norms", hqmp);
        LOGGER.debug("");
        Arrays.sort(hq);

        for (int zz = 0; zz < numtermstouse; zz++) {
            TreeSet<Integer> outtertreeset = new TreeSet<Integer>();
            outtertreeset.add(zz);
            LOGGER.debug("  (" + rtvn.getTerms()[hq[zz].spot] + ":" + hq[zz].value + ")");

            TermQuery tqoutter = new TermQuery(new Term(this.senseField, rtvn.getTerms()[hq[zz].spot]));
            BooleanQuery bqinner = new BooleanQuery();

            bqinner.add(tqoutter, BooleanClause.Occur.MUST);
            long numdocs = getNumDocs(outtertreeset, bqinner, otherFilterDocSet);

            if (numdocs <= this.threshold) {
                addToQuery(bqinner, BooleanClause.Occur.SHOULD, outtertreeset);
            } else {
                for (int yy = 0; yy < numtermstouse; yy++) {
                    TreeSet<Integer> innertreeset = (TreeSet<Integer>) outtertreeset.clone();
                    int lyy = yy;
                    if (zz == yy) {
                        continue;
                    }
                    BooleanQuery bqinner2 = bqinner.clone();
                    long lnumdocs = numdocs;
                    while (lnumdocs > this.threshold) {
                        //System.out.println(lyy+"\t"+numtermstouse+"\t"+lnumdocs);
                        TermQuery tqinner = new TermQuery(new Term(this.senseField, rtvn.getTerms()[hq[lyy].spot]));
                        bqinner2.add(tqinner, BooleanClause.Occur.MUST);
                        innertreeset.add(lyy);
                        lnumdocs = getNumDocs(innertreeset, bqinner2, otherFilterDocSet);
                        lyy++;
                    }
                    addToQuery(bqinner2, BooleanClause.Occur.SHOULD, innertreeset);
                }
            }
        }
        LOGGER.debug("");
        return this.filterQR;
    }

    private boolean addToQuery(BooleanQuery bqinner, Occur booleanclause, TreeSet<Integer> ts) {
        //returns true if it was added, false if it was already in the cache
        if (outterQuery.contains(ts)) {
            LOGGER.debug("+");
            return false;
        } else {
            this.filterQR.add(bqinner, booleanclause);
            outterQuery.add(ts);
        }
        return true;
    }

    private Long getNumDocs(TreeSet<Integer> treeset, Query q, DocSet otherFilterDocSet) throws IOException {
        Long numdocs = subQuerycache.get(treeset);

        if (numdocs == null) {
            numdocs = new Long(this.searcher.getDocSet(q, otherFilterDocSet).size());
            subQuerycache.put(treeset, numdocs);
        } else {
            //System.out.println("from cache!");
        }
        return numdocs;
    }

    public DocList getSubSetToSearchIn(List<Query> otherFilter) throws IOException {
        DocSet otherFilterDocSet = this.searcher.getDocSet(otherFilter);
        LOGGER.debug("otherFilterDocSet size:\t" + otherFilterDocSet.size());
        Query filterQR;

        if (otherFilterDocSet.size() <= minDocSetSizeForFilter) {
            LOGGER.debug("Filterset too small, not doing query reduction");
            dbginfo.add("did_qr", false);
            filterQR = new MatchAllDocsQuery();
        } else {
            filterQR = getFiltersForQueryRedux(otherFilterDocSet);
            dbginfo.add("did_qr", true);
        }

        LOGGER.debug("Filter used:\t" + filterQR);
        filtered = searcher.getDocListAndSet(filterQR, otherFilterDocSet, Sort.RELEVANCE, 0, maxDocSubSet);
        return filtered.docList.subset(0, maxDocSubSet);
    }

    public SimpleOrderedMap<Object> getDbgInfo() {
        dbginfo.add("threshold", threshold);
        dbginfo.add("numtermstouse", numtermstouse);
        dbginfo.add("maxDocSubSet", maxDocSubSet);
        dbginfo.add("minDocSetSizeForFilter", minDocSetSizeForFilter);
        dbginfo.add("filterQR", filterQR.toString());
        dbginfo.add("subQuerycache_size", subQuerycache.size());
        dbginfo.add("outterQuery_size", outterQuery.size());
        dbginfo.add("filtered_doclist_size", filtered.docList.size());
        return dbginfo;
    }

    private class Holder implements Comparable<Holder> {

        public int spot;
        public float value;

        private Holder() {
        }

        public int compareTo(Holder o2) {
            return this.value == o2.value ? 0 : this.value > o2.value ? -1 : 1;
        }
    }
}