/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.commons.params;

/**
 *
 * @author gamars
 */
public class SenseParams {
    
    public static String SENSE_WEIGHT = "sw";
    public static float DEFAULT_SENSE_WEIGHT = 0.8f;

    public static String SENSE_FIELD = "sf";
    public static String DEFAULT_SENSE_FIELD = "article-abstract";
    
    public static String SENSE_QPARSER = "sense";
    
    public static String SENSE_CKB_DEFAULT="1";
    public static String SENSE_CKB = "ckb";
    
    public static String SENSE_MINDOC4QR="mdq";
    public static int SENSE_MINDOC4QR_DEFAULT=10000;
    
    public static String SENSE_QR_NTU = "ntu";
    //public static int SENSE_QR_NTU_DEFAULT = -1;
    public static int SENSE_QR_NTU_DEFAULT = 5;
    
    public static String SENSE_QR_THRESH = "ntt";
    //public static int  SENSE_QR_THRESH_DEFAULT = 500;
    public static int  SENSE_QR_THRESH_DEFAULT = 5000;
    
    
    public static String SENSE_QR_MAXDOC = "nmd";
    //public static int  SENSE_QR_MAXDOC_DEFAULT = 10000;
    public static int  SENSE_QR_MAXDOC_DEFAULT = 5000;
                    
}
