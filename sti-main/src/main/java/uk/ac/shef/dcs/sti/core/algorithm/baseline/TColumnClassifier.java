package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by - on 12/04/2016.
 */
public abstract class TColumnClassifier {

    protected abstract void classify(Map<Integer, List<Pair<Entity, Map<String, Double>>>> rowIndex_and_entities,
                         Table table, TAnnotation tableAnnotation,
                         int column) throws STIException;

    //assigns highest scoring column_type_label to the column;
    //then disambiguate those rows that contributed to the prediction to column_type_scorings
    //WARNING: SUPPORTING ROWS NOT ADDED HERE
    protected void generateColumnClazzAnnotations(final Map<Clazz, Double> state,
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
