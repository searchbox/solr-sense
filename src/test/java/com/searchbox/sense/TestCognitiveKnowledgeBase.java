/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.searchbox.sense;

import com.searchbox.SolrUtils;
import com.searchbox.math.DoubleFullVector;
import com.searchbox.math.RealTermFreqVector;
import com.searchbox.solr.EmptySolrTestCase;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author gamars
 */
public class TestCognitiveKnowledgeBase extends EmptySolrTestCase {

    private static CognitiveKnowledgeBase CKB;

    @BeforeClass
    public static void setUp() throws Exception {
        TestCognitiveKnowledgeBase.CKB = CognitiveKnowledgeBase.loadSparseCKB("test",
                "./src/test/resources/CKB/pubmed/", "pubmed.cache",
                "pubmed.idflog", "pubmed.tdic", 1.0f, 1.0f);
    }

    @Test
    public void testLoadingCKB() {
        assertNotNull(CKB);
        assertTrue("No terms has been loaded", CKB.getTerms().size() > 0);
        assertTrue("CKB has no dimensions", CKB.getDimentionality() > 0);
    }

    @Test
    public void testTFcreation() throws SolrServerException, IOException {
        HashMap<String, Integer> ground_truth = new HashMap<String, Integer>();
        ground_truth.put("dog", 1);
        ground_truth.put("fish", 4);
        ground_truth.put("happi", 9);
        ground_truth.put("internet", 1);
        ground_truth.put("larg", 1);
        ground_truth.put("parti", 1);
        ground_truth.put("sad", 1);
        ground_truth.put("small", 3);
        ground_truth.put("studi", 2);
        ground_truth.put("swam", 1);
        ground_truth.put("swim", 2);
        ground_truth.put("unknown", 1);
        ground_truth.put("jump", 3);


        SolrInputDocument doc = new SolrInputDocument();
        String id = System.currentTimeMillis() + "";
        doc.addField("id", id);
        doc.addField("content_srch", "jump jumping jumped swim swimming swam dog fish fish fish happy happy happy happy happy happy happy happy happy sad large small small small unknown party internet studying fish study and the ");
        solrServer.add(doc);
        solrServer.commit();

        LOGGER.info("Getting TF for ONE document (q=id:" + id + ")");
        Map<String, HashMap<String, Integer>> tfms = SolrUtils.getTermFrequencyMapForQuery(solrServer, "content_srch", "id:" + id);
        for (String sid : tfms.keySet()) {
            LOGGER.info("TF for document id#" + sid);
            for (Entry<String, Integer> tf : tfms.get(sid).entrySet()) {
                LOGGER.info("\t" + tf.getKey() + "\t" + tf.getValue());
                assertTrue("TF stemming doesn't match ground truth! [ " + tf.getKey() + " ] ", ground_truth.get(tf.getKey()) == tf.getValue());
            }
        }
    }

