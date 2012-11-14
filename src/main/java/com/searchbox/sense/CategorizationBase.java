/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.math.RealTermFreqVector;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.RandomAccessSparseVector;
import org.apache.mahout.math.SingularValueDecomposition;
import org.apache.mahout.math.SparseMatrix;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.function.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author andrew
 */
public class CategorizationBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CategorizationBase.class);

    private HashMap<String,Integer> tdic ;
    private Matrix Model;
    private double ModelScore;
    private double[] singularValues;

    public double[] getSingularValues() {
        return singularValues;
    }

    
    public HashMap<String, Integer> getTdic() {
        return tdic;
    }

    public void setTdic(HashMap<String, Integer> tdic) {
        this.tdic = tdic;
    }

    public Matrix getModel() {
        return Model;
    }

    public void setModel(Matrix Model) {
        this.Model = Model;
    }
    
    
    public double getModelScore() {
        return ModelScore;
    }

    public  CategorizationBase(List<RealTermFreqVector> prototypetfs) {
        if(prototypetfs.size()>10){
            LOGGER.warn("Category model number of prototype documents is greater than 10! Using only 10");
    }
        
       /* uidsn=calcnormq(tf);
        indxtoremove=sum(uidsn>0,1)==0;
        uidsn(:,indxtoremove)=[];*/
        
        Map<Integer, RandomAccessSparseVector> rowVectors=new HashMap<Integer,RandomAccessSparseVector>();
        tdic = new HashMap();
        int row=0;
        int col=0;
        for(RealTermFreqVector protod:prototypetfs )
        {
            if(row>=10){
                break;
            }
            RealTermFreqVector protodn=protod.getUnitVector();
            RandomAccessSparseVector rowVector=new RandomAccessSparseVector(10000,10);
            for (Entry<String,Float> term : protodn.getData().entrySet()){
                Integer termcol=tdic.get(term.getKey());
                if(termcol==null) {
                    tdic.put(term.getKey(), col);
                    termcol=col;
                    col++;
                }
                //add to datastructure
                rowVector.setQuick(termcol, term.getValue());

            }
            rowVectors.put(row, rowVector);
            row++;
        }
        
        
        //[U,S,V]=svds(uidsn,min(size(uidsn,1),k));
        SparseMatrix uidsn=new SparseMatrix(row,col,rowVectors);
        SingularValueDecomposition svd=new SingularValueDecomposition(uidsn);
        
     
        //CKB=V*diag(1./diag(S));
        //CKBINV=S*V';
        //since we're doing V*1/S * S*V', we can remove S entirely as it multiplies to create the unity matrix
        
        Model=svd.getV();
        singularValues=svd.getSingularValues();
        
        //score=norm(U*S*V'-uidsn,'fro');
        ModelScore=(svd.getU().times(svd.getS().times(svd.getV().transpose())).minus(uidsn)).aggregate(Functions.PLUS, Functions.SQUARE);    

    }
    
    public double categorize(RealTermFreqVector querytf){
        RealTermFreqVector querytfnormalized=querytf.getUnitVector();
        float missing_err=0;
        
        /*
            lindxtoremove=ones(size(TFVectors,2),1);
            lindxtoremove(~indxtoremove)=0;

            indxtoremove=lindxtoremove>0;

            query=calcnormq(TFVectors);*/
        
        SparseMatrix querym=new SparseMatrix(1,Model.rowSize());
        for (Entry<String,Float> term : querytfnormalized.getData().entrySet()){
                Integer termcol=tdic.get(term.getKey());
                if(termcol==null) {
                    missing_err+=term.getValue()*term.getValue();
                    continue;
                }
                querym.set(0, termcol, term.getValue());
        }
        
        //uunproj=query(:,~indxtoremove)*CKB*CKBINV;
        Vector uunproj=querym.times(Model).times(Model.transpose()).viewRow(0);
        
        //scores=sum((query(:,~indxtoremove)-uunproj).^2,2)+sum(query(:,indxtoremove).^2,2);
        return  querym.viewRow(0).getDistanceSquared(uunproj)+missing_err;
    }
    
    
}
