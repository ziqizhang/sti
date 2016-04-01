package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.rep.TCellAnnotation;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.rep.TAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

import java.util.*;

/**
 * Represents the LEARNING phase, creates preliminary column classification and cell disambiguation
 */
public class LEARNING {

    private LEARNINGPreliminaryColumnClassifier columnTagger;
    private LEARNINGPreliminaryDisamb cellTagger;
    private int max_reference_entities;


    public LEARNING(LEARNINGPreliminaryColumnClassifier columnTagger, LEARNINGPreliminaryDisamb cellTagger,
                    int max_reference_entities) {
        this.columnTagger = columnTagger;
        this.cellTagger = cellTagger;
        this.max_reference_entities = max_reference_entities;
    }

    public void process(Table table, TAnnotation tableAnnotation, int column) throws KBSearchException {
        Pair<Integer, List<List<Integer>>> stopPosition =
                columnTagger.runPreliminaryColumnClassifier(table, tableAnnotation, column);

        cellTagger.runPreliminaryDisamb(
                stopPosition.getKey(),
                stopPosition.getValue(),
                table,
                tableAnnotation,
                column);
    }

    public static Set<Entity> selectReferenceEntities(Table table,
                                                      TAnnotation tableAnnotation, int column, int max){
        List<TCellAnnotation> winningCellAnnotations = new ArrayList<>();
        for(int i=0; i<table.getNumRows(); i++){
            List<TCellAnnotation> best = tableAnnotation.getWinningContentCellAnnotation(i, column);
            if(best!=null && best.size()>0)
                winningCellAnnotations.addAll(best);
        }
        Collections.sort(winningCellAnnotations);
        Set<Entity> result = new HashSet<>();
        for(int i=0; i<winningCellAnnotations.size() && i<max; i++)
            result.add(winningCellAnnotations.get(i).getAnnotation());
        return result;
    }
}
