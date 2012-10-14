/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gamars
 */
public class CKBUtils {

    private static HashMap<String, String> ckbs = loadCKBs();
    private static boolean initialized = false;

    private CKBUtils() {
    }

    private static final HashMap<String, String> loadCKBs() {
        HashMap<String, String> ckbs = new HashMap<String, String>();
        try {        
          System.out.println("Loading CKB...");
          Thread.sleep(200);
          ckbs.put("test", System.currentTimeMillis()+"");
          CKBUtils.initialized = true;
        } catch (InterruptedException ex) {
            Logger.getLogger(CKBUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ckbs;
    }
    
   
    public static String getCKB(String name) {
        return ckbs.get(name);
    }
}
