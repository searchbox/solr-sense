/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.solr.SenseQParserPlugin;
import java.util.HashMap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author gamars
 */
public class CKBService {
  
    private static final Logger LOGGER = LoggerFactory.getLogger(CKBService.class);
    private HashMap<String, String> ckbs;

  public CKBService() {
    this.ckbs = new HashMap<String, String>();
  }

  public String getCKB(String name) {
    if (!ckbs.containsKey(name)) {
      LOGGER.info("Loading CKB: " + name);
      try {
        Thread.sleep(5000);
      } catch (InterruptedException ex) {
        LOGGER.error(ex.toString());
      }
      this.ckbs.put(name, System.currentTimeMillis()+"");
    }
    return ckbs.get(name);
  }
  
  public static CKBService getCKBService() {
    Context sbCtx = null;
    Context initCtx = null;
    try {          
      initCtx = new InitialContext();
    } catch (NamingException ex) { }
    
    try {
      sbCtx = (Context)initCtx.lookup("searchbox");
    } catch (NamingException ex) {
      try {
        sbCtx = initCtx.createSubcontext("searchbox");
      } catch (NamingException ex1) {
        LOGGER.error(ex1.toString());
      }
    }
    
    CKBService ckbsrv;
    try {
      ckbsrv = (CKBService) sbCtx.lookup("bean/CKBService");
    } catch (NamingException ex) {
      LOGGER.info("Creating new CKBService in JNDI");
      ckbsrv = new CKBService();
      try {
        sbCtx.bind("bean/CKBService", ckbsrv);
      } catch (NamingException ex1) {
          LOGGER.error(ex1.toString());
      }
    }
    
    return ckbsrv;
  }
}
