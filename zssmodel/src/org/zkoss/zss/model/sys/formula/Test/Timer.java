package org.zkoss.zss.model.sys.formula.Test;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Timer {

    private Timer(){}

    private static Map<String, Long> labledDuration = new TreeMap<>();

    public static void time(String label, Runnable method){
        long startTime = System.nanoTime();
        method.run();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        labledDuration.put(label, labledDuration.computeIfAbsent(label, s -> 0L) + duration);
    }

    public static void outputTime(Set<String> exclude){
        StringBuilder output = new StringBuilder();
        output.append("**************************************************").append("\n");
        Long totalDuration = 0L;
        for (Map.Entry<String,Long> entry:labledDuration.entrySet()){
            String label = entry.getKey();
            Long duration = entry.getValue();
            if (exclude.contains(label)){
                output.append(label).append(":\t").append((double) duration / 1000000).append("ms\n");
            }
            else {
                totalDuration += duration;
            }
        }

        output.append("**************************************************").append("\n");

        output.append("Total duration" + ":\t").append(totalDuration / (double) 1000000).append("ms\n");

        for (Map.Entry<String,Long> entry:labledDuration.entrySet()){
            String label = entry.getKey();
            Long duration = entry.getValue();
            if (!exclude.contains(label)){
                output.append(label)
                        .append(":\t")
                        .append((double) duration / 1000000)
                        .append("ms\t")
                        .append(duration / (double) totalDuration * 100)
                        .append("%")
                        .append("\n");
            }
        }

        output.append("**************************************************").append("\n");
        System.out.println(output.toString());
        labledDuration.clear();
    }

    public static void clear(){
        labledDuration.clear();
    }

}
