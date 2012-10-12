/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.solr;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;

/**
 *
 * @author gamars
 */
public class SenseQueryHandler implements SolrRequestHandler {

    public void init(NamedList nl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void handleRequest(SolrQueryRequest sqr, SolrQueryResponse sqr1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //////////////////////  Bean Informaiton methods  ////////////////
    
    public String getName() {
        return "SenseQueryHandler";
    }

    public String getVersion() {
        return "1.0";
    }

    public String getDescription() {
        return "Searchbox handler based on latent semantics";
    }

    public Category getCategory() {
        return Category.QUERYHANDLER;
    }

    public String getSource() {
        return "";
    }

    public URL[] getDocs() {
        try {
            return new URL[]{new URL("http://www.searchbox.com")};
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    public NamedList getStatistics() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
