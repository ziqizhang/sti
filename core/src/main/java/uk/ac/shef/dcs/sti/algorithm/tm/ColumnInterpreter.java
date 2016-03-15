package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.LTable;
import uk.ac.shef.dcs.sti.rep.LTableAnnotation;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 18/02/14
 * Time: 21:11
 * To change this template use File | Settings | File Templates.
 */
public class ColumnInterpreter {

    private ColumnLearner_LEARN_Seeding learnerSeeding;
    private ColumnLearner_LEARN_Update updater;
    private int max_reference_entities;


    public ColumnInterpreter(ColumnLearner_LEARN_Seeding learnerSeeding, ColumnLearner_LEARN_Update updater,
                             int max_reference_entities) {
        this.learnerSeeding = learnerSeeding;
        this.updater = updater;
        this.max_reference_entities = max_reference_entities;
    }

    public void interpret(LTable table, LTableAnnotation table_annotation, int column) throws IOException {
        Pair<Integer, List<List<Integer>>> converge_position =
                learnerSeeding.learn_seeding(table, table_annotation, column);
        Set<Entity> reference_entities = new HashSet<>();
        if (max_reference_entities>0) {
            reference_entities = selectReferenceEntities(table, table_annotation, column,max_reference_entities);
        }
        updater.learn_consolidate(converge_position.getKey(),
                converge_position.getValue(),
                table,
                table_annotation,
                column,
                reference_entities);
    }

    public static Set<Entity> selectReferenceEntities(LTable table, LTableAnnotation table_annotation, int column, int max){
        List<CellAnnotation> selected_best_from_each_row = new ArrayList<CellAnnotation>();
        for(int i=0; i<table.getNumRows(); i++){
            List<CellAnnotation> best = table_annotation.getBestContentCellAnnotations(i, column);
            if(best!=null && best.size()>0)
                selected_best_from_each_row.addAll(best);
        }
        Collections.sort(selected_best_from_each_row);
        Set<Entity> result = new HashSet<>();
        for(int i=0; i<selected_best_from_each_row.size() && i<max; i++)
            result.add(selected_best_from_each_row.get(i).getAnnotation());
        return result;
    }

    public static Set<Entity> selectReferenceEntities(Pair<Integer, int[]> converge_position, LTableAnnotation table_annotation, int column, int max){
        Set<Entity> reference_entities = new HashSet<>();
        for (int i = 0; i < converge_position.getKey()&&i<max; i++) {
            int row = converge_position.getValue()[i];
            CellAnnotation[] annotations = table_annotation.getContentCellAnnotations(row, column);
            if (annotations != null && annotations.length > 0)
                reference_entities.add(annotations[0].getAnnotation());
        }
        return reference_entities;
    }
}
