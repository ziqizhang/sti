package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;
import uk.ac.shef.dcs.oak.sti.rep.HeaderBinaryRelationAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.Key_SubjectCol_ObjectCol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderHeaderAndRelation extends FactorBuilder {

    protected Map<String, Key_SubjectCol_ObjectCol> relationVarOutcomeDirection = new HashMap<String, Key_SubjectCol_ObjectCol>();

    public Map<String, Variable> addFactors(
            Map<Integer, Variable> columnHeaders,
            LTableAnnotation_JI_Freebase annotation,
            FactorGraph graph,
            Map<Variable, String> typeOfVariable,
            Map<String, Boolean> varOutcomeHasNonZeroPotential) {
        Map<String, Variable> result = new HashMap<String, Variable>(); //for each pair of col, will only have 1 key stored, both both directional keys are processed
        List<String> processed = new ArrayList<String>();
        Map<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>>
                candidateRelations = annotation.getRelationAnnotations_across_columns();
        for (int c1 = 0; c1 < annotation.getCols(); c1++) {
            for (int c2 = 0; c2 < annotation.getCols(); c2++) {
                if (c1 == c2) continue;
                if (processed.contains(c1 + "," + c2) || processed.contains(c2 + "," + c1)) continue;
                Key_SubjectCol_ObjectCol relation_direction = new Key_SubjectCol_ObjectCol(c1, c2);
                Key_SubjectCol_ObjectCol relation_direction_reverse = new Key_SubjectCol_ObjectCol(
                        c2, c1
                );
                List<HeaderBinaryRelationAnnotation> candidate_relations = candidateRelations.get(relation_direction);
                if (candidate_relations == null) {
                    candidate_relations = new ArrayList<HeaderBinaryRelationAnnotation>();
                }
                List<HeaderBinaryRelationAnnotation> candidate_relations_reversed = candidateRelations.get(relation_direction_reverse);
                if (candidate_relations_reversed != null) candidate_relations.addAll(candidate_relations_reversed);
                //assuming that a relation can have only
                //1 possible direction. not necessarily true always but reasonable
                if (candidate_relations.size() == 0)
                    continue;

                Map<String, Double> affinity_scores = new HashMap<String, Double>();
                Variable column1_header_variable = columnHeaders.get(relation_direction.getSubjectCol());
                Variable column2_header_variable = columnHeaders.get(relation_direction.getObjectCol());

                LabelAlphabet candidateIndex_relation = new LabelAlphabet();
                for (HeaderBinaryRelationAnnotation hbr : candidate_relations) {
                    int index_relation = candidateIndex_relation.lookupIndex(hbr.toStringExpanded(), true);
                    Key_SubjectCol_ObjectCol current_rel_direction = hbr.getSubject_object_key();
                    relationVarOutcomeDirection.put(hbr.toStringExpanded(), current_rel_direction);

                    if (column1_header_variable != null && column2_header_variable != null) {
                        for (int col1 = 0; col1 < column1_header_variable.getNumOutcomes(); col1++) {
                            String col1_header_concept_url = column1_header_variable.getLabelAlphabet().lookupLabel(col1).toString();
                            for (int col2 = 0; col2 < column2_header_variable.getNumOutcomes(); col2++) {
                                String col2_header_concept_url = column2_header_variable.getLabelAlphabet().lookupLabel(col2).toString();
                                if (current_rel_direction.getSubjectCol() == col1 && current_rel_direction.getObjectCol() == col2) {
                                    double score = annotation.getScore_conceptPairAndRelation(col1_header_concept_url,
                                            hbr.toStringExpanded(), col2_header_concept_url, annotation.getRows());
                                    if (score > 0)
                                        affinity_scores.put(col1 + ">" + index_relation + ">" + col2, score);

                                    //====== debug
                                    checkVariableOutcomeUsage(score, column1_header_variable.getLabel() + "." + col1_header_concept_url,
                                            varOutcomeHasNonZeroPotential);
                                    checkVariableOutcomeUsage(score, column2_header_variable.getLabel() + "." + col2_header_concept_url,
                                            varOutcomeHasNonZeroPotential);
                                    checkVariableOutcomeUsage(score, VariableType.RELATION.toString() + "." +
                                                    hbr.toStringExpanded(),
                                            varOutcomeHasNonZeroPotential);
                                    //====== debug
                                } else if (current_rel_direction.getObjectCol() == col1 && current_rel_direction.getSubjectCol() == col2) {
                                    double score = annotation.getScore_conceptPairAndRelation(col2_header_concept_url,
                                            hbr.toStringExpanded(), col1_header_concept_url, annotation.getRows());
                                    if (score > 0)
                                        affinity_scores.put(col2 + ">" + index_relation + ">" + col2, score);
                                    //====== debug
                                    checkVariableOutcomeUsage(score, column1_header_variable.getLabel() + "." + col1_header_concept_url,
                                            varOutcomeHasNonZeroPotential);
                                    checkVariableOutcomeUsage(score, column2_header_variable.getLabel() + "." + col2_header_concept_url,
                                            varOutcomeHasNonZeroPotential);
                                    checkVariableOutcomeUsage(score, VariableType.RELATION.toString() + "." +
                                                    hbr.toStringExpanded(),
                                            varOutcomeHasNonZeroPotential);
                                    //====== debug
                                }
                            }
                        }
                    }
                    Variable relationVariable = new Variable(candidateIndex_relation);
                    relationVariable.setLabel(VariableType.RELATION.toString() + "." + relation_direction.getSubjectCol() + "," + relation_direction.getObjectCol());
                    typeOfVariable.put(relationVariable, VariableType.RELATION.toString());
                    result.put(relation_direction.getSubjectCol() + "," +
                            relation_direction.getObjectCol(), relationVariable);

                    //create potentials
                    double[] potential1 = computePotential(affinity_scores,
                            column1_header_variable,
                            relationVariable,
                            column2_header_variable);
                    if (isValidPotential(potential1)) {
                        VarSet varSet1 = new HashVarSet(new Variable[]{column1_header_variable, relationVariable, column2_header_variable});
                        TableFactor factor1 = new TableFactor(varSet1, potential1);
                        graph.addFactor(factor1);
                    }

                    processed.add(c1 + "," + c2);
                    processed.add(c2 + "," + c1);
                }
            }

        }
        return result;
    }


}
