/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.SolrUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 *
 */
public class ExportTest extends DataSolrTestCase {
  HashMap<String, Integer> dic;

  @Test
  public void testMakeExport() throws SolrServerException, IOException {
    Map<String, HashMap<String, Integer>> tfms = SolrUtils.getTermFrequencyMapForQuery(solrServer, "content_srch", "*:*");

    FileWriter tffile = new FileWriter("export_nonbatch.tf");
    FileWriter tdic = new FileWriter("tdic_nonbatch.tdic");
    FileWriter doc2row = new FileWriter("doc2row_nonbatch.idx");


    BufferedWriter tffile_out = new BufferedWriter(tffile);
    BufferedWriter tdic_out = new BufferedWriter(tdic);
    BufferedWriter doc2row_out = new BufferedWriter(doc2row);


    dic = new HashMap();
    LOGGER.info("Export has this many docs:\t" + tfms.size());
    int docnum = 0;
    for (Entry<String, HashMap<String, Integer>> onedoc : tfms.entrySet()) {
      doc2row_out.write(docnum + "\t" + onedoc.getKey() + "\n");
      for (Entry<String, Integer> term : onedoc.getValue().entrySet()) {
        Integer offset = getTerm(term.getKey());
        tffile_out.write(docnum + "\t" + offset + "\t" + term.getValue() + "\n");
      }
      docnum++;
    }


    HashMap<Integer, String> invdic = new HashMap();

    for (String term : dic.keySet()) {
      invdic.put(dic.get(term), term);
    }

    for (Integer index : invdic.keySet()) {
      tdic_out.write(index + "\t" + invdic.get(index) + "\n");
    }

    tffile_out.close();
    tdic_out.close();
    doc2row_out.close();

  }

  @Test
  public void testExportinBatch() throws SolrServerException, IOException {

    FileWriter tffile = new FileWriter("export_batch.tf");
    FileWriter tdic = new FileWriter("tdic_batch.tdic");
    FileWriter doc2row = new FileWriter("doc2row_batch.idx");


    BufferedWriter tffile_out = new BufferedWriter(tffile);
    BufferedWriter tdic_out = new BufferedWriter(tdic);
    BufferedWriter doc2row_out = new BufferedWriter(doc2row);

    dic = new HashMap();
    int offset = 0;
    int fetchSize = 5;
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("qt", "/tvrh");
    params.set("tv.tf", "true");
    params.set("tv.fl", "content_srch");
    params.set("q", "*:*");
    params.set("rows", fetchSize);
    params.set("start", offset);

    QueryResponse response = solrServer.query(params);

    long totalResults = response.getResults().getNumFound();
    long totaldoc = 0;
    while (offset < totalResults) {

      LOGGER.info("exporting... [ " + offset + " ] of [ " + totalResults + " ] ");

      params.set("start", offset);
      params.set("rows", fetchSize);

      NamedList termVectors = (NamedList) solrServer.query(params).getResponse().get("termVectors");
      for (int i = 1; i < termVectors.size(); i++) {
        NamedList doc = (NamedList) termVectors.getVal(i);
        String id = (String) doc.get("uniqueKey");
        doc2row_out.write(totaldoc + "\t" + id + "\n");
        NamedList tf = (NamedList) doc.get("content_srch");
        if (tf != null) {
          for (int tfi = 0; tfi < tf.size(); tfi++) {
            String term = tf.getName(tfi);
            Integer count = (Integer) ((NamedList) tf.getVal(tfi)).get("tf");
            tffile_out.write(totaldoc + "\t" + getTerm(term) + "\t" + count + "\n");
          }
        }
        totaldoc++;
      }
      offset += fetchSize;
    }


    HashMap<Integer, String> invdic = new HashMap();

    for (String term : dic.keySet()) {
      invdic.put(dic.get(term), term);
    }

    for (Integer index : invdic.keySet()) {
      tdic_out.write(index + "\t" + invdic.get(index) + "\n");
    }

    tffile_out.close();
    tdic_out.close();
    doc2row_out.close();
  }

  private Integer getTerm(String term) {
    Integer offset = dic.get(term);
    if (offset == null) {
      offset = dic.size();
      dic.put(term, offset);
    }
    return offset;
  }
}
