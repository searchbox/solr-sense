/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.lucene.SenseQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;

/**
 *
 * @author gamars
 */
public class SenseQParser extends QParser{

  QParser parent;
  
  public SenseQParser(QParser parent, String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
    this.parent = parent;
  }
  
  @Override
  public Query parse() throws ParseException {
    // here we wrap the query with our senseQuery.
    return new SenseQuery(parent.parse());
  }
  
}
