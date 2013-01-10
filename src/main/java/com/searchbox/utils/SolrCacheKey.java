/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.utils;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.solr.common.params.SolrParams;

/**
 *
 * @author andrew
 */
public class SolrCacheKey {

    //private HashMap<String,String> keyval = new HashMap<String,String>();
    private HashSet<String> keyval = new HashSet<String>();
    
    public SolrCacheKey(SolrParams params) {
        String key;
        Iterator<String> keys=params.getParameterNamesIterator();
        while(keys.hasNext()){
            key=keys.next();
            if(key.compareToIgnoreCase("start")!=0&&key.compareToIgnoreCase("rows")!=0&&key.compareToIgnoreCase("fl")!=0){
                keyval.add(key+":"+params.get(key));
            }
        }
    }
    
    public Set<String> getSet(){
        return keyval;
    }
    
    
}
