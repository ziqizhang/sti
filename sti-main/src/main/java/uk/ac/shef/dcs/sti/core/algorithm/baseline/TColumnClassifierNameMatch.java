package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 * The default column classifier adopts the simple voting strategy (B_nm)
 */
public class TColumnClassifierNameMatch extends TColumnClassifier{

    protected void classify(Map<Integer, List<Pair<Entity, Map<String, Double>>>> rowIndex_and_entities,
                         Table table, TAnnotation tableAnnotation,
                         int column){
        Map<String, Double> state = new HashMap<>();
        Map<String, Clazz> uri_and_clazz = new HashMap<>();
        for (Map.Entry<Integer, List<Pair<Entity, Map<String, Double>>>> e : rowIndex_and_entities.entrySet()) {
            List<Pair<Entity, Map<String, Double>>> entities = e.getValue();
            if (entities.size() > 0) {
                Collections.sort(entities, (o1, o2) -> {
                    Double score1 = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
                    Double score2=o2.getValue().get(TCellAnnotation.SCORE_FINAL);
                    return score2.compareTo(score1);
                });

                double maxScore = entities.get(0).getValue().get(TCellAnnotation.SCORE_FINAL);
                for(Pair<Entity, Map<String, Double>> p:entities){
                    Entity ec=p.getKey();
                    Double score = p.getValue().get(TCellAnnotation.SCORE_FINAL);
                    if(score!=maxScore){
                        break;
                    }
                    List<Clazz> types = ec.getTypes();
                    for (Clazz t : types) {
                        uri_and_clazz.put(t.getId(), t);
                        Double prevScore = state.get(t.getId());
                        if (prevScore == null)
                            prevScore = 0.0;
                        prevScore += 1.0;
                        state.put(t.getId(), prevScore);
                    }
                }
            }
        }

        Map<Clazz, Double> clazzScores = new HashMap<>();
        for(Map.Entry<String, Double> e: state.entrySet()){
            Clazz c = uri_and_clazz.get(e.getKey());
            Double score = e.getValue();
            clazzScores.put(c, score);
        }
        generateColumnClazzAnnotations(clazzScores, table, tableAnnotation, column); //supporting rows not added
    }

}
