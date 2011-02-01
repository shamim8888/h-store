package edu.brown.markov.features;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.voltdb.catalog.ProcParameter;
import org.voltdb.catalog.Procedure;

import weka.core.Instance;

import edu.brown.markov.FeatureExtractor;
import edu.brown.utils.ClassUtil;
import edu.brown.utils.PartitionEstimator;

public abstract class FeatureUtil {
    private static final Logger LOG = Logger.getLogger(FeatureUtil.class);
    
    protected final static String DELIMITER = "-";
    protected final static String SUFFIX = "Feature";
    protected final static int SUFFIX_LENGTH = SUFFIX.length();
    protected final static Pattern PREFIX_SPLITTER = Pattern.compile(DELIMITER);
    protected final static String PREFIX_FORMAT = "%s" + DELIMITER + "%02d";

    /**
     * For the given feature key, return a new instance of that AbstractFeature
     * @param key
     * @param catalog_proc
     * @param p_estimator
     * @return
     */
    public static AbstractFeature getFeatureForKey(String key, Procedure catalog_proc, PartitionEstimator p_estimator) {
        assert(key != null);
        assert(catalog_proc != null);
        assert(p_estimator != null);
        
        String featureName = FeatureUtil.getFeatureKeyPrefix(key) + "Feature";
        Class<?> featureClass = ClassUtil.getClass(featureName); 
        AbstractFeature f = (AbstractFeature)ClassUtil.newInstance(
                                featureClass,
                                new Object[]{ p_estimator, catalog_proc },
                                new Class[] { PartitionEstimator.class, Procedure.class });
        return (f);
    }

    /**
     * Return the prefix that will be used for all attributes generated by the given Feature class
     * @param feature_class
     * @return
     */
    public static final String getFeatureKeyPrefix(Class<? extends AbstractFeature> feature_class) {
        String prefix = feature_class.getSimpleName(); 
        return (prefix.substring(0, prefix.length() - SUFFIX_LENGTH));
    }

    /**
     * Return the feature key that includes the given ProcParameter index number
     * @param catalog_param
     * @return
     */
    public static String getFeatureKeyPrefix(Class<? extends AbstractFeature> feature_class, ProcParameter catalog_param) {
        return (String.format(FeatureUtil.PREFIX_FORMAT, FeatureUtil.getFeatureKeyPrefix(feature_class), catalog_param.getIndex()));
    }

    /**
     * Return the feature key that includes the given ProcParameter index number and the array offset
     * @param catalog_param
     * @param idx
     * @return
     */
    public static String getFeatureKey(Class<? extends AbstractFeature> feature_class, ProcParameter catalog_param, int idx) {
        return (String.format(FeatureUtil.PREFIX_FORMAT, FeatureUtil.getFeatureKeyPrefix(feature_class, catalog_param), idx));
    }
    
    /**
     * 
     * @param feature_key
     * @return
     */
    public static final String getFeatureKeyPrefix(String feature_key) {
        return (PREFIX_SPLITTER.split(feature_key, 1)[0]);
    }
    
    /**
     * Return the TransactionId for a given Instance
     * @param inst
     * @return
     */
    public static long getTransactionId(Instance inst) {
        String val = inst.stringValue(FeatureExtractor.TXNID_ATTRIBUTE_IDX);
        assert(val.isEmpty() == false) : "Missing TransactionIdFeature for Instance\n" + inst;
        long txn_id = -1;
        try {
            txn_id = Long.valueOf(val);
        } catch (NumberFormatException ex) {
            LOG.fatal("Invalid Instance: " + inst);
            throw new RuntimeException("Invalid TransactionIdFeature for Instance", ex);
        }
        assert(txn_id != -1);
        return (txn_id);
    }
    
}
