package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;
import cc.mallet.grmm.types.Variable;
import cc.mallet.types.LabelAlphabet;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseFreebaseFilter;
import uk.ac.shef.dcs.oak.sti.rep.*;

import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class FactorGraphBuilder {

    protected static final String CELL_VARIABLE = "cell";
    protected static final String HEADER_VARIABLE = "header";
    protected static final String RELATION_VARIABLE = "relation";
    private Map<Variable, String> typeOfVariable = new HashMap<Variable, String>();
    private Map<Variable, int[]> cellVarOutcomePosition = new HashMap<Variable, int[]>();
    private Map<Variable, Integer> headerVarOutcomePosition = new HashMap<Variable, Integer>();
    private Map<String, Key_SubjectCol_ObjectCol> relationVarOutcomeDirection = new HashMap<String, Key_SubjectCol_ObjectCol>();

    public FactorGraph build(LTableAnnotation_JI_Freebase annotation, LTable table) {
        FactorGraph graph = new FactorGraph();
        //cell text and entity label
        Map<String, Variable> cellAnnotations = addCellAnnotationFactors(annotation, graph);
        //column header and type label
        Map<Integer, Variable> columnHeaders = addColumnHeaderFactors(annotation, table, graph);
        //column type and cell entities
        addHeaderAndCellFactors(cellAnnotations,
                columnHeaders,
                annotation,
                graph);
        //relation and pair of column types
        Map<String, Variable> relations = addRelationAndHeaderFactors(
                columnHeaders,
                annotation,
                graph
        );

        //relation and entity pairs
        addRelationAndCellFactors(
                relations,
                cellAnnotations,
                annotation,
                graph
        );

        return graph;
    }

    private void addRelationAndCellFactors(Map<String, Variable> relationVariables,
                                           Map<String, Variable> cellVariables,
                                           LTableAnnotation_JI_Freebase annotation,
                                           FactorGraph graph) {
        Map<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                relations_per_row = annotation.getRelationAnnotations_per_row();

        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                entry : relations_per_row.entrySet()) {
            Key_SubjectCol_ObjectCol relation_direction = entry.getKey();
            Map<Integer, List<CellBinaryRelationAnnotation>> relations = entry.getValue();
            for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> ent : relations.entrySet()) {
                int row = ent.getKey();
                int column1 = relation_direction.getSubjectCol();
                int column2 = relation_direction.getObjectCol();

                Variable relationVariable = relationVariables.get(column1 + "," + column2);
                if (relationVariable == null)
                    relationVariable = relationVariables.get(column2 + "," + column1);
                if (relationVariable == null)
                    continue;//this should not happen
                Variable cellVariable1 = cellVariables.get(row + "," + column1);
                Variable cellVariable2 = cellVariables.get(row + "," + column2);

                if (cellVariable1 != null) {
                    double[] potential1 = new double[cellVariable1.getNumOutcomes() * relationVariable.getNumOutcomes()];
                    for (int i = 0; i < cellVariable1.getNumOutcomes(); i++) {
                        for (int j = 0; j < relationVariable.getNumOutcomes(); j++) {
                            String cellAnnotationId = cellVariable1.getLabelAlphabet().lookupLabel(i).toString();
                            String relationURL = relationVariable.getLabelAlphabet().lookupLabel(j).toString();
                            double score = annotation.getScore_entityAndRelation(cellAnnotationId, relationURL);
                            potential1[i + j] = score;
                        }
                    }
                    VarSet varSet1 = new HashVarSet(new Variable[]{cellVariable1, relationVariable});
                    TableFactor factor1 = new TableFactor(varSet1, potential1);
                    graph.addFactor(factor1);
                }
                if (cellVariable2 != null) {
                    double[] potential2 = new double[cellVariable2.getNumOutcomes() * relationVariable.getNumOutcomes()];
                    for (int i = 0; i < cellVariable2.getNumOutcomes(); i++) {
                        for (int j = 0; j < relationVariable.getNumOutcomes(); j++) {
                            String cellAnnotationId = cellVariable2.getLabelAlphabet().lookupLabel(i).toString();
                            String relationURL = relationVariable.getLabelAlphabet().lookupLabel(j).toString();
                            double score = annotation.getScore_entityAndRelation(cellAnnotationId, relationURL);
                            potential2[i + j] = score;
                        }
                    }
                    VarSet varSet2 = new HashVarSet(new Variable[]{cellVariable2, relationVariable});
                    TableFactor factor2 = new TableFactor(varSet2, potential2);
                    graph.addFactor(factor2);
                }
            }
        }
    }

    private Map<String, Variable> addRelationAndHeaderFactors(
            Map<Integer, Variable> columnHeaders,
            LTableAnnotation_JI_Freebase annotation,
            FactorGraph graph) {
        Map<String, Variable> result = new HashMap<String, Variable>(); //for each pair of col, will only have 1 key stored, both both directional keys are processed

        Map<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>>
                candidateRelations = annotation.getRelationAnnotations_across_columns();

        List<Key_SubjectCol_ObjectCol> processed = new ArrayList<Key_SubjectCol_ObjectCol>();
        for (Map.Entry<Key_SubjectCol_ObjectCol, List<HeaderBinaryRelationAnnotation>> entry :
                candidateRelations.entrySet()) {
            //the relation and its reversed-directional relation are the same variable, so collect
            //candidates from both
            Key_SubjectCol_ObjectCol relation_direction = entry.getKey();
            if (processed.contains(relation_direction))
                continue;

            Key_SubjectCol_ObjectCol relation_direction_reverse = new Key_SubjectCol_ObjectCol(
                    relation_direction.getObjectCol(), relation_direction.getSubjectCol()
            );
            processed.add(relation_direction);
            processed.add(relation_direction_reverse);

            List<HeaderBinaryRelationAnnotation> candidate_relations = entry.getValue();
            List<HeaderBinaryRelationAnnotation> candidate_relations_reversed =
                    candidateRelations.get(relation_direction_reverse);
            if (candidate_relations_reversed != null)
                candidate_relations.addAll(candidate_relations_reversed);  //assuming that a relation can have only
            //1 possible direction. not necessarily true always but reasonable
            Map<String, Double> affinity_scores_column1_and_relation = new HashMap<String, Double>();
            Map<String, Double> affinity_scores_column2_and_relation = new HashMap<String, Double>();
            Variable column1_header_variable = columnHeaders.get(relation_direction.getSubjectCol());
            Variable column2_header_variable = columnHeaders.get(relation_direction.getObjectCol());

            //index all relation candidate for this relation variable
            LabelAlphabet candidateIndex_relation = new LabelAlphabet();
            for (HeaderBinaryRelationAnnotation hbr : candidate_relations) {
                int index_relation = candidateIndex_relation.lookupIndex(hbr.getAnnotation_url(), true);

                relationVarOutcomeDirection.put(hbr.getAnnotation_url(), hbr.getSubject_object_key());

                if(column1_header_variable!=null) {
                    for (int c = 0; c < column1_header_variable.getNumOutcomes(); c++) {
                        String header_concept_url = column1_header_variable.getLabelAlphabet().lookupLabel(c).toString();
                        double score = annotation.getScore_conceptAndRelation(header_concept_url, hbr.getAnnotation_url());
                        if (score > 0)
                            affinity_scores_column1_and_relation.put(c + "," + index_relation, score);
                    }
                }
                if(column2_header_variable!=null) {
                    for (int c = 0; c < column2_header_variable.getNumOutcomes(); c++) {
                        String header_concept_url = column2_header_variable.getLabelAlphabet().lookupLabel(c).toString();
                        double score = annotation.getScore_conceptAndRelation(header_concept_url, hbr.getAnnotation_url());
                        if (score > 0)
                            affinity_scores_column2_and_relation.put(c + "," + index_relation, score);
                    }
                }
            }
            Variable relationVariable = new Variable(candidateIndex_relation);
            typeOfVariable.put(relationVariable, RELATION_VARIABLE);
            result.put(relation_direction.getSubjectCol() + "," +
                    relation_direction.getObjectCol(), relationVariable);

            //create potentials
            if(column1_header_variable!=null) {
                double[] potential1 = computePotential(affinity_scores_column1_and_relation, column1_header_variable,
                        relationVariable);
                VarSet varSet1 = new HashVarSet(new Variable[]{column1_header_variable, relationVariable});
                TableFactor factor1 = new TableFactor(varSet1, potential1);
                graph.addFactor(factor1);
            }
            if(column2_header_variable!=null) {
                double[] potential2 = computePotential(affinity_scores_column2_and_relation, column2_header_variable,
                        relationVariable);
                VarSet varSet2 = new HashVarSet(new Variable[]{column2_header_variable, relationVariable});
                TableFactor factor2 = new TableFactor(varSet2, potential2);
                graph.addFactor(factor2);
            }
        }
        return result;
    }

    private void addHeaderAndCellFactors(Map<String, Variable> cellVariables,
                                         Map<Integer, Variable> headerVariables,
                                         LTableAnnotation_JI_Freebase annotation,
                                         FactorGraph graph) {
        for (int row = 0; row < annotation.getRows(); row++) {
            for (int col = 0; col < annotation.getCols(); col++) {
                CellAnnotation[] candidateEntityAnnotations = annotation.getContentCellAnnotations(row, col);
                if (candidateEntityAnnotations.length == 0)
                    continue;

                Variable cellVar = cellVariables.get(row + "," + col);
                Variable headerVar = headerVariables.get(col);

                Map<String, Double> affinity_values_between_variable_outcomes = new HashMap<String, Double>();
                //go thru every candidate cell entity
                for (CellAnnotation ca : candidateEntityAnnotations) {
                    //which concept it has a relation with
                    String entId = ca.getAnnotation().getId();
                    int cellVarOutcomeIndex = cellVar.getLabelAlphabet().lookupIndex(entId, false);
                    if (cellVarOutcomeIndex < 0) continue;
                    for (String[] type : KnowledgeBaseFreebaseFilter.filterTypes(ca.getAnnotation().getTypes())) {
                        int headerVarOutcomeIndex = headerVar.getLabelAlphabet().lookupIndex(type[0], false);
                        if (headerVarOutcomeIndex < 0) continue;

                        affinity_values_between_variable_outcomes.put(
                                cellVarOutcomeIndex + "," + headerVarOutcomeIndex,
                                annotation.getScore_entityAndConcept(entId, type[0]));

                    }
                }

                if (affinity_values_between_variable_outcomes.size() > 0) {
                    double[] potential = computePotential(affinity_values_between_variable_outcomes,
                            cellVar, headerVar);
                    VarSet varSet = new HashVarSet(new Variable[]{cellVar, headerVar});
                    TableFactor factor = new TableFactor(varSet, potential);
                    graph.addFactor(factor);
                }
            }
        }
    }

    /**
     * @param affinity_values_between_variable_outcomes in the key, the first element (int) must correspond to the index in cellVar; the second must
     *                                                  correpsond to the index in headerVar
     * @param firstVar
     * @param secondVar
     * @return
     */
    private double[] computePotential(
            Map<String, Double> affinity_values_between_variable_outcomes,
            Variable firstVar,
            Variable secondVar) {
        int dimensionFirstVar = firstVar.getNumOutcomes();
        int dimensionSecondVar = secondVar.getNumOutcomes();
        double[] res = new double[dimensionFirstVar * dimensionSecondVar];
        for (int first = 0; first < dimensionFirstVar; first++) {
            for (int second = 0; second < dimensionSecondVar; second++) {
                String key = first + "," + second;
                Double affinity = affinity_values_between_variable_outcomes.get(key);

                if (affinity == null)
                    affinity = 0.0;
                res[first + second] = affinity;
            }
        }
        return res;
    }

    private Map<String, Variable> addCellAnnotationFactors(LTableAnnotation annotation, FactorGraph graph) {
        Map<String, Variable> variables = new HashMap<String, Variable>();
        for (int row = 0; row < annotation.getRows(); row++) {
            for (int col = 0; col < annotation.getCols(); col++) {
                CellAnnotation[] candidateEntityAnnotations = annotation.getContentCellAnnotations(row, col);
                if (candidateEntityAnnotations.length == 0)
                    continue;
                String cellText = String.valueOf(row) + "," + String.valueOf(col);

                LabelAlphabet candidateIndex_cell = new LabelAlphabet();
                double[] potential = new double[candidateEntityAnnotations.length];
                for (int i = 0; i < candidateEntityAnnotations.length; i++) {
                    CellAnnotation ca = candidateEntityAnnotations[i];
                    candidateIndex_cell.lookupIndex(ca.getAnnotation().getId());

                    potential[i] = ca.getScore_element_map().get(
                            DisambiguationScorer_JI_adapted.SCORE_CELL_FACTOR
                    );
                }
                Variable variable_cell = new Variable(candidateIndex_cell);
                variable_cell.setLabel(cellText);
                typeOfVariable.put(variable_cell, CELL_VARIABLE);
                cellVarOutcomePosition.put(variable_cell, new int[]{row, col});

                TableFactor factor = new TableFactor(variable_cell, potential);
                graph.addFactor(factor);
                variables.put(row + "," + col, variable_cell);
            }
        }
        return variables;
    }

    protected Map<Integer, Variable> addColumnHeaderFactors(LTableAnnotation annotation, LTable table,
                                                            FactorGraph graph) {
        Map<Integer, Variable> variables = new HashMap<Integer, Variable>();
        for (int col = 0; col < annotation.getCols(); col++) {
            HeaderAnnotation[] candidateConcepts_header = annotation.getHeaderAnnotation(col);
            if (candidateConcepts_header.length == 0)
                continue;

            String headerText = String.valueOf(col);
            LabelAlphabet candidateIndex_header = new LabelAlphabet();

            double[] potential = new double[candidateConcepts_header.length];
            for (int i = 0; i < candidateConcepts_header.length; i++) {
                HeaderAnnotation ha = candidateConcepts_header[i];
                candidateIndex_header.lookupIndex(ha.getAnnotation_url());

                potential[i] = ha.getScoreElements().get(
                        ClassificationScorer_JI_adapted.SCORE_HEADER_FACTOR
                );
            }
            Variable variable_header = new Variable(candidateIndex_header);
            variable_header.setLabel(headerText);
            typeOfVariable.put(variable_header, HEADER_VARIABLE);
            headerVarOutcomePosition.put(variable_header, col);
            TableFactor factor = new TableFactor(variable_header, potential);
            graph.addFactor(factor);
            variables.put(col, variable_header);
        }
        return variables;
    }

    public String getTypeOfVariable(Variable variable) {
        return typeOfVariable.get(variable);
    }

    public int[] getCellPosition(Variable variable) {
        return cellVarOutcomePosition.get(variable);
    }

    public int getHeaderPosition(Variable variable) {
        return headerVarOutcomePosition.get(variable);
    }

    public Key_SubjectCol_ObjectCol getRelationDirection(String varOutcomeLabel) {
        return relationVarOutcomeDirection.get(varOutcomeLabel);
    }
}

