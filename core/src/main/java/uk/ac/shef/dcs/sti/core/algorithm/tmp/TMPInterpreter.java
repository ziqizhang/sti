package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.rep.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.scorer.ClazzScorer;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.core.subjectcol.SubjectColumnDetector;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
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

    public TAnnotation start(Table table, boolean relationLearning) throws IOException, APIKeysDepletedException, KBSearchException, STIException {
        //1. find the main subject column of this table
        LOG.info(">\t Detecting subject column...");
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
        LOG.info(">\t LEARNING phrase ...");
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
            LOG.info(">\t UPDATE phase ...");
            update.update(annotatedColumns, table, tableAnnotations);
        }

        if (relationLearning) {
            new RELATIONENUMERATION().enumerate(subjectColumnScores,
                    ignoreCols, relationEnumerator,
                    tableAnnotations, table,
                    annotatedColumns, update, relationScorer);

            //4. consolidation-for columns that have relation with main subject column, if the column is
            // entity column, do column typing and disambiguation; otherwise, simply create header annotation
            LOG.info(">\t Annotate literal-columns in relation with main column");
            literalColumnTagger.annotate(table, tableAnnotations, annotatedColumns.toArray(new Integer[0]));
        }

        return tableAnnotations;
    }

    protected static void addCellAnnotation(
            Table table,
            TAnnotation tableAnnotation,
            List<Integer> rowBlock,
            int table_cell_col,
            List<Pair<Entity, Map<String, Double>>> entities_and_scoreMap) {

        Collections.sort(entities_and_scoreMap, (o1, o2) -> {
            Double o2_score = o2.getValue().get(TCellAnnotation.SCORE_FINAL);
            Double o1_score = o1.getValue().get(TCellAnnotation.SCORE_FINAL);
            return o2_score.compareTo(o1_score);
        });

        String cellText = table.getContentCell(rowBlock.get(0), table_cell_col).getText();
        for (int row : rowBlock) {
            TCellAnnotation[] annotationsForCell = new TCellAnnotation[entities_and_scoreMap.size()];
            for (int i = 0; i < entities_and_scoreMap.size(); i++) {
                Pair<Entity, Map<String, Double>> e = entities_and_scoreMap.get(i);
                double score = e.getValue().get(TCellAnnotation.SCORE_FINAL);
                annotationsForCell[i] = new TCellAnnotation(cellText,
                        e.getKey(), score, e.getValue());

            }
            tableAnnotation.setContentCellAnnotations(row, table_cell_col, annotationsForCell);
        }
    }

    /**
     * after disamb on the column, go thru the cells that have been newly disambiguated (i.e., in addition to cold start
     * disamb) update class annotation for the column due to these new cells
     *
     * @param rowsUpdated
     * @param column
     * @param tableAnnotations
     * @param table
     */
    public static void updateColumnClazz(List<Integer> rowsUpdated,
                                         int column,
                                         TAnnotation tableAnnotations,
                                         Table table,
                                         ClazzScorer clazzScorer) {
        List<TColumnHeaderAnnotation> existingColumnClazzAnnotations;
        existingColumnClazzAnnotations = tableAnnotations.getHeaderAnnotation(column) == null
                ? new ArrayList<>() : new ArrayList<>(Arrays.asList(tableAnnotations.getHeaderAnnotation(column)));

        //supporting rows are added if a header for the type of the cell annotation exists
        List<TColumnHeaderAnnotation> toAdd = new ArrayList<>();
        //deal with newly disambiguated cells (that is, in addition to cold start disamb)
        for (int row : rowsUpdated) {
            List<TCellAnnotation> winningEntities =
                    tableAnnotations.getWinningContentCellAnnotation(row, column);
            for (TCellAnnotation ca : winningEntities) {
                for (TColumnHeaderAnnotation ha : TColumnHeaderAnnotationUpdater.selectNew(ca, column, table, existingColumnClazzAnnotations)) {
                    if (!toAdd.contains(ha))
                        toAdd.add(ha);
                }
            }
        }

        toAdd.addAll(existingColumnClazzAnnotations);
        TColumnHeaderAnnotation[] result = TColumnHeaderAnnotationUpdater.updateColumnClazzAnnotationScores(
                rowsUpdated,
                column,
                table.getNumRows(),
                existingColumnClazzAnnotations,
                table,
                tableAnnotations,
                clazzScorer
        );
        tableAnnotations.setHeaderAnnotation(column, result);
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
