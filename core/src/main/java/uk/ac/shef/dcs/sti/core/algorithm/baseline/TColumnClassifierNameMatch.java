package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 * The default column classifier adopts the simple voting strategy (B_nm)
 */
public class TColumnClassifierNameMatch extends TColumnClassifier{

    protected void classify(Map<Integer, List<Pair<Entity, Map<String, Double>>>> rowIndex_and_entities,
                         Table table, TAnnotation tableAnnotation,
                         int column){
        Map<Clazz, Double> state = new HashMap<>();
        for (Map.Entry<Integer, List<Pair<Entity, Map<String, Double>>>> e : rowIndex_and_entities.entrySet()) {
            List<Pair<Entity, Map<String, Double>>> container = e.getValue();
            if (container.size() > 0) {
                Entity ec = container.get(0).getKey();
                List<Clazz> types = ec.getTypes();
                for (Clazz t : types) {
                    Double score = state.get(t);
                    if (score == null)
                        score = 0.0;
                    score += 1.0;
                    state.put(t, score);
                }
            }
        }
        generateColumnClazzAnnotations(state, table, tableAnnotation, column); //supporting rows not added
    }

}
