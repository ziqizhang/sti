package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import uk.ac.shef.dcs.oak.lodie.table.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableAnnotation;

import java.util.*;

/**
 * SMP uses granularity of a concept. In freebase, not every concept has instances.
 * some concepts are classified as "topic" and not properly as a concept, and therefore, they have no instances
 * Currently #KBSearcher_Freebase simply returns "1" for such concepts, effectively they have the maximum granularity.
 * This put other concepts that do have instances (e.g., /location/location) at disadvantage.
 *
 * This class extends LTableAnnotation and overwrites method #getBestHeaderAnnotations to cope with such problems.
 * Effectively, the "real" freebase concept that has the smallest number of instances (highest granularity) and any
 * "topic-based" concept (/m/*) are both considered the best header annotations
 */
public class LTableAnnotation_SMP_Freebase extends LTableAnnotation {
    public LTableAnnotation_SMP_Freebase(int rows, int cols) {
        super(rows, cols);
    }

    public List<HeaderAnnotation> getBestHeaderAnnotations(int headerCol){
        HeaderAnnotation[] annotations =getHeaderAnnotation(headerCol);
        List<HeaderAnnotation> result = new ArrayList<HeaderAnnotation>();
        if(annotations==null||annotations.length==0)
            return result;

        List<HeaderAnnotation> sorted = Arrays.asList(annotations);
        Collections.sort(sorted);

        //container to keep temporarily any concepts that have real instances and score the same "entity score)
        List<HeaderAnnotation> tmp = new ArrayList<HeaderAnnotation>();
        double maxScore = sorted.get(0).getFinalScore();
        for(HeaderAnnotation h: sorted){
            if(h.getFinalScore()==maxScore){
                if(h.getAnnotation_url().startsWith("/m/"))
                    result.add(h);
                else{
                    result.add(h);
                }
            }
        }

        if(tmp.size()>1){
            Collections.sort(tmp, new Comparator<HeaderAnnotation>() {
                @Override
                public int compare(HeaderAnnotation o1, HeaderAnnotation o2) {
                    return o1.getScoreElements().get(ColumnClassifier.SMP_SCORE_GRANULARITY).compareTo(
                            o2.getScoreElements().get(ColumnClassifier.SMP_SCORE_GRANULARITY)
                    );
                }
            });
            Double highest_granularity_score = tmp.get(0).getScoreElements().get(ColumnClassifier.SMP_SCORE_GRANULARITY);
            for(HeaderAnnotation ha: tmp){
                if(ha.getScoreElements().get(ColumnClassifier.SMP_SCORE_GRANULARITY)==highest_granularity_score)
                    result.add(ha);
            }
        }else{
            result.addAll(tmp);
        }

        return result;
    }
}
