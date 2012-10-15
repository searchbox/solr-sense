/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.math.DoubleFullVector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author gamars
 */
public class CognitiveKnowledgeBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(CognitiveKnowledgeBase.class);

    enum Type {

        SPARSE,
        FULL
    }
    private final double certainyValue;
    private final double maximumDistance;
    private final String name;
    private final Map<String, int[]> col;
    private final Map<String, double[]> val;
    private final int dimentionality;
    private final Map<String, Double> idf;

    private CognitiveKnowledgeBase(final String name, final Map<String, int[]> col,
            final Map<String, double[]> val, final Map<String, Double> idf, final int dimentionality,
            final double certainyValue, final double maximumDistance) {
        this.name = name;
        this.col = col;
        this.val = val;
        this.dimentionality = dimentionality;
        this.certainyValue = certainyValue;
        this.maximumDistance = maximumDistance;
        this.idf = idf;
    }

    //TODO make a recycable class for Integer (cf MLT).
    public DoubleFullVector getFullCkbVector(final Map<String, Integer> termFreqMap) {
        double[] vector = new double[this.getColumnDimension()];
        for (Entry<String, Integer> tf : termFreqMap.entrySet()) {
            final String term = tf.getKey();
            final double[] termValues = val.get(term);
            if (termValues == null) {
                continue;
            }
            final int[] termCols = col.get(term);
            final double termFrequencies = tf.getValue();
            for (int i = 0; i < termCols.length; i++) {
                vector[termCols[i]] += termFrequencies * termValues[i];
            }
        }
        return new DoubleFullVector(vector);
    }

    public double computeSimilarity(DoubleFullVector q, DoubleFullVector t) {
        return q.getDistance(t);
    }

    private double getIdf(final String stem) {
        final Double val = idf.get(stem);
        return val == null ? 0.0 : val;
    }

    // Explicit number of terms present in CKB
    private int getRowDimension() {
        return col.keySet().size();
    }

    private int getColumnDimension() {
        return this.dimentionality;
    }

    public static CognitiveKnowledgeBase loadSparseCKB(String name, Type type, String directory,
            String modelFile, String idfFile, String dictionary,
            double certainyValue, double maximumDistance) {
        CognitiveKnowledgeBase ckb = null;
        BufferedReader in = null;
        try {
            ArrayList<String> terms = CognitiveKnowledgeBase.loadDictionary(new File(directory + modelFile));
            final Map<String, List<Integer>> col = new HashMap<String, List<Integer>>();
            final Map<String, List<Double>> val = new HashMap<String, List<Double>>();

            //Init the col and val data struct to save time later...
            for (String term : terms) {
                col.put(term, new ArrayList<Integer>());
                val.put(term, new ArrayList<Double>());
            }

            in = new BufferedReader(new InputStreamReader(new FileInputStream(directory + modelFile)));
            final String metaLine = in.readLine();
            if (metaLine == null) {
                throw new RuntimeException("Can't read first line of Sparse CKB file");
            }

            final String[] parameters = metaLine.split("\\s");
            int nrow = Integer.valueOf(parameters[0]); //shoudl be equal to terms.size();
            int ncol = Integer.valueOf(parameters[1]); //this is dim

            final Map<String, List<Entry>> values = new HashMap<String, List<Entry>>();
            while (true) {
                final String line = in.readLine();
                if (line == null) {
                    break;
                }

                final String[] dline = line.split("\\s");
                if (dline.length != 3) {
                    LOGGER.warn("Line should contain 3 values!!!");
                    continue;
                }

                final String term = terms.get(Integer.valueOf(dline[0]));
                col.get(term).add(Integer.valueOf(dline[1]));
                val.get(term).add(Double.valueOf(dline[2]));
            }

            final Map<String, int[]> coll = new HashMap<String, int[]>();
            final Map<String, double[]> vall = new HashMap<String, double[]>();
            for (Entry<String, List<Integer>> _col : col.entrySet()) {
                int[] tcol = new int[_col.getValue().size()];
                int count = 0;
                for(Integer i:_col.getValue()){
                    tcol[count++] = i.intValue();
                }
                coll.put(_col.getKey(), tcol);
            }
            for (Entry<String, List<Double>> _val : val.entrySet()) {
                double[] tval = new double[_val.getValue().size()];
                int count = 0;
                for(Double i:_val.getValue()){
                    tval[count++] = i.doubleValue();
                }
                vall.put(_val.getKey(), tval);
            }
            col.clear();
            val.clear();

            ckb = new CognitiveKnowledgeBase(name, coll, vall, null, ncol, certainyValue, maximumDistance);
            
        } catch (IOException e) {
            throw new RuntimeException("DLines can not be read.", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error("File can not be closed.", e);
                }
            }
        }
        return ckb;
            
    }

    private static Map<String, Double> loadIdf(File idfFile) {
        return null;
    }

    private static ArrayList<String> loadDictionary(File dictionary) {
        BufferedReader fin = null;
        ArrayList<String> terms = new ArrayList<String>();
        try {
            fin = new BufferedReader(new InputStreamReader(new FileInputStream(dictionary), "UTF-8"));
            String line = "";
            int count = 0;
            while ((line = fin.readLine()) != null) {
                String data[] = line.split("\t");
                count++;

                //tdic files from export come of the following format index stem terms.
                if (data.length > 2) {
                    terms.add(data[1]);
                } //tdic files form MATLAB are 1 based...
                else if (data.length == 2) {
                    terms.add(data[1]);//Integer.parseInt(data[0]) - 1);
                } else if (data.length == 1) {
                    LOGGER.debug(data[0] + " with empty term");
                    terms.add(data[0]);
                }
            }
            LOGGER.debug("READ " + count + " lines and added " + terms.size() + " stems.");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Dictionary can not be created.", e);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Dictionary can not be created.", e);
        } catch (IOException e) {
            throw new RuntimeException("Dictionary can not be created.", e);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return terms;
    }
}
