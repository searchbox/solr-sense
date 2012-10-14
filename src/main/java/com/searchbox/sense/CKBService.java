/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.solr.SenseQParserPlugin;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author gamars
 */
public class CKBService {
  
  private HashMap<String, String> ckbs;

  public CKBService() {
    this.ckbs = new HashMap<String, String>();
  }

  public String getCKB(String name) {
    if (!ckbs.containsKey(name)) {
      System.out.println("Loading CKB: " + name);
      try {
        Thread.sleep(5000);
      } catch (InterruptedException ex) {
        Logger.getLogger(CKBService.class.getName()).log(Level.SEVERE, null, ex);
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
        Logger.getLogger(CKBService.class.getName()).log(Level.SEVERE, null, ex1);
      }
    }
    
    CKBService ckbsrv;
    try {
      ckbsrv = (CKBService) sbCtx.lookup("bean/CKBService");
    } catch (NamingException ex) {
      System.out.println("Creating new CKBService in JNDI");
      ckbsrv = new CKBService();
      try {
        sbCtx.bind("bean/CKBService", ckbsrv);
      } catch (NamingException ex1) {
        Logger.getLogger(CKBService.class.getName()).log(Level.SEVERE, null, ex1);
      }
    }
    
    return ckbsrv;
  }
}
