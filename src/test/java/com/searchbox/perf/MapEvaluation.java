/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.perf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 *
 * @author gamars
 */
public class MapEvaluation {

    static class SparseEntry implements Comparable<SparseEntry> {

        int col;
        double value;

        SparseEntry(int c, double v) {
            this.col = c;
            this.value = v;
        }

        public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(col).
            
            toHashCode();
    }
        
        public int compareTo(SparseEntry t) {
            return  t.col - col;
        }
    }

    public static void main(String... args) {
        int max = 150;
        int tmax = 100000;
        int ttest = 100;

        //Pseudo CKB
        System.out.println("Making Psuedo CKB with " + tmax + " terms");
        Map<String, TreeSet<SparseEntry>> pseudockb = new TreeMap<String, TreeSet<SparseEntry>>();
        for (int i = 0; i < tmax; i++) {
            TreeSet<SparseEntry> entries = new TreeSet<SparseEntry>();
            for (int j = 0; j < max; j++) {
                if (Math.random() >= 0.8) {
                    entries.add(new SparseEntry(j, Math.random()));
                }
            }
            pseudockb.put(System.currentTimeMillis() + "_" + i, entries);
        }
        System.out.println("build CKB with " + pseudockb.keySet().size() + " terms");



        //Pseudo TFmap
        Map<String, Integer> termFreqMap = new HashMap<String, Integer>();
        for (String term : pseudockb.keySet()) {
            if (Math.random() >= 0.97) {
                termFreqMap.put(term, 1);
            }
        }
        System.out.println("build pseudo document with " + termFreqMap.keySet().size() + " terms");


        // Test I
        System.out.println("Vectorizing with test I");
        double[] vector = new double[max];
        long start = System.currentTimeMillis();
        for (int i = 0; i < ttest; i++) {
            Iterator<Entry<String, Integer>> iter = termFreqMap.entrySet().iterator();
            while(iter.hasNext()){
                Entry<String, Integer> stuff = iter.next();
                Iterator<SparseEntry> speit = pseudockb.get(stuff.getKey()).iterator();
                while(speit.hasNext()){
                    SparseEntry spe = speit.next();
                    vector[spe.col] += spe.value * stuff.getValue();
                }
            }
        }
        System.out.println("vectored test1 (10 times) in: " + (System.currentTimeMillis() - start) + "ms");

        
        

        Map<String, int[]> col = new HashMap<String, int[]>();
        Map<String, double[]> val = new HashMap<String, double[]>();
        for (Entry<String, TreeSet<SparseEntry>> ckb : pseudockb.entrySet()) {
            col.put(ckb.getKey(), new int[ckb.getValue().size()]);
            val.put(ckb.getKey(), new double[ckb.getValue().size()]);
            int count = 0;
            for (SparseEntry spe : ckb.getValue()) {
                col.get(ckb.getKey())[count] = spe.col;
                val.get(ckb.getKey())[count] = spe.value;
                count++;
            }
        }

        // Test II
        System.out.println("Vectorizing with test II");
        double[] vector2 = new double[max];
        long start2 = System.currentTimeMillis();
        for (int c = 0; c < ttest; c++) {
            for (Entry<String, Integer> tf : termFreqMap.entrySet()) {
                final String term = tf.getKey();
                final double[] termValues = val.get(term);
                if (termValues == null) {
                    continue;
                }
                final int[] termCols = col.get(term);
                final double termFrequencies = tf.getValue();
                for (int i = 0; i < termCols.length; i++) {
                    vector2[termCols[i]] += termFrequencies * termValues[i];
                }
            }
        }
        System.out.println("vectored test2 (10 times) in: " + (System.currentTimeMillis() - start2) + "ms");
        
        
        
        
        Map<String, LinkedList<Integer>> cols = new HashMap<String, LinkedList<Integer>>();
        Map<String, LinkedList<Double>> vals = new HashMap<String, LinkedList<Double>>();
        for (Entry<String, TreeSet<SparseEntry>> ckb : pseudockb.entrySet()) {
            LinkedList<Integer> ccols = new LinkedList<Integer>();
            LinkedList<Double> vvals = new LinkedList<Double>();
            for (SparseEntry spe : ckb.getValue()) {
                ccols.add(spe.col);
                vvals.add(spe.value);
            }
            cols.put(ckb.getKey(), ccols);
            vals.put(ckb.getKey(), vvals);
            
        }

        // Test II
        System.out.println("Vectorizing with test II");
        double[] vector5 = new double[max];
        long start5 = System.currentTimeMillis();
        Iterator<Double> vvals;
        Iterator<Integer> ccols;
        for (int c = 0; c < ttest; c++) {
            for (Entry<String, Integer> tf : termFreqMap.entrySet()) {
                final String term = tf.getKey();
                vvals = vals.get(term).iterator();
                ccols = cols.get(term).iterator();
                final double termFrequencies = tf.getValue();

                while(ccols.hasNext()){
                    vector5[ccols.next()] += termFrequencies * vvals.next();
                }
               
            }
        }
        System.out.println("vectored test5 (10 times) in: " + (System.currentTimeMillis() - start5) + "ms");
        
        
        
        
        
        Map<String, Integer[]> coll = new HashMap<String, Integer[]>();
        Map<String, Double[]> vall = new HashMap<String, Double[]>();
        for(Entry<String, LinkedList<Integer>> _col:cols.entrySet()){
          coll.put(_col.getKey(), _col.getValue().toArray(new Integer[0]));  
        }
        for(Entry<String, LinkedList<Double>> _val:vals.entrySet()){
          vall.put(_val.getKey(), _val.getValue().toArray(new Double[0]));  
        }
       
       // Test II
        System.out.println("Vectorizing with test II");
        vector2 = new double[max];
        start2 = System.currentTimeMillis();
        for (int c = 0; c < ttest; c++) {
            for (Entry<String, Integer> tf : termFreqMap.entrySet()) {
                final String term = tf.getKey();
                final Double[] termValues = vall.get(term);
                if (termValues == null) {
                    continue;
                }
                final Integer[] termCols = coll.get(term);
                final double termFrequencies = tf.getValue();
                for (int i = 0; i < termCols.length; i++) {
                    vector2[termCols[i]] += termFrequencies * termValues[i];
                }
            }
        }
        System.out.println("vectored test2 (10 times) in: " + (System.currentTimeMillis() - start2) + "ms");
        
        
        
        
        
        for(int i =0; i<max; i++)
            System.out.print(vector[i]+", ");
        
        System.out.println();
        for(int i =0; i<max; i++)
            System.out.print(vector5[i]+", ");
//        
    }
}
