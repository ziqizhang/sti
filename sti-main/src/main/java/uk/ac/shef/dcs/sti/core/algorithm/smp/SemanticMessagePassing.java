package uk.ac.shef.dcs.sti.core.algorithm.smp;

import cern.colt.matrix.ObjectMatrix2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**
 * Created by - on 11/04/2016.
 */
public class SemanticMessagePassing {

    private int haltingMaxIterations=10;
    private CellAnnotationUpdater cellAnnotationUpdater;
    private ChangeMessageComputer messageComputer;

    private static final Logger LOG = LoggerFactory.getLogger(SemanticMessagePassing.class.getName());

    public SemanticMessagePassing(int haltingMaxIterations, double changeMessageScoreThreshold){
        this.haltingMaxIterations=haltingMaxIterations;
        cellAnnotationUpdater = new CellAnnotationUpdater();
        messageComputer = new ChangeMessageComputer(changeMessageScoreThreshold);
    }

    protected void start(Table table, TAnnotation tableAnnotations,
                         TColumnClassifier columnClassifier,
                         TColumnColumnRelationEnumerator relationLearner,
                         Collection<Integer> mustDoColumns,
                         Collection<Integer> ignoreColumns) throws STIException, KBProxyException {
        TAnnotation copy;
        for (int i = 1; i <= haltingMaxIterations; i++) {
            LOG.info("\t\t>> [ITERATION] " + i);
            copy = new TAnnotation(table.getNumRows(), table.getNumCols());
            TAnnotation.copy(tableAnnotations, copy);
            //column concept and relation factors send message to entity factors
            LOG.info("\t\t>> computing messages");
            ObjectMatrix2D messages = messageComputer.
                    computeChangeMessages(tableAnnotations, table);

            //re-compute cell annotations based on messages
            LOG.info("\t\t>> cell annotation update");
            int[] updateResult = cellAnnotationUpdater.update(messages, tableAnnotations);
            LOG.info("\t\t   (requiredForUpdate=" + updateResult[1] + ", updated=" + updateResult[0] + ")");

            //check stopping condition
            boolean stop = haltingConditionReached(i, haltingMaxIterations, copy, tableAnnotations);
            if (stop) {
                LOG.info("\t> Halting condition reached, iteration=" + i);
                break;
            } else {
                //re-compute header and relation annotations
                LOG.info(">\t Halting condition NOT reached, re-compute column and relation annotations based on updated cell annotations: iter=" + i);
                resetClassesAndRelations(tableAnnotations);
                SMPInterpreter.columnClassification(columnClassifier, tableAnnotations, table,mustDoColumns,ignoreColumns);
                if (relationLearner!=null)
                    SMPInterpreter.relationEnumeration(relationLearner,
                            tableAnnotations, table, tableAnnotations.getSubjectColumn());
            }
        }
    }

    private boolean haltingConditionReached(int currentIteration,
                                            int maxIterations,
                                            TAnnotation previous,
                                            TAnnotation current) {
        if (currentIteration == maxIterations)
            return true;

        //check header annotations
        int header_converged_count = 0;
        boolean header_converged = false;
        for (int c = 0; c < previous.getCols(); c++) {
            List<TColumnHeaderAnnotation> header_annotations_prev_iteration = previous.getWinningHeaderAnnotations(c);
            List<TColumnHeaderAnnotation> header_annotations_current_iteration = current.getWinningHeaderAnnotations(c);
            if (header_annotations_prev_iteration == null && header_annotations_current_iteration == null) {
                header_converged_count++;
                continue;
            }
            if (header_annotations_current_iteration.size() == header_annotations_prev_iteration.size()) {
                header_annotations_current_iteration.retainAll(header_annotations_prev_iteration);
                if (header_annotations_current_iteration.size() == header_annotations_prev_iteration.size())
                    header_converged_count++;
                else
                    return false;
            } else
                return false;
        }
        if (header_converged_count == previous.getCols()) {
            header_converged = true;
        }

        //check cell annotations
        for (int c = 0; c < previous.getCols(); c++) {
            for (int row = 0; row < previous.getRows(); row++) {
                List<TCellAnnotation> cell_prev_annotations = previous.getWinningContentCellAnnotation(row, c);
                List<TCellAnnotation> cell_current_annotations = current.getWinningContentCellAnnotation(row, c);
                if (cell_current_annotations.size() == cell_prev_annotations.size()) {
                    cell_current_annotations.retainAll(cell_prev_annotations);
                    if (cell_current_annotations.size() != cell_prev_annotations.size())
                        return false;
                }
            }
        }

        //check relation annotations
        int relation_converged_count = 0;
        boolean relation_converged = false;
        Map<RelationColumns, List<TColumnColumnRelationAnnotation>> prev_relations = previous.getColumncolumnRelations();
        Map<RelationColumns, List<TColumnColumnRelationAnnotation>> current_relation = current.getColumncolumnRelations();
        Set<RelationColumns> tmp_keys = new HashSet<>(prev_relations.keySet());
        tmp_keys.retainAll(current_relation.keySet());
        if (tmp_keys.size() != prev_relations.keySet().size() || tmp_keys.size() != current_relation.keySet().size())
            return false;
        for (RelationColumns subobj : tmp_keys) {
            List<TColumnColumnRelationAnnotation> prev_candidates = previous.getWinningRelationAnnotationsBetween(subobj);
            List<TColumnColumnRelationAnnotation> current_candidates = current.getWinningRelationAnnotationsBetween(subobj);
            List<TColumnColumnRelationAnnotation> tmp = new ArrayList<>(prev_candidates);
            tmp.retainAll(current_candidates);
            if (tmp.size() == prev_candidates.size() && tmp.size() == current_candidates.size()) {
                relation_converged_count++;
            }
        }
        if (relation_converged_count == tmp_keys.size())
            relation_converged = true;

        return header_converged && relation_converged;
    }

    private void resetClassesAndRelations(TAnnotation tab_annotations) {
        tab_annotations.resetHeaderAnnotations();
        tab_annotations.resetRelationAnnotations();
    }

}
