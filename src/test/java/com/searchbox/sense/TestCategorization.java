/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.math.RealTermFreqVector;
import org.apache.mahout.math.Matrix;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * @author andrew
 */
public class TestCategorization {
  protected static final Logger LOGGER = LoggerFactory.getLogger(TestCategorization.class);

  @Test
  public void testCategorization() {
    List<RealTermFreqVector> prototypetfs = new ArrayList<RealTermFreqVector>();

    Map<String, Float> doc1 = new HashMap<String, Float>();
    doc1.put("Robot", 6.0f);
    doc1.put("Needle", 1.0f);
    doc1.put("Spiral", 10.0f);
    doc1.put("Worm", 4.0f);
    doc1.put("Mouth", 5.0f);
    doc1.put("Cave", 4.0f);
    doc1.put("Gas", 10.0f);
    doc1.put("Butterfly", 3.0f);
    doc1.put("Foot", 8.0f);
    doc1.put("Balloon", 7.0f);
    prototypetfs.add(new RealTermFreqVector(doc1));


    Map<String, Float> doc2 = new HashMap<String, Float>();
    doc2.put("Solid", 6.0f);
    doc2.put("Needle", 1.0f);
    doc2.put("Spiral", 10.0f);
    doc2.put("Worm", 4.0f);
    doc2.put("Water", 5.0f);
    doc2.put("Sphere", 4.0f);
    doc2.put("Vampire", 10.0f);
    doc2.put("Album", 3.0f);
    doc2.put("Foot", 8.0f);
    doc2.put("Knife", 7.0f);
    prototypetfs.add(new RealTermFreqVector(doc2));


    Map<String, Float> doc3 = new HashMap<String, Float>();
    doc3.put("Robot", 6.0f);
    doc3.put("Tongue", 1.0f);
    doc3.put("Chair", 10.0f);
    doc3.put("Finger", 4.0f);
    doc3.put("Tiger", 5.0f);
    doc3.put("Circle", 4.0f);
    doc3.put("Gas", 10.0f);
    doc3.put("Butterfly", 3.0f);
    doc3.put("Air", 8.0f);
    doc3.put("Skeleton", 7.0f);
    prototypetfs.add(new RealTermFreqVector(doc3));


    CategorizationBase category1 = new CategorizationBase(prototypetfs);

    LOGGER.info(category1.getTdic().toString());

    Matrix Model = category1.getModel();
    LOGGER.info("Produced a model of size [  " + Model.numRows() + " , " + Model.numCols() + " ]");
    for (int r = 0; r < Model.numRows(); r++) {
      for (int c = 0; c < Model.numCols(); c++) {
        System.out.print(Model.getQuick(r, c) + "\t");
      }
      System.out.print("\n");
    }

    double[] singularValues_groundtruth = {1.247996556975199, 1.00, 0.665210187668565};
    double[] singularValues = category1.getSingularValues();
    float totalerr = 0;
    for (int zz = 0; zz < singularValues.length; zz++) {
      totalerr += Math.abs(singularValues[zz] - singularValues_groundtruth[zz]);
    }

    LOGGER.info("Singular value error is: \t" + totalerr);
    assertTrue("Singular Values don't match!", totalerr < .000001);

    double modelScore = category1.getModelScore();
    LOGGER.info("Model Score: \t" + modelScore);
    assertTrue("Model score is too high!", modelScore < .000001);

    Map<String, Float> queryDoc = new HashMap<String, Float>();
    queryDoc.put("Robot", 3.0f);
    queryDoc.put("Needle", 2.0f);
    queryDoc.put("Spiral", 1.0f);
    queryDoc.put("Worm", 7.0f);
    queryDoc.put("Mouth", 3.0f);
    queryDoc.put("Cave", 2.0f);
    queryDoc.put("Gas", 2.0f);
    queryDoc.put("Butterfly", 4.0f);
    queryDoc.put("Foot", 9.0f);
    queryDoc.put("Balloon", 4.0f);
    queryDoc.put("Tongue", 4.0f);
    queryDoc.put("Circle", 4.0f);

    double score = category1.categorize(new RealTermFreqVector(queryDoc));
    LOGGER.info("Query Score is: \t" + score);
    assertTrue("Categorization Score is wrong!", Math.abs(score - .510362139267438) < .00001);

    queryDoc.put("UNKNOWN", 15.0f);
    score = category1.categorize(new RealTermFreqVector(queryDoc));
    LOGGER.info("Query Score is: \t" + score);
    assertTrue("Categorization Score is wrong when term missing from tdic is present!", Math.abs(score - .755181069633719) < .00001);

  }


  @Test
  public void testCategorizationSpeed() {
    List<RealTermFreqVector> prototypetfs = new ArrayList<RealTermFreqVector>();


    for (int zz = 0; zz < 10; zz++) {
      Map<String, Float> doc = new HashMap<String, Float>();
      for (int numtermsi = 0; numtermsi < 80; numtermsi++) {
        doc.put(UUID.randomUUID().toString(), new Float(Math.random() * 10));
      }
      prototypetfs.add(new RealTermFreqVector(doc));
    }


    long startTime = System.nanoTime();
    CategorizationBase category1 = new CategorizationBase(prototypetfs);
    long endTime = System.nanoTime();

    long duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
    LOGGER.info("Time (in ms) to make a 10 dimensional model with 10 documents and 80 unique terms per document: \t" + duration);


    String[] values = category1.getTdic().keySet().toArray(new String[category1.getTdic().size()]);

    startTime = System.nanoTime();
    double score = 0;
    for (int zz = 0; zz < 1000; zz++) {
      Map<String, Float> queryDoc = new HashMap<String, Float>();
      for (int numtermsi = 0; numtermsi < 80; numtermsi++) {   //randomly build query document from existing terms
        queryDoc.put(values[(int) Math.floor(Math.random() * values.length)], new Float(Math.random() * 10));
      }
      score = category1.categorize(new RealTermFreqVector(queryDoc));
    }
    endTime = System.nanoTime();
    duration = TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS);
    LOGGER.info("Time to run 1000 (ms) categorizations: \t" + duration);

  }


}
