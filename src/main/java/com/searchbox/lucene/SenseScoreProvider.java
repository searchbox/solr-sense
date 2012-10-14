/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.lucene;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.queries.CustomScoreProvider;

/**
 *
 * @author gamars
 */
public class SenseScoreProvider extends CustomScoreProvider {

  SenseScoreProvider(AtomicReaderContext context) {
    super(context);
  }
}
