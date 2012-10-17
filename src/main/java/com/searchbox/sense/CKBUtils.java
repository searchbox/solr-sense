/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import java.util.HashMap;

import org.apache.solr.common.util.NamedList;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author gamars
 */
public class CKBUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CKBUtils.class);

    private static HashMap<String, String> ckbs = new HashMap<String, String>();

    private CKBUtils() {
    }
   
    public static String getCKB(String name) {
        return ckbs.get(name);
    }

    public static void initCKB(NamedList namedList) {
        String name = (String) namedList.get("name");
        
//                        <str name="locale">en</str>

        LOGGER.info("Loading CKB["+name+"]"); 
    }
}
