package uk.ac.shef.dcs.sti.util;

import javafx.util.Pair;

import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/05/13
 * Time: 16:38
 */
public class CollectionUtils {

    public static double computeDice(Collection<String> c1, Collection<String> c2) {
        Set<String> intersection = new HashSet<>(c1);
        intersection.retainAll(c1);
        intersection.retainAll(c2);

        if (intersection.size() == 0)
            return 0.0;
        double score = 2 * (double) intersection.size() / (c1.size() + c2.size());
        return score;

    }

                                                  //entity         //context

    /**
     * how much of b does a cover
     * @param a
     * @param b
     * @return
     */
    public static double computeCoverage(Collection<String> a, Collection<String> b) {
        List<String> c = new ArrayList<>(b);
        c.retainAll(a);
        if(c.size()==0)
            return 0.0;
        double score = (double) c.size() / b.size();
        return score;
    }

    public static double computeFrequencyWeightedDice(Collection<String> c1, Collection<String> c2) {
        List<String> union = new ArrayList<>();
        union.addAll(c1);
        union.addAll(c2);

        List<String> intersection = new ArrayList<>(union);
        intersection.retainAll(c1);
        intersection.retainAll(c2);

        if (intersection.size() == 0)
            return 0.0;
        double score = 2 * (double) intersection.size() / (c1.size() + c2.size());
        return score;

    }

}
