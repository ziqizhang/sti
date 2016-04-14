package uk.ac.shef.dcs.sti.core.algorithm.baseline;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TCellCellRelationAnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;

import java.util.*;

/**
 * Created by - on 12/04/2016.
 */
public class BaselineRelationScorer implements RelationScorer {
    @Override
    public List<TColumnColumnRelationAnnotation> computeElementScores(
            List<TCellCellRelationAnotation> cellcellRelationsOnRow,
            Collection<TColumnColumnRelationAnnotation> output,
            int subjectCol, int objectCol, Table table) throws STIException {

        for (TCellCellRelationAnotation cellcellRelationAnnotation : cellcellRelationsOnRow) {
            TColumnColumnRelationAnnotation columncolumnRelationAnnotation = null;
            for (TColumnColumnRelationAnnotation key : output) {
                if (key.getRelationURI().equals(cellcellRelationAnnotation.getRelationURI())) {
                    columncolumnRelationAnnotation = key;
                    break;
                }
            }

            if (columncolumnRelationAnnotation == null) {
                columncolumnRelationAnnotation = new TColumnColumnRelationAnnotation(
                        new RelationColumns(subjectCol, objectCol),
                        cellcellRelationAnnotation.getRelationURI(),
                        cellcellRelationAnnotation.getRelationLabel(), 0.0);
                output.add(columncolumnRelationAnnotation);
            }

            Map<String, Double> scoreElements = columncolumnRelationAnnotation.getScoreElements();
            if (scoreElements == null || scoreElements.size() == 0) {
                scoreElements = new HashMap<>();
                scoreElements.put(TColumnColumnRelationAnnotation.SCORE_FINAL, 0.0);
            }

            scoreElements.put(TColumnColumnRelationAnnotation.SCORE_FINAL,
                    scoreElements.get(TColumnColumnRelationAnnotation.SCORE_FINAL) + 1.0);
            columncolumnRelationAnnotation.setFinalScore(scoreElements.
                    get(TColumnColumnRelationAnnotation.SCORE_FINAL));

        }
        return new ArrayList<>(output);
    }

    @Override
    public List<TColumnColumnRelationAnnotation> computeREScore(List<TCellCellRelationAnotation> cellcellRelationAnotations, Collection<TColumnColumnRelationAnnotation> output, int subjectCol, int objectCol) throws STIException {
        throw new STIException("Not supported");
    }

    @Override
    public List<TColumnColumnRelationAnnotation> computeRCScore(Collection<TColumnColumnRelationAnnotation> candidates, Table table, int column) throws STIException {
        throw new STIException("Not supported");
    }

    @Override
    public Map<String, Double> computeFinal(TColumnColumnRelationAnnotation relation, int tableRowsTotal) {
        return relation.getScoreElements();
    }

    @Override
    public double scoreDC(TColumnColumnRelationAnnotation hbr, List<String> domain_representation) throws STIException {
        throw new STIException("Not supported");
    }
}
