package uk.ac.shef.dcs.sti.core.algorithm.ji.factorgraph;

import cc.mallet.grmm.types.*;
import cc.mallet.grmm.types.Variable;
import cc.mallet.types.LabelAlphabet;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.ji.DebuggingUtil;
import uk.ac.shef.dcs.sti.core.algorithm.ji.TAnnotationJI;
import uk.ac.shef.dcs.sti.core.algorithm.ji.VariableType;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;

import java.util.*;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderHeaderAndRelation extends FactorBuilder {

    protected Map<String, RelationColumns> relationVarOutcomeDirection = new HashMap<>();
    public Map<String, RelationColumns> getRelationVarOutcomeDirection() {
        return relationVarOutcomeDirection;
    }

    public Map<String, Variable> addFactors(
            Map<Integer, Variable> columnHeaders,
            TAnnotationJI annotation,
            FactorGraph graph,
            Map<Variable, String> typeOfVariable,
            String tableId, Set<Integer> columns) throws STIException {
        Map<String, Variable> result = new HashMap<>(); //for each pair of col, will only have 1 key stored, both both directional keys are processed
        List<String> processed = new ArrayList<>();
        Map<RelationColumns, List<TColumnColumnRelationAnnotation>>
                candidateRelations = annotation.getColumncolumnRelations();
        for (int c1 = 0; c1 < annotation.getCols(); c1++) {
            for (int c2 = 0; c2 < annotation.getCols(); c2++) {
                if (c1 == c2) continue;
                if(columns!=null&& !columns.contains(c1)&&columns.contains(c2)) continue;

                if (processed.contains(c1 + "," + c2) || processed.contains(c2 + "," + c1)) continue;
                RelationColumns relation_direction = new RelationColumns(c1, c2);
                RelationColumns relation_direction_reverse = new RelationColumns(
                        c2, c1
                );
                List<TColumnColumnRelationAnnotation> candidate_relations = candidateRelations.get(relation_direction);
                if (candidate_relations == null) {
                    candidate_relations = new ArrayList<>();
                }
                List<TColumnColumnRelationAnnotation> candidate_relations_reversed = candidateRelations.get(relation_direction_reverse);
                if (candidate_relations_reversed != null) candidate_relations.addAll(candidate_relations_reversed);
                //assuming that a relation can have only
                //1 possible direction. not necessarily true always but reasonable
                if (candidate_relations.size() == 0)
                    continue;

                Map<String, Double> affinity_scores = new HashMap<String, Double>();
                Variable column1_header_variable = columnHeaders.get(relation_direction.getSubjectCol());
                Variable column2_header_variable = columnHeaders.get(relation_direction.getObjectCol());
                if(column1_header_variable==null||column2_header_variable==null)
                    continue;

                Collections.sort(candidate_relations, (o1, o2) -> o1.getRelationURI().compareTo(o2.getRelationURI()));
                LabelAlphabet candidateIndex_relation = new LabelAlphabet();

                //key- outcome index of a relation var; value-true if relation is forward; false otherwise
                Map<Integer, Boolean> relationIndex_forwardRelation = new HashMap<>();
                for (TColumnColumnRelationAnnotation hbr : candidate_relations) {
                    int index_relation = candidateIndex_relation.lookupIndex(hbr.toStringExpanded(), true);
                    RelationColumns current_rel_direction = hbr.getRelationColumns();
                    relationVarOutcomeDirection.put(hbr.toStringExpanded(), current_rel_direction);

                    if (column1_header_variable != null && column2_header_variable != null) {
                        for (int col1_outcome = 0; col1_outcome < column1_header_variable.getNumOutcomes(); col1_outcome++) {
                            String col1_header_concept_url = column1_header_variable.getLabelAlphabet().lookupLabel(col1_outcome).toString();
                            for (int col2_outcome = 0; col2_outcome < column2_header_variable.getNumOutcomes(); col2_outcome++) {
                                String col2_header_concept_url = column2_header_variable.getLabelAlphabet().lookupLabel(col2_outcome).toString();
                                double score = 0.0;
                                if (current_rel_direction.getSubjectCol() == c1 && current_rel_direction.getObjectCol() == c2) {
                                    score = annotation.getScoreClazzPairAndRelation(col1_header_concept_url,
                                            hbr.toStringExpanded(), col2_header_concept_url, annotation.getRows());
                                } else if (current_rel_direction.getObjectCol() == c1 && current_rel_direction.getSubjectCol() == c2) {

                                    score = annotation.getScoreClazzPairAndRelation(col2_header_concept_url,
                                            hbr.toStringExpanded(), col1_header_concept_url, annotation.getRows());
                                }
                                if (score > 0) {
                                    if (column1_header_variable.getIndex() < column2_header_variable.getIndex()) {
                                        affinity_scores.put(col1_outcome + ">" + col2_outcome + ">" + index_relation, score);
                                        relationIndex_forwardRelation.put(index_relation, true);
                                    } else {
                                        affinity_scores.put(col2_outcome + ">" + col1_outcome + ">" + index_relation, score);
                                        relationIndex_forwardRelation.put(index_relation, false);
                                    }
                                }
                            }
                        }
                    }
                    processed.add(c1 + "," + c2);
                    processed.add(c2 + "," + c1);
                }

                Variable relationVariable = new Variable(candidateIndex_relation);
                relationVariable.setLabel(VariableType.RELATION.toString() + "." + relation_direction.getSubjectCol() + "," + relation_direction.getObjectCol());
                typeOfVariable.put(relationVariable, VariableType.RELATION.toString());
                result.put(relation_direction.getSubjectCol() + "," +
                        relation_direction.getObjectCol(), relationVariable);

                //create potentials
                double[] compatibility;
                if (column1_header_variable.getIndex() < column2_header_variable.getIndex()) {
                    compatibility = computePotential(affinity_scores,
                            column1_header_variable,
                            column2_header_variable, relationVariable, relationIndex_forwardRelation);
                } else {
                    compatibility = computePotential(affinity_scores,
                            column2_header_variable,
                            column1_header_variable, relationVariable, relationIndex_forwardRelation);
                }
                if (isValidGraphAffinity(compatibility, affinity_scores)) {
                    Variable[] vars = null;
                    if (column1_header_variable.getIndex() < column2_header_variable.getIndex())
                        vars = new Variable[]{column1_header_variable, column2_header_variable, relationVariable};
                    else
                        vars = new Variable[]{column2_header_variable, column1_header_variable, relationVariable};

                    TableFactor factor1 = new TableFactor(vars, compatibility);
                    DebuggingUtil.debugFactorAndAffinity(factor1, affinity_scores, tableId);
                    graph.addFactor(factor1);
                }
                else{
                    throw new STIException("Fatal: inconsistency detected on graph, while mapping affinity scores to potentials");
                }
            }

        }
        return result;
    }

    public Map<String, Variable> addFactors(
            Map<Integer, Variable> columnHeaders,
            TAnnotationJI annotation,
            FactorGraph graph,
            Map<Variable, String> typeOfVariable,
            String tableId) throws STIException {
        return addFactors(columnHeaders, annotation, graph, typeOfVariable, tableId, null);
    }


}
