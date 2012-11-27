/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.lucene;

import com.searchbox.sense.CategorizationBase;
import java.io.IOException;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.CustomScoreProvider;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author andrew
 */
public class CategoryQuery extends CustomScoreQuery {
    public static final Logger LOGGER = LoggerFactory.getLogger(CategoryQuery.class);
    CategorizationBase model;
    IndexReader reader;
    String senseField;
    
    public static CategoryQuery CategoryQueryForDocument(Query filter,CategorizationBase model, DirectoryReader indexReader,String senseField) {
        return new CategoryQuery(filter,model,indexReader,senseField);
    }

    private CategoryQuery(Query filter, CategorizationBase model, DirectoryReader indexReader, String senseField) {
        super(filter);
        this.model=model;
        this.reader=indexReader;
        this.senseField=senseField;
    }
     
    
    @Override
    protected CustomScoreProvider getCustomScoreProvider(AtomicReaderContext context) throws IOException {
        System.out.println("Setting up custom score provider.");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Overriding ScoreProvider for IndexReader " + context);
        }
        return new CategoryScoreProvier(context,model,senseField);
    }
}