    @Test
    public void testCKBVector() {
        Map<String, Integer> tf = new HashMap<String, Integer>();
        /*
         pheser	1	113333
         ccpvdz	2	113270
         snaploop    	3	109249
         preconnect	4	109243
         poseinvari	5	109212
         mulatta	6	109169
         twopap	7	109063
         flexibl	8	14
         fossil	9	16
         group	10	142
         * 
         * 
         * vals=[	
         1	113333
         2	113270
         3	109249
         4	109243
         5	109212
         6	109169
         7	109063
         8	14
         9	16
         10	142
         ];


         tf=zeros(1,length(tdic));
         tf(vals(:,2))=vals(:,1);
         * 
         * 
         */

        tf.put("pheser", 1);
        tf.put("ccpvdz", 2);
        tf.put("snaploop", 3);
        tf.put("preconnect", 4);
        tf.put("poseinvari", 5);
        tf.put("mulatta", 6);
        tf.put("twopap", 7);
        tf.put("flexibl", 8);
        tf.put("fossil", 9);
        tf.put("group", 10);

        double[] ground_truth = {0.0311542293581266, 0.0311542293581266, 0.0160398321740129, 0, 0.0213861537363948, 0.0151143971841137, 0.0311542293581266, 0.0471940615321395, 0.0471940615321395, 0.0471940615321395, 0.0311542293581266, 0.0462686265422403, 0.0311542293581266, 0.0151143971841137, 0.0311542293581266, 0.0160398321740129, 0, 0.0160398321740129, 0.0311542293581266, 0.0311542293581266, 0.0632338937061525, 0, 0.0151143971841137, 0, 0.0151143971841137, 0.0302287943682274, 0.0801982933831975, 0.0151143971841137, 0.0199408338669993, 0.0471940615321395, 0, 0.0311542293581266, 0.0444478583567327, 0, 0.0311542293581266, 0.0311542293581266, 0.0471940615321395, 0.0160398321740129, 0.0311542293581266, 0.0151143971841137, 0.0311542293581266, 0.0160398321740129, 0.0418468724828905, 0.0462686265422403, 0.0160398321740129, 0.0387110809554366, 0, 0.0481194965220388, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0453732933466319, 0.0160398321740129, 0.0801982933831975, 0.0151143971841137, 0.0590887812087438, 0.0471940615321395, 0, 0.0311542293581266, 0.0160398321740129, 0.0311542293581266, 0, 0.0311542293581266, 0.0302287943682274, 0.0160398321740129, 0.0311542293581266, 0.0387110809554366, 0.0160398321740129, 0.0641593286960517, 0.0160398321740129, 0.0320796643480259, 0.0160398321740129, 0.0311542293581266, 0.0160398321740129, 0, 0.0160398321740129, 0.0320796643480259, 0.0320796643480259, 0.0160398321740129, 0, 0.0412302628177045, 0.0160398321740129, 0.0695056502584336, 0.0160398321740129, 0.043986095097337, 0.0311542293581266, 0.0311542293581266, 0.0160398321740129, 0.0664697064693913, 0, 0.0311542293581266, 0.0481194965220388, 0.0311542293581266, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0284080261827198, 0.0311542293581266, 0.0160398321740129, 0.0151143971841137, 0, 0.0311542293581266, 0.0311542293581266, 0.0160398321740129, 0.0311542293581266, 0.0462686265422403, 0.0151143971841137, 0.0427723074727897, 0, 0, 0, 0.0801982933831975, 0.0151143971841137, 0.0623084587162532, 0, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0311542293581266, 0.0160398321740129, 0.0632338937061525, 0.0160398321740129, 0.0462686265422403, 0.0471940615321395, 0, 0, 0, 0.0160398321740129, 0, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0, 0.0160398321740129, 0.0151143971841137, 0.0311542293581266, 0.0375701622277318, 0.0311542293581266, 0.0882182094702039, 0.0311542293581266, 0.0311542293581266, 0.0311542293581266, 0, 0.0311542293581266, 0.0365005509205085, 0.0151143971841137, 0.0311542293581266, 0, 0, 0, 0.0311542293581266, 0, 0.0311542293581266, 0.0453732933466319, 0, 0.0311542293581266, 0.0365005509205085, 0.0311542293581266, 0.0311542293581266, 0.0311542293581266, 0.0311542293581266, 0.0311542293581266, 0.0462686265422403, 0.0676547802786351, 0.0444478583567327, 0.0783482908902662, 0.0302287943682274, 0.0320796643480259, 0.0151143971841137, 0.0462686265422403, 0.0151143971841137, 0.0387110809554366, 0.0311542293581266, 0.0302287943682274, 0.0792737258801654, 0.0311542293581266, 0.0151143971841137, 0.0311542293581266, 0.0302287943682274, 0.0151143971841137, 0.0151143971841137, 0.0151143971841137, 0.0471940615321395, 0.0151143971841137, 0.0151143971841137, 0.0151143971841137, 0.0160398321740129, 0.0311542293581266, 0.0311542293581266, 0.0471940615321395, 0.0151143971841137, 0, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0151143971841137, 0.0311542293581266, 0.0151143971841137, 0.0160398321740129, 0.0471940615321395, 0.0151143971841137, 0, 0.0311542293581266, 0.0311542293581266, 0, 0.0151143971841137, 0, 0.0160398321740129, 0.0151143971841137, 0.0311542293581266, 0.0151143971841137, 0.0311542293581266, 0.0302287943682274, 0.0151143971841137, 0.0462686265422403, 0.0311542293581266, 0.0320796643480259, 0.0311542293581266, 0.0561394126090452, 0.0160398321740129, 0.0311542293581266, 0.0311542293581266, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0151143971841137, 0.0311542293581266, 0.0311542293581266, 0, 0.0311542293581266, 0.0311542293581266, 0.0311542293581266, 0.0160398321740129, 0.0151143971841137, 0.0320796643480259, 0.0713310161243372, 0.0284080261827198, 0.0462686265422403, 0.0151143971841137, 0.0151143971841137, 0, 0.0471940615321395, 0.0471940615321395, 0.0311542293581266, 0.0151143971841137, 0.0764967269209739, 0.0160398321740129, 0.0160398321740129, 0.0953126905673112, 0.0151143971841137, 0.0160398321740129, 0.0160398321740129, 0, 0.0320796643480259, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0311542293581266, 0.0481194965220388, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0, 0.0311542293581266, 0.0160398321740129, 0, 0.0160398321740129, 0.0160398321740129, 0, 0.0160398321740129, 0, 0.0213861537363948, 0, 0.0320796643480259, 0.0160398321740129, 0.0160398321740129, 0, 0.0160398321740129, 0.0320796643480259, 0.0311542293581266, 0.0160398321740129, 0.0320796643480259, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0311542293581266, 0.0151143971841137, 0.0311542293581266, 0.0160398321740129, 0.0481194965220388, 0.0281084829674852, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0213861537363948, 0.0160398321740129, 0, 0.136954315657429, 0, 0.0311542293581266, 0.0160398321740129, 0, 0.0311542293581266, 0.0320796643480259, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0, 0.0160398321740129, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0311542293581266, 0, 0.0311542293581266, 0.0471940615321395, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0224557650436181, 0, 0.0320796643480259, 0.0160398321740129, 0, 0.0311542293581266, 0.0462686265422403, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0703283748032597, 0.0160398321740129, 0.0346797827350009, 0.0623084587162532, 0.0160398321740129, 0.0160398321740129, 0.0462686265422403, 0.0311542293581266, 0.0160398321740129, 0.0481194965220388, 0, 0, 0, 0.0449115300872362, 0.0320796643480259, 0, 0.0160398321740129, 0.0160398321740129, 0.0151143971841137, 0.0160398321740129, 0.0320796643480259, 0, 0, 0, 0.0311542293581266, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0641593286960517, 0.0311542293581266, 0.0160398321740129, 0.0462686265422403, 0.0311542293581266, 0.0160398321740129, 0.0481194965220388, 0.0934626880743798, 0.033828170877498, 0.0311542293581266, 0.0311542293581266, 0.0471940615321395, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.159472019263363, 0.0462686265422403, 0.0160398321740129, 0.0160398321740129, 0.0311542293581266, 0.0160398321740129, 0.0311542293581266, 0.0311542293581266, 0.0359815335278794, 0.0801982933831975, 0.0311542293581266, 0.0160398321740129, 0.0311542293581266, 0.0311542293581266, 0.0462686265422403, 0.0160398321740129, 0.0471940615321395, 0.0160398321740129, 0.0160398321740129, 0.0320796643480259, 0.0311542293581266, 0.0311542293581266, 0.0471940615321395, 0.0160398321740129, 0.0160398321740129, 0.0311542293581266, 0.0311542293581266, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0311542293581266, 0.0160398321740129, 0.0160398321740129, 0.0494564672817245, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0471940615321395, 0.0160398321740129, 0.0320796643480259, 0.0160398321740129, 0.0311542293581266, 0.0632338937061525, 0.0160398321740129, 0.0160398321740129, 0.0471940615321395, 0.0290382553917203, 0.0320796643480259, 0.027856391283885, 0.0308107045587264, 0, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0374259859104078, 0.0160398321740129, 0.0160398321740129, 0.027856391283885, 0.0641593286960517, 0.0160398321740129, 0.0160398321740129, 0.0320796643480259, 0.0160398321740129, 0.027856391283885, 0.027856391283885, 0.0160398321740129, 0.0426279576580922, 0.0320796643480259, 0.0320796643480259, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.027856391283885, 0.027856391283885, 0.0160398321740129, 0.0160398321740129, 0.027856391283885, 0.0498048500075808, 0.0160398321740129, 0.176437286427275, 0.0160398321740129, 0.0557134765572637, 0.027856391283885, 0.0320796643480259, 0.0160398321740129, 0.027856391283885, 0.0438962234578979, 0.0320796643480259, 0.027856391283885, 0.0160398321740129, 0.027856391283885, 0.0160398321740129, 0.0641593286960517, 0.0308107045587264, 0.0320796643480259, 0.027856391283885, 0.0552207440167099, 0.0160398321740129, 0.0160398321740129, 0, 0.0776643642441876, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.033108503772492, 0.0160398321740129, 0.0320796643480259, 0.0160398321740129, 0.0320796643480259, 0.027856391283885, 0.0801982933831975, 0.0325831537257326, 0.0160398321740129, 0, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0132936289986061, 0.0160398321740129, 0.0160398321740129, 0.0481194965220388, 0.0160398321740129, 0.027856391283885, 0.017725185659555, 0.017725185659555, 0.0557134765572637, 0.0160398321740129, 0, 0.027856391283885, 0.0438962234578979, 0.0160398321740129, 0, 0.0641593286960517, 0.0160398321740129, 0.0160398321740129, 0.0132936289986061, 0.0160398321740129, 0.029333461172619, 0.0284192167633063, 0.0377041022000232, 0, 0.0481194965220388, 0.0251101881084781, 0.029333461172619, 0.029333461172619, 0, 0.0160398321740129, 0.0251101881084781, 0.0160398321740129, 0.0320796643480259, 0.0160398321740129, 0.0160398321740129, 0.028644763348771, 0.0118165591098721, 0.027856391283885, 0.0160398321740129, 0, 0.111008997941924, 0.0160398321740129, 0.0468505367327394, 0.0160398321740129, 0.0160398321740129, 0.027856391283885, 0, 0.029333461172619, 0, 0.0333104547151697, 0.0160398321740129, 0.0160398321740129, 0.0458767827243386, 0, 0.027856391283885, 0.0160398321740129, 0.0308107045587264, 0.0438962234578979, 0.0320796643480259, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0396736443832508, 0.0160398321740129, 0.027856391283885, 0.027856391283885, 0, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0367193311084093, 0.017725185659555, 0, 0, 0.0438962234578979, 0.0320796643480259, 0.027856391283885, 0, 0.0504613640686567, 0.0481194965220388, 0, 0.0160398321740129, 0.0135050355481359, 0.0357345600167955, 0, 0.0160398321740129, 0, 0.0160398321740129, 0.0160398321740129, 0.0438962234578979, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.029333461172619, 0.0346099500421936, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0641593286960517, 0.0160398321740129, 0, 0.0160398321740129, 0.027856391283885, 0.0302201194995556, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0160398321740129, 0.0453732933466319, 0.029333461172619, 0.029333461172619, 0.0160398321740129, 0.0436127287497061, 0.0160398321740129, 0.0173211102768262, 0.0387607012042388, 0.0134720710471834, 0.0134720710471834, 0.0134720710471834, 0.0118165591098721, 0.0657048432986056, 0, 0.0118165591098721, 0.0134720710471834, 0.0252886301570554, 0.0589688077750139, 0.0118165591098721, 0.079176914345789, 0, 0.0252886301570554, 0.0306774585759288, 0, 0.0252886301570554, 0.0134720710471834, 0.0134720710471834, 0, 0, 0.0196947278427826, 0.0118165591098721, 0.0608183765245116, 0, 0.0251101881084781, 0.0387607012042388, 0.0387607012042388, 0.0323791208145737, 0, 0.0252886301570554, 0, 0.0134720710471834, 0.0118165591098721, 0.0118165591098721, 0.0134720710471834, 0.0272135835153104, 0.0252886301570554, 0.0785353210588285, 0.0309373576413336, 0.0252886301570554, 0, 0.0118165591098721, 0.033166798889966, 0, 0.0446693277539217, 0.0179630505585336, 0.0252886301570554, 0, 0.0267859124897946, 0.0366334499085593, 0.0410449676228765, 0.0648774343246968, 0.0282823273356665, 0, 0.048143352410886, 0.048597048042417, 0.0118165591098721, 0.0230802688392953, 0.0453182946792532, 0.0352388780208934, 0.0132936289986061, 0.0118165591098721, 0.0522327722514222, 0.0924705433443951, 0.0252886301570554, 0.0646771316070668, 0.0252886301570554, 0.0246990860821253, 0.0252886301570554, 0.0196947278427826, 0.0134720710471834, 0, 0.0134720710471834, 0.0333718727853655, 0.0134720710471834, 0.0144426153541756, 0.0286562141754177, 0.0297796096684056, 0.0134720710471834, 0.0252886301570554, 0.0266358372617738, 0.0236338122092379, 0.0118165591098721, 0, 0.0269441420943668, 0.0134720710471834, 0.0269441420943668, 0.0118165591098721, 0.0251101881084781, 0, 0.0471522486651418, 0, 0, 0.0380906543480351, 0.0317573929768714, 0.0320246656806471, 0.0118165591098721, 0.0516982268438707, 0.0385822591556615, 0.0272581723402831, 0.0444908857053444, 0.0267657000457895, 0.0134720710471834, 0.0387607012042388, 0.0304879994441384, 0.0500064539555116, 0.0134720710471834, 0.034911661974596, 0.0317573929768714, 0.0252886301570554, 0, 0.0118165591098721, 0.0335017355693812, 0, 0.0696017678033163, 0.0415968627677714, 0.0132936289986061, 0.0402377710929728, 0.0252886301570554, 0.0540466005419924, 0.0134720710471834, 0.027914686401359, 0, 0.0134720710471834, 0, 0.0397454722958527, 0.0282429434318969, 0.0118165591098721, 0.033891670916175, 0.0118165591098721, 0.0118165591098721, 0.0252886301570554, 0.0211014445465889, 0.0252886301570554, 0.0236338122092379, 0.0118165591098721, 0, 0, 0.0252886301570554, 0.0371058832564213, 0.0522327722514222, 0.0134720710471834, 0.0134720710471834, 0.0252886301570554, 0.0252886301570554, 0.0252886301570554, 0.0118165591098721, 0.0252886301570554, 0.0252886301570554, 0.0489224423662933, 0.0252886301570554, 0.0252886301570554, 0.0118165591098721, 0.0118165591098721, 0.0472676244184757, 0.0269441420943668, 0.0716134698482885, 0.0179630505585336, 0.0202081065707751, 0.0252886301570554, 0.0269441420943668, 0.0385898062914059, 0.0252886301570554, 0.0252886301570554, 0.0314680861066695, 0.0134720710471834, 0, 0.044265252371193, 0.0252886301570554, 0.0202081065707751, 0.0134720710471834, 0.0134720710471834, 0, 0.0269441420943668, 0.0269771065953193, 0.0202081065707751, 0.0134720710471834, 0.0276523583727261, 0.0118165591098721, 0.0538882841887335, 0.0161664852566201, 0.0134720710471834, 0, 0.0134720710471834, 0.0179630505585336, 0.0134720710471834, 0.023095102864724, 0.0252886301570554, 0, 0.0134720710471834, 0.0134720710471834, 0.0252886301570554, 0.0387607012042388, 0.0134720710471834, 0.0175136923613384, 0.0168396550655456, 0.0134720710471834, 0.0252886301570554, 0.0134720710471834, 0.0252886301570554, 0.0252886301570554, 0.0134720710471834, 0.0371058832564213, 0.0639081044993186, 0.0134720710471834, 0.0252886301570554, 0.0141802873255427, 0.0311972567067384, 0.0202081065707751, 0.0134720710471834, 0.0356882362180885, 0, 0.0118165591098721, 0.0415968627677714, 0.0803575639720103, 0.0134720710471834, 0.0134720710471834, 0.03793329223033, 0.0134720710471834, 0.0157171270594249, 0.0168396550655456, 0.0134720710471834, 0.027533686169297, 0.030311726112729, 0.0269441420943668, 0.0252886301570554, 0.0134720710471834, 0.0286562141754177, 0.0306774585759288, 0.033166798889966, 0.0202081065707751, 0.0421282852226011, 0.0252886301570554, 0.0286562141754177, 0.0202081065707751, 0.0269441420943668, 0.0252886301570554, 0.0320246656806471, 0.0224531625830166, 0.0371058832564213, 0.0269771065953193, 0.0252886301570554, 0.0134720710471834, 0, 0.0215553136754934, 0.0252886301570554, 0.0252886301570554, 0.0311972567067384, 0.0252886301570554, 0.0252886301570554, 0.0252886301570554, 0.0320246656806471, 0.0387607012042388, 0.0134720710471834, 0.0252886301570554, 0.0387607012042388, 0.0134720710471834, 0.0134720710471834, 0.0252886301570554, 0.0536504192897549, 0.0356882362180885, 0.0252886301570554, 0.0252886301570554, 0.0252886301570554, 0.0252886301570554, 0.0292277145235107, 0.0349789789554886, 0.0311972567067384, 0.0297796096684056, 0.0286562141754177, 0.0480357840393567, 0.0461793621436083, 0.026798664546742, 0.0252886301570554, 0.0602682163533511, 0.0497497645915154, 0.0385822591556615, 0.0522327722514222, 0.0267657000457895, 0.0387607012042388, 0.0252886301570554, 0, 0, 0.0118165591098721, 0.033166798889966, 0.0135050355481359, 0.0252886301570554, 0.0252886301570554, 0.0134720710471834, 0.0252886301570554, 0.0464604278885721, 0.0252886301570554, 0.079176914345789, 0.0134720710471834, 0.0387607012042388, 0.0426997855706941, 0.0297796096684056, 0.0252886301570554, 0.0385822591556615, 0.0251101881084781, 0.0320246656806471, 0.0323791208145737, 0.0387607012042388, 0.0236338122092379, 0.0252886301570554, 0.0356882362180885, 0.0333718727853655, 0.0252886301570554, 0, 0.0387607012042388, 0.0134720710471834, 0.0266358372617738, 0.0148192781519017, 0.0118165591098721, 0.0252886301570554, 0.0252886301570554, 0.0252886301570554, 0.0252886301570554, 0.0489224423662933, 0.026766133789223, 0.0342697216928886, 0.0269771065953193, 0.0320411479311234, 0.0341515699815798, 0.0292277145235107, 0.0252886301570554, 0.0297796096684056, 0.0758665844606601, 0.0252886301570554, 0.0320246656806471, 0.0252886301570554, 0.0387607012042388, 0.0341515699815798, 0.0360662869948022, 0.0387607012042388, 0.0371058832564213, 0.0252886301570554, 0.0252886301570554, 0.0252886301570554, 0.0292277145235107, 0.0522327722514222, 0.0387607012042388, 0.0252886301570554, 0.0342697216928886, 0.0252886301570554, 0.0252886301570554, 0.0320246656806471, 0.0134720710471834, 0.0539455383219669, 0.0314680861066695, 0.0306774585759288, 0.0173211102768262, 0.0252886301570554, 0.0292277145235107, 0.0574258088843748, 0.0252886301570554, 0.0387607012042388, 0.0387607012042388, 0.0349789789554886, 0.0436469944809594, 0.0404162131415502, 0.0252886301570554, 0.0134720710471834, 0.0252886301570554, 0.0252886301570554, 0.0325069883787949, 0.0252886301570554, 0.0385822591556615, 0.0252886301570554, 0.0252886301570554, 0.0282429434318969, 0.0252886301570554, 0.0282429434318969, 0.0410449676228765, 0.0252886301570554, 0.0306601088385854, 0.0286562141754177, 0.0252886301570554, 0.0157171270594249, 0.0252886301570554, 0.0252886301570554, 0.0252886301570554};

        DoubleFullVector vector = CKB.getFullCkbVector(tf).getUnitVector();
        LOGGER.info("Got vector of dimension: " + vector.getDimension());
        assertTrue("Vector has null dimention", vector.getDimension() > 0);
        assertTrue("Vector has different dim than CKB", vector.getDimension() == CKB.getDimentionality());


        float[] vectordata = vector.getData();
        double score = 0;
        double val = 0;
        for (int zz = 0; zz < ground_truth.length; zz++) {
            val = ground_truth[zz] - vectordata[zz];
            score += val * val;
        }
        score = Math.sqrt(score);
        LOGGER.info("Score difference between solr + matlab " + score);

        assertTrue("CKB Vector does't match expected value by tolerance level", score < .00001);

        
    }
    
    
    @Test
    public void testCKBDistance() {
        double ground_truth= 1.36364445181519;
        
        Map<String, Integer> tf1 = new HashMap<String, Integer>();
        Map<String, Integer> tf2 = new HashMap<String, Integer>();


        tf1.put("pheser", 1);
        tf1.put("ccpvdz", 2);
        tf1.put("snaploop", 3);
        tf1.put("preconnect", 4);
        tf1.put("poseinvari", 5);
        tf1.put("mulatta", 6);
        tf1.put("twopap", 7);
        tf1.put("flexibl", 8);
        tf1.put("fossil", 9);
        tf1.put("group", 10);

        /*
         * "httplglepflchteammwadaextensionsadaextensionshtml"	1	17864
            "brainbodyrobot"	2	110004
            "highand"	3	108482
            "srecarg"	4	55012
            "nontrap"	5	90702
            "steamtocarbon"	6	16082
            "chote"	7	47802
            "multiphononassist"	8	103787
            "livetim"	9	89787
            "macroenviron"	10	108746

         */
        
        tf2.put("httplglepflchteammwadaextensionsadaextensionshtml", 1);
        tf2.put("brainbodyrobot", 2);
        tf2.put("highand", 3);
        tf2.put("srecarg", 4);
        tf2.put("nontrap", 5);
        tf2.put("steamtocarbon", 6);
        tf2.put("chote", 7);
        tf2.put("multiphononassist", 8);
        tf2.put("livetim", 9);
        tf2.put("macroenviron", 10);

        
        

        DoubleFullVector vector1 = CKB.getFullCkbVector(tf1).getUnitVector();
        DoubleFullVector vector2 = CKB.getFullCkbVector(tf2).getUnitVector();
        assertTrue("Distance to self is not zero!",vector1.getDistance(vector1)==0);
        double distval=Math.abs(vector1.getDistance(vector2)-ground_truth);
        LOGGER.info("Distance to other vector (ckb): "+ distval);
        assertTrue("Distance to other vector is not corrent!",distval<.00001);
   }
 
