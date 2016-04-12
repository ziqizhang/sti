package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 * Created by - on 12/04/2016.
 */
public class TColumnClassifierNameMatch {

    public void classify(Map<Integer, List<Pair<Entity, Map<String, Double>>>> rowIndex_and_entities,
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


    //assigns highest scoring column_type_label to the column;
    //then disambiguate those rows that contributed to the prediction to column_type_scorings
    //WARNING: SUPPORTING ROWS NOT ADDED HERE
    private void generateColumnClazzAnnotations(final Map<Clazz, Double> state,
                                                Table table,
                                                TAnnotation tableAnnotation,
                                                int column) {
        List<Clazz> candidates = new ArrayList<>(state.keySet());
        Collections.sort(candidates, (o1, o2) -> state.get(o2).compareTo(state.get(o1)));
        //insert column type annotations
        TColumnHeaderAnnotation[] finalResult = new TColumnHeaderAnnotation[candidates.size()];
        for (int i = 0; i < candidates.size(); i++){
            Clazz c = candidates.get(i);
            TColumnHeaderAnnotation ha = new TColumnHeaderAnnotation(
                    table.getColumnHeader(column).getHeaderText(),c,state.get(c));
            finalResult[i] = ha;
        }
        tableAnnotation.setHeaderAnnotation(column, finalResult);
    }


}
