package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 *
 */
public class TColumnClassifierSimilarity extends TColumnClassifier {

    private BaselineSimilarityClazzScorer clazzScorer;

    public TColumnClassifierSimilarity(BaselineSimilarityClazzScorer clazzScorer){
        this.clazzScorer=clazzScorer;
    }

    @Override
    protected void classify(Map<Integer, List<Pair<Entity, Map<String, Double>>>> rowIndex_and_entities,
                            Table table,
                            TAnnotation tableAnnotation,
                            int column) throws STIException {
        Map<Clazz, Double> state = new HashMap<>();
        List<TColumnHeaderAnnotation> columnHeaderAnnotations = new ArrayList<>();
        for (Map.Entry<Integer, List<Pair<Entity, Map<String, Double>>>> e : rowIndex_and_entities.entrySet()) {
            List<Pair<Entity, Map<String, Double>>> entities = e.getValue();
            List<Integer> blockOfRows = Collections.singletonList(e.getKey());

            columnHeaderAnnotations=
                    clazzScorer.computeElementScores(entities,columnHeaderAnnotations, table,
                    blockOfRows, column);
        }

        for(TColumnHeaderAnnotation columnHeaderAnnotation: columnHeaderAnnotations){
            clazzScorer.computeFinal(columnHeaderAnnotation,
                    table.getNumRows());
        }
        generateColumnClazzAnnotations(state, table, tableAnnotation, column); //supporting rows not added

    }
}
