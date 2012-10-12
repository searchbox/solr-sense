/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import com.searchbox.sense.CKBUtils;
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
    
    public static final String NAME = "sense";
    
    @Override
    public QParser createParser(String query, SolrParams sp, SolrParams sp1, SolrQueryRequest sqr) {
        
        System.out.println("Here I get the query that I could Expand: " + query);
        
        System.out.println("Got CKB hash: " + CKBUtils.getCKB("test"));
        
        QParser parentQparser = super.createParser(query, sp1, sp1, sqr);
        
        
        System.out.println("Hey this is a searchbox query. Going to use sense!!!");
        return parentQparser;
    }

    public void init(NamedList nl) {
        super.init(nl);
    }
    
}
