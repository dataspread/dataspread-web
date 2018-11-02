package org.zkoss.zss.model.sys.formula.Test;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.zkoss.zss.model.impl.GraphCompressor;
import org.zkoss.zss.model.sys.formula.Primitives.DataOperator;
import org.zkoss.zss.model.sys.formula.Primitives.PhysicalOperator;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Logger;

public class Timer {

    private Timer(){}

    private static final Logger logger = Logger.getLogger(Timer.class.getName());

    private static Map<String, Long> labledDuration = new TreeMap<>();

    public static void time(String label, Runnable method){
        long startTime = System.nanoTime();
        method.run();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        labledDuration.put(label, labledDuration.computeIfAbsent(label, s -> 0L) + duration);
    }

    public static void outputTime(Set<String> exclude){
        logger.info("**************************************************");
        Long totalDuration = 0L;
        for (Map.Entry<String,Long> entry:labledDuration.entrySet()){
            String label = entry.getKey();
            Long duration = entry.getValue();
            if (exclude.contains(label)){
                logger.info(label + ":\t" + duration);
            }
            else {
                totalDuration += duration;
            }
        }

        logger.info("**************************************************");

        logger.info("Total duration" + ":\t" + totalDuration);

        for (Map.Entry<String,Long> entry:labledDuration.entrySet()){
            String label = entry.getKey();
            Long duration = entry.getValue();
            if (!exclude.contains(label)){
                logger.info(label + ":\t" + duration + "\t" + duration / (double) totalDuration + "%");
            }
        }

        logger.info("**************************************************");
        labledDuration.clear();
    }

    public static void clear(){
        labledDuration.clear();
    }

}
