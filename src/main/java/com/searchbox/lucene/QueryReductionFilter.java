package com.searchbox.lucene;

import com.searchbox.math.RealTermFreqVector;
import com.searchbox.sense.CognitiveKnowledgeBase;
import com.searchbox.solr.SenseQParserPlugin;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.SolrIndexSearcher;

public class QueryReductionFilter {

    private final CognitiveKnowledgeBase ckb;
    private RealTermFreqVector rtv;
    private SolrIndexSearcher searcher;
    private String senseField;
    private int threshold = 500;
    private int numtermstouse=-1;
    private int maxDocSubSet=5000;
    private BooleanQuery filterQR;

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
        this.maxDocSubSet=maxDocSubSet;
    }
    
    
    public int getMaxDocSubSet() {
        return maxDocSubSet;
    }

    
    public QueryReductionFilter(RealTermFreqVector rtv, String CKBid, SolrIndexSearcher searcher, String senseField) {
        this.ckb = ((CognitiveKnowledgeBase) SenseQParserPlugin.ckbByID.get(CKBid));
        this.rtv = rtv;
        this.searcher = searcher;
        this.senseField = senseField;
    }

    public BooleanQuery getFiltersForQueryRedux() throws IOException {
        BooleanQuery bqoutter = new BooleanQuery();
        int numterms = this.rtv.getSize();
        if(numtermstouse==-1) {
            numtermstouse = (int) Math.max(Math.round(numterms * 0.2),5);
        }
        System.out.println("Numtermstouse\t"+numtermstouse);
        RealTermFreqVector rtvn = rtv.getUnitVector();

        Holder[] hq = new Holder[numterms];
        for (int zz = 0; zz < numterms; zz++) {
            Holder lhq = new Holder();
            lhq.spot = zz;
            lhq.value = this.ckb.getFullCkbVector(rtvn.getTerms()[zz], rtvn.getFreqs()[zz]).getNorm();
            
            hq[zz] = lhq;
        }
        System.out.println();

        Arrays.sort(hq);

        for (int zz = 0; zz < numtermstouse; zz++) {
            System.out.print("\t\t"+rtvn.getTerms()[hq[zz].spot]);
            TermQuery tqoutter = new TermQuery(new Term(this.senseField, rtvn.getTerms()[hq[zz].spot]));
            BooleanQuery bqinner = new BooleanQuery();

            bqinner.add(tqoutter, BooleanClause.Occur.MUST);
            long numdocs = this.searcher.getDocSet(tqoutter).size();

            if (numdocs <= this.threshold) {
                bqoutter.add(bqinner, BooleanClause.Occur.SHOULD);
            } else {
                for (int yy = 0; yy < numtermstouse; yy++) {
                    int lyy = yy;
                    if (zz == yy) {
                        continue;
                    }
                    BooleanQuery bqinner2 = bqinner.clone();
                    long lnumdocs = numdocs;
                    while (lnumdocs > this.threshold) {
                        TermQuery tqinner = new TermQuery(new Term(this.senseField, rtvn.getTerms()[hq[lyy].spot]));
                        bqinner2.add(tqinner, BooleanClause.Occur.MUST);
                        lnumdocs = this.searcher.getDocSet(bqinner2).size();
                        lyy++;
                    }
                    bqoutter.add(bqinner2, BooleanClause.Occur.SHOULD);
                }
            }
        }
        System.out.println();
        this.filterQR=bqoutter;
        
        return bqoutter;
    }
    
    public DocList getSubSetToSearchIn(List<Query> otherFilter) throws IOException {
        Query filterQR=getFiltersForQueryRedux();
        System.out.println("Filter used:\t"+filterQR);
        DocListAndSet filtered = searcher.getDocListAndSet(filterQR, otherFilter, Sort.RELEVANCE, 0, maxDocSubSet);
        return filtered.docList.subset(0,maxDocSubSet);
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