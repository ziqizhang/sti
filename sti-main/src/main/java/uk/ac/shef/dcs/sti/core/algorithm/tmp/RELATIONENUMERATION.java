package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.core.subjectcol.TColumnFeature;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by - on 02/04/2016.
 */
public class RELATIONENUMERATION {
    private static final Logger LOG = LoggerFactory.getLogger(RELATIONENUMERATION.class.getName());

    public void enumerate(List<Pair<Integer, Pair<Double, Boolean>>> subjectColCandidadteScores,
        Set<Integer> ignoreCols,
        TColumnColumnRelationEnumerator relationEnumerator,
        TAnnotation tableAnnotations,
        Table table,
        List<Integer> annotatedColumns,
        UPDATE update
        ) throws STIException {
      enumerate(subjectColCandidadteScores, ignoreCols, relationEnumerator,
          tableAnnotations, table, annotatedColumns, update, new Constraints());
    }

    public void enumerate(List<Pair<Integer, Pair<Double, Boolean>>> subjectColCandidadteScores,
                          Set<Integer> ignoreCols,
                          TColumnColumnRelationEnumerator relationEnumerator,
                          TAnnotation tableAnnotations,
                          Table table,
                          List<Integer> annotatedColumns,
                          UPDATE update,
                          Constraints constraints
                          ) throws STIException {
        double winningSolutionScore = 0;
        int subjectCol;
        TAnnotation winningSolution = null;
        for (Pair<Integer, Pair<Double, Boolean>> mainCol : subjectColCandidadteScores) {
            //tab_annotations = new TAnnotation(table.getNumRows(), table.getNumCols());
            subjectCol = mainCol.getKey();
            if (ignoreCols.contains(subjectCol)) continue;

            LOG.info(">>\t\t Let subject column=" + subjectCol);
            int relatedColumns =
                    relationEnumerator.runRelationEnumeration(tableAnnotations, table, subjectCol, constraints);

            boolean interpretable = false;
            if (relatedColumns > 0)
                interpretable = true;

            if (interpretable) {
                tableAnnotations.setSubjectColumn(subjectCol);
                break;
            } else {
                //the current subject column could be wrong, try differently
                double overallScore = scoreSolution(tableAnnotations, table, subjectCol);
                if (overallScore > winningSolutionScore) {
                    tableAnnotations.setSubjectColumn(subjectCol);
                    winningSolution = tableAnnotations;
                    winningSolutionScore = overallScore;
                }
                tableAnnotations.resetRelationAnnotations();
                LOG.warn(">>\t\t (this subject column does not form relation with other columns, try the next column)");
                continue;
            }
        }
        if (tableAnnotations == null && winningSolution != null)
            tableAnnotations = winningSolution;


        if (STIConstantProperty.REVISE_RELATION_ANNOTATION_BY_DC && update != null) {
            List<String> domain_rep = update.createDomainRep(table, tableAnnotations, annotatedColumns);
            reviseColumnColumnRelationAnnotations(tableAnnotations, domain_rep, relationEnumerator.getRelationScorer());
        }
    }

    private double scoreSolution(TAnnotation tableAnnotations, Table table, int subjectColumn) {
        double entityScores = 0.0;
        for (int col = 0; col < table.getNumCols(); col++) {
            for (int row = 0; row < table.getNumRows(); row++) {
                TCellAnnotation[] cAnns = tableAnnotations.getContentCellAnnotations(row, col);
                if (cAnns != null && cAnns.length > 0) {
                    entityScores += cAnns[0].getFinalScore();
                }
            }
        }

        double relationScores = 0.0;
        for (Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>> entry : tableAnnotations.getColumncolumnRelations().entrySet()) {
            RelationColumns key = entry.getKey();
            TColumnColumnRelationAnnotation rel = entry.getValue().get(0);
            relationScores += rel.getFinalScore();
        }
        TColumnFeature cf = table.getColumnHeader(subjectColumn).getFeature();
        //relationScores = relationScores * cf.getValueDiversity();

        double diversity = cf.getUniqueCellCount() + cf.getUniqueTokenCount();
        return (entityScores + relationScores) * diversity * ((table.getNumRows() - cf.getEmptyCellCount()) / (double) table.getNumRows());
    }

    private void reviseColumnColumnRelationAnnotations(TAnnotation annotation,
                                                       List<String> domain_representation,
                                                       RelationScorer relationScorer
    ) throws STIException {
        for (Map.Entry<RelationColumns, List<TColumnColumnRelationAnnotation>>
                entry : annotation.getColumncolumnRelations().entrySet()) {

            for (TColumnColumnRelationAnnotation relation : entry.getValue()) {
                double domain_consensus = relationScorer.scoreDC(relation, domain_representation);
                relation.setFinalScore(relation.getFinalScore() + domain_consensus);
            }
            Collections.sort(entry.getValue());
        }
    }

}
