/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.math.DoubleFullVector;
import com.searchbox.sense.CognitiveKnowledgeBase;
import com.searchbox.utils.SystemUtils;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gamars
 */
public class TestCognitiveKnowledgeBase extends TestCase {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CognitiveKnowledgeBase.class);
    private CognitiveKnowledgeBase ckb;

    public TestCognitiveKnowledgeBase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.ckb = CognitiveKnowledgeBase.loadSparseCKB("test",
                "./src/test/resources/CKB/pubmed/",
               "pubmed.cache", "pubmed.idflog", "pubmed.tdic", 1.0, 1.0);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    
    public void testLoadingCKB(){
        assertTrue("No terms has been loaded", ckb.getTerms().size() > 0);
        assertTrue("CKB has no dimensions", ckb.getDimentionality()>0);
    }
    
    public void testCKBVector(){
        Map<String, Integer> tf = new HashMap<String, Integer>();
        tf.put("hello", 1);
        tf.put("world", 1);
        DoubleFullVector vector = ckb.getFullCkbVector(tf);
        System.out.println("Got vector of dimension: " + vector.getDimension());
        assertTrue("Vector has null dimention",vector.getDimension()>0);
        assertTrue("Vector has different dim than CKB", vector.getDimension() == ckb.getDimentionality());
        
        for(double d:vector.getData())
            System.out.print(d+", ");
        System.out.println();
    }
}
