/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.lucene.SenseQuery;
import com.searchbox.sense.CKBUtils;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.ExtendedDismaxQParserPlugin;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;

/**
 *
 * @author gamars
 */
public class SenseQParserPlugin extends ExtendedDismaxQParserPlugin {

  CKBUtils ckbService;
  
  public static final String NAME = "sense";

  @Override
  public QParser createParser(String query, SolrParams sp, SolrParams sp1, SolrQueryRequest sqr) {

    System.out.println("Here I get the query that I could Expand: " + query);

    System.out.println("Got CKB hash: " + ckbService.getCKB("test"));

    QParser parentQparser = super.createParser(query, sp1, sp1, sqr);

    SenseQParser qParser = new SenseQParser(parentQparser, query, sp1, sp1, sqr);
    
    
    System.out.println("Hey this is a searchbox query. Going to use sense!!!");
    return qParser;
  }  
  
  public void init(NamedList nl) {
    super.init(nl);
  }
}
