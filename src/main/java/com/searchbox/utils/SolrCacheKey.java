/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.utils;


import org.apache.solr.common.params.SolrParams;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author andrew
 */
public class SolrCacheKey {

  //private HashMap<String,String> keyval = new HashMap<String,String>();
  private HashSet<String> keyval = new HashSet<String>();

  public SolrCacheKey(SolrParams params, Set<String> toIgnore) {
    String key;
    Iterator<String> keys = params.getParameterNamesIterator();
    while (keys.hasNext()) {
      key = keys.next();
      if (!toIgnore.contains(key)) {
        keyval.add(key + ":" + params.get(key));
      }
    }
  }

  public Set<String> getSet() {
    return keyval;
  }


}
