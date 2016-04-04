package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.dcs.websearch.bing.v2.APIKeysDepletedException;

import java.io.IOException;
import java.util.*;

/**

 */
public class TMPInterpreter {

    private SubjectColumnDetector subjectColumnDetector;
    private LEARNING learning;
    private LiteralColumnTagger literalColumnTagger;
    private TColumnColumnRelationEnumerator relationEnumerator;
    private RelationScorer relationScorer;
    private Set<Integer> ignoreCols;
    private int[] mustdoColumns;
    private UPDATE update;

    private static final Logger LOG = Logger.getLogger(TMPInterpreter.class.getName());

    public TMPInterpreter(SubjectColumnDetector subjectColumnDetector,
                          LEARNING learning,
                          UPDATE update,
                          TColumnColumnRelationEnumerator relationEnumerator,
                          RelationScorer relationScorer,
                          LiteralColumnTagger literalColumnTagger,
                          int[] ignoreColumns,
                          int[] mustdoColumns
    ) {
        this.subjectColumnDetector = subjectColumnDetector;
        this.learning = learning;
        this.literalColumnTagger = literalColumnTagger;
        this.relationEnumerator = relationEnumerator;
        this.ignoreCols = new HashSet<>();
        for (int i : ignoreColumns)
            ignoreCols.add(i);
        this.mustdoColumns = mustdoColumns;
        this.update = update;
        this.relationScorer = relationScorer;
    }

    public TAnnotation start(Table table, boolean relationLearning) throws IOException, APIKeysDepletedException, KBSearchException, STIException, ClassNotFoundException {
        //1. find the main subject column of this table
        LOG.info(">\t PHASE: Detecting subject column...");
        int[] ignoreColumnsArray = new int[ignoreCols.size()];

        int index = 0;
        for (Integer i : ignoreCols) {
            ignoreColumnsArray[index] = i;
            index++;
        }
        List<Pair<Integer, Pair<Double, Boolean>>> subjectColumnScores =
                subjectColumnDetector.compute(table, ignoreColumnsArray);

        TAnnotation tableAnnotations = new TAnnotation(table.getNumRows(), table.getNumCols());
        tableAnnotations.setSubjectColumn(subjectColumnScores.get(0).getKey());

        List<Integer> annotatedColumns = new ArrayList<>();
        LOG.info(">\t PHASE: LEARNING ...");
        for (int col = 0; col < table.getNumCols(); col++) {
            /*if(col!=1)
                continue;*/
            if (isCompulsoryColumn(col)) {
                LOG.info("\t>> Column=(compulsory)" + col);
                annotatedColumns.add(col);
                learning.learn(table, tableAnnotations, col);
            } else {
                if (ignoreCols.contains(col)) continue;
                if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                    continue;
                /*if (table.getColumnHeader(col).getFeature().isAcronymColumn())
                    continue;*/
                annotatedColumns.add(col);

                //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                LOG.info("\t>> Column=" + col);
                learning.learn(table, tableAnnotations, col);
                //}
            }
        }

        if (update != null) {
            LOG.info(">\t PHASE: UPDATE phase ...");
            update.update(annotatedColumns, table, tableAnnotations);
        }

        if (relationLearning) {
            LOG.info("\t> PHASE: RELATION ENUMERATION ...");
            new RELATIONENUMERATION().enumerate(subjectColumnScores,
                    ignoreCols, relationEnumerator,
                    tableAnnotations, table,
                    annotatedColumns, update, relationScorer);

            //4. consolidation-for columns that have relation with main subject column, if the column is
            // entity column, do column typing and disambiguation; otherwise, simply create header annotation
            LOG.info("\t\t>> Annotate literal-columns in relation with main column");
            literalColumnTagger.annotate(table, tableAnnotations, annotatedColumns.toArray(new Integer[0]));
        }

        return tableAnnotations;
    }


    private boolean isCompulsoryColumn(Integer i) {
        if (i != null) {
            for (int a : mustdoColumns) {
                if (a == i)
                    return true;
            }
        }
        return false;
    }

}