    @Test
    public void testtfidfDistance() {
        double ground_truth= 1.4142135623731;
        
        
        Map<String, Float> tf1_ground = new HashMap<String, Float>();
        tf1_ground.put("pheser", 9.8752f);
        tf1_ground.put("ccpvdz", 19.7504f);
        tf1_ground.put("snaploop", 29.6256f);
        tf1_ground.put("preconnect", 39.5008f);
        tf1_ground.put("poseinvari", 49.376f);
        tf1_ground.put("mulatta", 59.2512f);
        tf1_ground.put("twopap", 66.2879f);
        tf1_ground.put("flexibl", 28.5728f);
        tf1_ground.put("fossil", 55.026f);
        tf1_ground.put("group", 28.386f);
        
        
        Map<String, Float> tf1_ground_norm = new HashMap<String, Float>();
        tf1_ground_norm.put("pheser", 0.0737728082379262f);
        tf1_ground_norm.put("ccpvdz", 0.147545616475852f);
        tf1_ground_norm.put("snaploop", 0.221318424713779f);
        tf1_ground_norm.put("preconnect", 0.295091232951705f);
        tf1_ground_norm.put("poseinvari", 0.368864041189631f);
        tf1_ground_norm.put("mulatta", 0.442636849427557f);
        tf1_ground_norm.put("twopap",  0.49520460701503f);
        tf1_ground_norm.put("flexibl", 0.213453468812846f);
        tf1_ground_norm.put("fossil",  0.411072438644293f);
        tf1_ground_norm.put("group", 0.212057977017354f);
        
        Map<String, Integer> tf1 = new HashMap<String, Integer>();
        Map<String, Integer> tf2 = new HashMap<String, Integer>();
        
        

        tf1.put("pheser", 1);
        tf1.put("ccpvdz", 2);
        tf1.put("snaploop", 3);
        tf1.put("preconnect", 4);
        tf1.put("poseinvari", 5);
        tf1.put("mulatta", 6);
        tf1.put("twopap", 7);
        tf1.put("flexibl", 8);
        tf1.put("fossil", 9);
        tf1.put("group", 10);

        /*
         * "httplglepflchteammwadaextensionsadaextensionshtml"	1	17864
            "brainbodyrobot"	2	110004
            "highand"	3	108482
            "srecarg"	4	55012
            "nontrap"	5	90702
            "steamtocarbon"	6	16082
            "chote"	7	47802
            "multiphononassist"	8	103787
            "livetim"	9	89787
            "macroenviron"	10	108746

         */
        
        tf2.put("httplglepflchteammwadaextensionsadaextensionshtml", 1);
        tf2.put("brainbodyrobot", 2);
        tf2.put("highand", 3);
        tf2.put("srecarg", 4);
        tf2.put("nontrap", 5);
        tf2.put("steamtocarbon", 6);
        tf2.put("chote", 7);
        tf2.put("multiphononassist", 8);
        tf2.put("livetim", 9);
        tf2.put("macroenviron", 10);

        
        double distval;
        RealTermFreqVector vector1_ground = new RealTermFreqVector(tf1_ground) ;
        RealTermFreqVector vector1 = CKB.getTfIdfVector(tf1);
        distval=Math.abs(vector1.getDistance(vector1_ground));
        LOGGER.info("Distance to self (idf): "+ distval);
        assertTrue("Distance to self is not zero!",distval<0.0000001);
        
        
        RealTermFreqVector vector1_ground_norm = new RealTermFreqVector(tf1_ground_norm) ;
        distval=Math.abs(vector1.getUnitVector().getDistance(vector1_ground_norm));
        LOGGER.info("Distance to self (idf-norm): "+ distval);
        assertTrue("Distance to self is not zero!",distval<0.0000001);
        
        
        RealTermFreqVector vector2 = CKB.getTfIdfVector(tf2).getUnitVector();
        distval=Math.abs(vector1.getUnitVector().getDistance(vector2)-ground_truth);
        LOGGER.info("Distance to other vector (idf): "+ distval);
        assertTrue("Distance to other vector is not corrent!",distval<.00001);
   }
    
    
    
    
}
