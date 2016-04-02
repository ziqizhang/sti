package uk.ac.shef.dcs.sti.algorithm.tm;

import uk.ac.shef.dcs.sti.rep.TCellCellRelationAnotation;
import uk.ac.shef.dcs.sti.rep.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.rep.Table;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public interface RelationScorer {

    List<TColumnColumnRelationAnnotation> computeElementScores(List<TCellCellRelationAnotation> cellcellRelationsOnRow,
                                                               Collection<TColumnColumnRelationAnnotation> output,
                                                               int subjectCol, int objectCol,
                                                               Table table);

    List<TColumnColumnRelationAnnotation> computeREScore(List<TCellCellRelationAnotation> cellcellRelationAnotations,
                                                         Collection<TColumnColumnRelationAnnotation> output,
                                                         int subjectCol, int objectCol);

    List<TColumnColumnRelationAnnotation> computeRCScore(Collection<TColumnColumnRelationAnnotation> candidates,
                                                         Table table, int column);

    Map<String, Double> computeFinal(TColumnColumnRelationAnnotation relation, int tableRowsTotal);

    double scoreDC(TColumnColumnRelationAnnotation hbr, List<String> domain_representation);
}
