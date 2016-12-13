package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.SemanticTableInterpreter;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**

 */
public class TMPInterpreter extends SemanticTableInterpreter {

    private SubjectColumnDetector subjectColumnDetector;
    private LEARNING learning;
    private LiteralColumnTagger literalColumnTagger;
    private TColumnColumnRelationEnumerator relationEnumerator;
    private UPDATE update;

    private static final Logger LOG = LoggerFactory.getLogger(TMPInterpreter.class.getName());

    public TMPInterpreter(SubjectColumnDetector subjectColumnDetector,
                          LEARNING learning,
                          UPDATE update,
                          TColumnColumnRelationEnumerator relationEnumerator,
                          LiteralColumnTagger literalColumnTagger,
                          int[] ignoreColumns,
                          int[] mustdoColumns
    ) {
        super(ignoreColumns,mustdoColumns);
        this.subjectColumnDetector = subjectColumnDetector;
        this.learning = learning;
        this.literalColumnTagger = literalColumnTagger;
        this.relationEnumerator = relationEnumerator;

        this.update = update;
    }

    public TAnnotation start(Table table, Constraints constraints) throws STIException {
      return start(table, true);
    }

    public TAnnotation start(Table table, boolean relationLearning) throws STIException {
        //1. find the main subject column of this table
        LOG.info(">\t PHASE: Detecting subject column...");
        int[] ignoreColumnsArray = new int[getIgnoreColumns().size()];

        int index = 0;
        for (Integer i : getIgnoreColumns()) {
            ignoreColumnsArray[index] = i;
            index++;
        }
        try {
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
                    if (getIgnoreColumns().contains(col)) continue;
                    if (!table.getColumnHeader(col).getFeature().getMostFrequentDataType().getType().equals(DataTypeClassifier.DataType.NAMED_ENTITY))
                        continue;
                /*if (table.getColumnHeader(col).getFeature().isAcronymColumn())
                    continue;*/
                    annotatedColumns.add(col);

                    //if (tab_annotations.getRelationAnnotationsBetween(main_subject_column, col) == null) {
                    LOG.info("\t>> Column=" + col);
                    learning.learn(table, tableAnnotations, col);
                }
            }

            if (update != null) {
                LOG.info(">\t PHASE: UPDATE phase ...");
                update.update(annotatedColumns, table, tableAnnotations);
            }
            if (relationLearning) {
                LOG.info("\t> PHASE: RELATION ENUMERATION ...");
                new RELATIONENUMERATION().enumerate(subjectColumnScores,
                        getIgnoreColumns(), relationEnumerator,
                        tableAnnotations, table,
                        annotatedColumns, update);

                //4. consolidation-for columns that have relation with main subject column, if the column is
                // entity column, do column typing and disambiguation; otherwise, simply create header annotation
                LOG.info("\t\t>> Annotate literal-columns in relation with main column");
                literalColumnTagger.annotate(table, tableAnnotations, annotatedColumns.toArray(new Integer[0]));
            }
            return tableAnnotations;
        }catch (Exception e){
            throw new STIException(e);
        }
    }

}
