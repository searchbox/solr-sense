/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.utils;

/**
 *
 * @author gamars
 */
public class SystemUtils {
    
    public static String getMemoryUsage(){         
        int mb = 1024*1024;
         
        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
         
        String usage = "##### Heap -- ";
         
        //Print used memory
        usage += "used: " + (runtime.totalMemory() - runtime.freeMemory()) / mb;
 
        //Print free memory
        usage += ", free: " + runtime.freeMemory() / mb;
         
        //Print total available memory
        usage += ", available: " + runtime.totalMemory() / mb;
 
        //Print Maximum available memory
        usage += ", max: " + runtime.maxMemory() / mb;
        
        return usage;
   
    }
}
