package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 * Represents the LEARNING phase, creates preliminary column classification and cell disambiguation
 */
public class LEARNING {

    private LEARNINGPreliminaryColumnClassifier columnTagger;
    private LEARNINGPreliminaryDisamb cellTagger;


    public LEARNING(LEARNINGPreliminaryColumnClassifier columnTagger, LEARNINGPreliminaryDisamb cellTagger) {
        this.columnTagger = columnTagger;
        this.cellTagger = cellTagger;
    }

    public void learn(Table table, TAnnotation tableAnnotation, int column) throws KBSearchException, ClassNotFoundException, STIException {
      learn(table, tableAnnotation, column, new Constraints());
    }

    public void learn(Table table, TAnnotation tableAnnotation, int column, Constraints constraints) throws KBSearchException, ClassNotFoundException, STIException {
        Pair<Integer, List<List<Integer>>> stopPosition =
                columnTagger.runPreliminaryColumnClassifier(table, tableAnnotation, column, constraints);

        cellTagger.runPreliminaryDisamb(
                stopPosition.getKey(),
                stopPosition.getValue(),
                table,
                tableAnnotation,
                column,
                constraints);
    }

}
