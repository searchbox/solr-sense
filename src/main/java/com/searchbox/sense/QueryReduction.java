package com.searchbox.sense;

import com.searchbox.math.RealTermFreqVector;
import com.searchbox.solr.SenseQParserPlugin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.search.SolrIndexSearcher;

public class QueryReduction {

    private final CognitiveKnowledgeBase ckb;
    private HashMap<String, Float> termFreqMap;
    private SolrIndexSearcher searcher;
    private String senseField;
    private int threshold = 500;

    public QueryReduction(HashMap<String, Float> termFreqMap, String CKBid, SolrIndexSearcher searcher, String senseField) {
        this.ckb = ((CognitiveKnowledgeBase) SenseQParserPlugin.ckbByID.get(CKBid));
        this.termFreqMap = termFreqMap;
        this.searcher = searcher;
        this.senseField = senseField;
    }

    public BooleanQuery getFiltersForQueryRedux() throws IOException {
        BooleanQuery bqoutter = new BooleanQuery();
        int numterms = this.termFreqMap.size();
        int numtermstouse = (int) Math.round(numterms * 0.2D);
        RealTermFreqVector rtv = new RealTermFreqVector(this.termFreqMap);
        RealTermFreqVector rtvn = rtv.getUnitVector();

        Holder[] hq = new Holder[numterms];
        for (int zz = 0; zz < numterms; zz++) {
            Holder lhq = new Holder();
            lhq.spot = zz;
            lhq.value = this.ckb.getFullCkbVector(rtvn.getTerms()[zz], rtvn.getFreqs()[zz]).getNorm();
            hq[zz] = lhq;
        }

        Arrays.sort(hq);

        for (int zz = 0; zz < numtermstouse; zz++) {
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

        return bqoutter;
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