/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.math.DoubleFullVector;
import com.searchbox.math.RealTermFreqVector;
import com.searchbox.utils.SystemUtils;
import com.searchbox.utils.TermFreqContainer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

   

    public enum Type {
        SPARSE,
        FULL
    }
    
    private final float certainyValue;
    private final float maximumDistance;
    private final String name;
    private final int dimentionality;

    private final Map<String, int[]> col;
    private final Map<String, float[]> val;
    private final Map<String, Float> idf;

    private CognitiveKnowledgeBase(final String name, final Map<String, int[]> col,
            final Map<String, float[]> val, final Map<String, Float> idf, final int dimentionality,
            final float certainyValue, final float maximumDistance) {
        this.name = name;
        this.col = col;
        this.val = val;
        this.dimentionality = dimentionality;
        this.certainyValue = certainyValue;
        this.maximumDistance = maximumDistance;
        this.idf = idf;
    }

    public float getCertainyValue() {
        return certainyValue;
    }

    public float getMaximumDistance() {
        return maximumDistance;
    }

    public String getName() {
        return name;
    }

    public int getDimentionality() {
        return dimentionality;
    }   
    
    public int getDictionarySize() {
        return this.getRowDimension();
    }   
    
    public Collection<String> getTerms(){
        return this.col.keySet();
    }

    //TODO make a recycable class for Integer (cf MLT).
    public DoubleFullVector getFullCkbVector(final Map<String, Integer> termFreqMap) {
        float[] vector = new float[this.getColumnDimension()];
        for (Entry<String, Integer> tf : termFreqMap.entrySet()) {
            final String term = tf.getKey();
            final float[] termValues = val.get(term);
            if (termValues == null) {
                continue;
            }
            final int[] termCols = col.get(term);
            final float termFrequencies = tf.getValue();
            for (int i = 0; i < termCols.length; i++) {
                vector[termCols[i]] += termFrequencies * termValues[i];
            }
        }
        return new DoubleFullVector(vector);
    }
    
    
    public DoubleFullVector getFullCkbVector(final TermFreqContainer tfc) {
        float[] vector = new float[this.getColumnDimension()];
        for (int zz=0;zz<tfc.getSize() ; zz++) {
            String term= tfc.getTerms()[zz];
            final float[] termValues = val.get(term);
            if (termValues == null) {
                continue;
            }
            final int[] termCols = col.get(term);
            final float termFrequencies = tfc.getFreqs()[zz];
            for (int i = 0; i < termCols.length; i++) {
                vector[termCols[i]] += termFrequencies * termValues[i];
            }
        }
        return new DoubleFullVector(vector);
    }
    
    

    public RealTermFreqVector getTfIdfVector(Map<String, Integer> termFreqMap) {
        Map<String, Float> termFreqMapOut= new HashMap<String,Float>();
        for (Entry<String, Integer> tf : termFreqMap.entrySet()) {
            String key = tf.getKey();
            Float val=idf.get(key);
            if (val!=null) {
                termFreqMapOut.put(key, new Float(tf.getValue()*val));
            }
        }
        return new RealTermFreqVector(termFreqMapOut);
    }
    
    
    public RealTermFreqVector getTfIdfVector(TermFreqContainer tfc) {
        TermFreqContainer out = new TermFreqContainer(tfc.getSize());
        for (int zz=0;zz<tfc.getSize();zz++) {
            String key = tfc.getTerms()[zz];
            Float val=idf.get(key);
            if (val!=null) {
                out.set(key, tfc.getFreqs()[zz]*val, zz);
            }
        }
        return new RealTermFreqVector(tfc);
    }
    

    public float computeSimilarity(DoubleFullVector q, DoubleFullVector t) {
        return q.getDistance(t);
    }

    private float getIdf(final String stem) {
        final Float val = idf.get(stem);
        return val == null ? 0.0f : val;
    }

    // Explicit number of terms present in CKB
    private int getRowDimension() {
        return col.keySet().size();
    }

    private int getColumnDimension() {
        return this.dimentionality;
    }

    public static CognitiveKnowledgeBase loadSparseCKB(String name, String baseDirectory,
            String modelFile, String idfFile, String dictionaryFile,
            float certainyValue, float maximumDistance) {
        CognitiveKnowledgeBase ckb = null;
        BufferedReader in = null;
        try {
            
            LOGGER.info("Loadign CKB dictionary data from: " + baseDirectory + dictionaryFile);
            ArrayList<String> terms = CognitiveKnowledgeBase.loadDictionary(new File(baseDirectory + dictionaryFile));
            LOGGER.info("Dictionary loaded with " + terms.size() + " terms.");
            Map<String, List<Integer>> col = new HashMap<String, List<Integer>>();
            Map<String, List<Float>> val = new HashMap<String, List<Float>>();

            //Init the col and val data struct to save time later...
            for (String term : terms) {
                col.put(term, new ArrayList<Integer>());
                val.put(term, new ArrayList<Float>());
            }
            
            LOGGER.info("Loadign CKB data from: " + baseDirectory + modelFile);
            in = new BufferedReader(new InputStreamReader(new FileInputStream(baseDirectory + modelFile)));
            final String metaLine = in.readLine();
            if (metaLine == null) {
                throw new RuntimeException("Can't read first line of Sparse CKB file");
            }

            final String[] parameters = metaLine.split("\\s");
            int nrow = Integer.valueOf(parameters[0]); //shoudl be equal to terms.size();
            int ncol = Integer.valueOf(parameters[1]); //this is dim
            LOGGER.info("Readign CKB data with rows: " + nrow+", cols:"+ncol);

            String line;
            int ival = 0;
            while ((line = in.readLine())!=null) {
                final String[] dline = line.split("\\s");
                if (dline.length != 3) {
                    LOGGER.warn("Line should contain 3 values!!!");
                    continue;
                }

                final String term = terms.get(Integer.valueOf(dline[0]));
                col.get(term).add(Integer.valueOf(dline[1]));
                val.get(term).add(Float.valueOf(dline[2]));
                ival++;
            }
            LOGGER.info("Loaded " + ival + " values in CKB");

            final Map<String, int[]> coll = new HashMap<String, int[]>();
            final Map<String, float[]> vall = new HashMap<String, float[]>();
            
            LOGGER.info("Transforming col array");
            for (Entry<String, List<Integer>> _col : col.entrySet()) {
                int[] tcol = new int[_col.getValue().size()];
                int count = 0;
                for (Integer i : _col.getValue()) {
                    tcol[count++] = i.intValue();
                }
                coll.put(_col.getKey(), tcol);
            }
            
            LOGGER.info("Transforming val array");
            for (Entry<String, List<Float>> _val : val.entrySet()) {
                float[] tval = new float[_val.getValue().size()];
                int count = 0;
                for (Float i : _val.getValue()) {
                    tval[count++] = i.floatValue();
                }
                vall.put(_val.getKey(), tval);
            }
            
            LOGGER.info("Transformation done. Clearing temp objects.");
            col.clear();
            val.clear();
            col=null;
            val=null;

            LOGGER.info("Loadign idf data from: " + baseDirectory + idfFile);
            
            Map<String, Float> idf  = CognitiveKnowledgeBase.loadIdf(new File(baseDirectory + idfFile));
            
            LOGGER.info("Done. idf has size: " + idf.size());
            
            LOGGER.info(SystemUtils.getMemoryUsage());

            ckb = new CognitiveKnowledgeBase(name, coll, vall, idf, ncol, certainyValue, maximumDistance);


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
            throw new RuntimeException("Dictionary file could not be found in: " + dictionary.getAbsolutePath(), e);
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

    

    private static Map<String, Float> loadIdf(File idfFile) {
        Map<String, Float> idf = new HashMap<String, Float>();
        try {
     
            LOGGER.debug("Loading idf from file: " + idfFile.getName());
            BufferedReader fin = null;
            try {
                fin = new BufferedReader(new FileReader(idfFile));
                String idfline;
                while ((idfline = fin.readLine()) != null) {
                    final String[] parts = idfline.trim().split("\\s");
                    final String term = parts[0];
                    final float value = Float.parseFloat(parts[1]);
                    idf.put(term, value);
                }
            } catch (IOException e) {
                LOGGER.error("IDF File can not be read. Returning default IDF vector");
            } finally {
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e) {
                        LOGGER.error("IDF File can not be closed.");
                    }
                }
            }
        } finally {
            LOGGER.info("Loaded " + idf.size() + " items.");
        }
        return idf;
    }
}
