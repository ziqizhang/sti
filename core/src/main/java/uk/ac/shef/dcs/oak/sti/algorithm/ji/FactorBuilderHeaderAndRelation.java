package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;
import cc.mallet.types.LabelAlphabet;
import uk.ac.shef.dcs.oak.sti.rep.HeaderBinaryRelationAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.Key_SubjectCol_ObjectCol;

import java.util.*;

/**
 * Created by zqz on 12/05/2015.
 */
class FactorBuilderHeaderAndRelation extends FactorBuilder {

    protected Map<String, Key_SubjectCol_ObjectCol> relationVarOutcomeDirection = new HashMap<String, Key_SubjectCol_ObjectCol>();

    public Map<String, Key_SubjectCol_ObjectCol> getRelationVarOutcomeDirection() {
        return relationVarOutcomeDirection;
    }

    public Map<String, Variable> addFactors(
            Map<Integer, Variable> columnHeaders,
            LTableAnnotation_JI_Freebase annotation,
            FactorGraph graph,
            Map<Variable, String> typeOfVariable,
            String tableId, Set<Integer> columns) {
        Map<String, Variable> result = new HashMap<String, Variable>(); //for each pair of col, will only have 1 key stored, both both directional keys are processed
        List<String> processed = new ArrayList<String>();
        Map<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>>
                candidateRelations = annotation.getRelationAnnotations_across_columns();
        for (int c1 = 0; c1 < annotation.getCols(); c1++) {
            for (int c2 = 0; c2 < annotation.getCols(); c2++) {
                if (c1 == c2) continue;
                if(columns!=null&& !columns.contains(c1)&&columns.contains(c2)) continue;

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

                Collections.sort(candidate_relations, new Comparator<HeaderBinaryRelationAnnotation>() {
                    @Override
                    public int compare(HeaderBinaryRelationAnnotation o1, HeaderBinaryRelationAnnotation o2) {
                        return o1.getAnnotation_url().compareTo(o2.getAnnotation_url());
                    }
                });
                LabelAlphabet candidateIndex_relation = new LabelAlphabet();

                //key- outcome index of a relation var; value-true if relation is forward; false otherwise
                Map<Integer, Boolean> relationIndex_forwardRelation = new HashMap<Integer, Boolean>();
                for (HeaderBinaryRelationAnnotation hbr : candidate_relations) {
                    int index_relation = candidateIndex_relation.lookupIndex(hbr.toStringExpanded(), true);
                    Key_SubjectCol_ObjectCol current_rel_direction = hbr.getSubject_object_key();
                    relationVarOutcomeDirection.put(hbr.toStringExpanded(), current_rel_direction);

                    if (column1_header_variable != null && column2_header_variable != null) {
                        for (int col1_outcome = 0; col1_outcome < column1_header_variable.getNumOutcomes(); col1_outcome++) {
                            String col1_header_concept_url = column1_header_variable.getLabelAlphabet().lookupLabel(col1_outcome).toString();
                            for (int col2_outcome = 0; col2_outcome < column2_header_variable.getNumOutcomes(); col2_outcome++) {
                                String col2_header_concept_url = column2_header_variable.getLabelAlphabet().lookupLabel(col2_outcome).toString();
                                double score = 0.0;
                                if (current_rel_direction.getSubjectCol() == c1 && current_rel_direction.getObjectCol() == c2) {
                                    score = annotation.getScore_conceptPairAndRelation(col1_header_concept_url,
                                            hbr.toStringExpanded(), col2_header_concept_url, annotation.getRows());
                                } else if (current_rel_direction.getObjectCol() == c1 && current_rel_direction.getSubjectCol() == c2) {

                                    score = annotation.getScore_conceptPairAndRelation(col2_header_concept_url,
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
                if (isValidCompatibility(compatibility, affinity_scores)) {
                    if(patchScores) compatibility= patchCompatibility(compatibility);
                    VarSet varSet;
                    if (column1_header_variable.getIndex() < column2_header_variable.getIndex())
                        varSet = new HashVarSet(new Variable[]{column1_header_variable, column2_header_variable, relationVariable});
                    else
                        varSet = new HashVarSet(new Variable[]{column2_header_variable, column1_header_variable, relationVariable});
                    TableFactor factor1 = new TableFactor(varSet, compatibility);
                    GraphCheckingUtil.checkFactorAgainstAffinity(factor1, affinity_scores,tableId);
                    graph.addFactor(factor1);
                }
            }

        }
        return result;
    }

    public Map<String, Variable> addFactors(
            Map<Integer, Variable> columnHeaders,
            LTableAnnotation_JI_Freebase annotation,
            FactorGraph graph,
            Map<Variable, String> typeOfVariable,
            String tableId) {
        return addFactors(columnHeaders, annotation, graph, typeOfVariable, tableId, null);
    }


}
