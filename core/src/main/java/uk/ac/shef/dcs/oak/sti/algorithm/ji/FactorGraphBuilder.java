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

    private Map<String, Boolean> varOutcomeHasNonZeroPotential = new HashMap<String, Boolean>();

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
        /*addRelationAndCellFactors(
                relations,
                cellAnnotations,
                annotation,
                graph
        );*/
        return graph;
    }

    private void addRelationAndCellFactors(Map<String, Variable> relationVariables,
                                           Map<String, Variable> cellVariables,
                                           LTableAnnotation_JI_Freebase annotation,
                                           FactorGraph graph) {
        List<String> processed = new ArrayList<String>();
        for (int c1 = 0; c1 < annotation.getCols(); c1++) {
            for (int c2 = 0; c2 < annotation.getCols(); c2++) {
                if (c1 == c2) continue;
                if (processed.contains(c1 + "," + c2) || processed.contains(c2 + "," + c1)) continue;
                Variable relation_var = relationVariables.get(c1 + "," + c2);
                if (relation_var != null) {
                    //there is a relation between c1, c2, go thru each row, to create factor between the cell pair and relation
                    for (int r = 0; r < annotation.getRows(); r++) {
                        Variable sbj_cell_var = cellVariables.get(r + "," + c1);
                        createCellRelationFactor(sbj_cell_var, relation_var, annotation, graph);
                        Variable obj_cell_var = cellVariables.get(r + "," + c2);
                        createCellRelationFactor(obj_cell_var, relation_var, annotation, graph);
                    }
                } else {
                    relation_var = relationVariables.get(c2 + "," + c1);
                    if (relation_var != null) {
                        for (int r = 0; r < annotation.getRows(); r++) {
                            Variable sbj_cell_var = cellVariables.get(r + "," + c2);
                            createCellRelationFactor(sbj_cell_var, relation_var, annotation, graph);
                            Variable obj_cell_var = cellVariables.get(r + "," + c1);
                            createCellRelationFactor(obj_cell_var, relation_var, annotation, graph);
                        }
                    }
                }
                processed.add(c1 + "," + c2);
                processed.add(c2 + "," + c1);
            }
        }

    }

    private void createCellRelationFactor(Variable cellVar, Variable relationVar,
                                          LTableAnnotation_JI_Freebase annotation,
                                          FactorGraph graph) {
        if (cellVar != null) {
            Map<String, Double> affinity_scores = new HashMap<String, Double>();
            for (int i = 0; i < cellVar.getNumOutcomes(); i++) {
                String sbj = cellVar.getLabelAlphabet().lookupLabel(i).toString();
                for (int j = 0; j < relationVar.getNumOutcomes(); j++) {
                    String rel = relationVar.getLabelAlphabet().lookupLabel(j).toString();
                    double score = annotation.getScore_entityAndRelation(sbj, rel);
                    if (score > 0) {
                        affinity_scores.put(i + "," + j, score);
                    }
                    checkVariableOutcomeUsage(score, cellVar.getLabel() + "." + sbj);
                    checkVariableOutcomeUsage(score, relationVar.getLabel() + "." + rel);
                }
            }
            if (affinity_scores.size() > 0) {
                double[] potential = computePotential(affinity_scores,
                        cellVar, relationVar);
                if (isValidPotential(potential, "cell-relation(" + cellVar.getLabel() + ")," + relationVar.getLabel())) {
                    VarSet varSet = new HashVarSet(new Variable[]{cellVar, relationVar});
                    TableFactor factor = new TableFactor(varSet, potential);
                    graph.addFactor(factor);
                }
            }
        }
    }

    /*private void addRelationAndCellFactors(Map<String, Variable> relationVariables,
                                           Map<String, Variable> cellVariables,
                                           LTableAnnotation_JI_Freebase annotation,
                                           FactorGraph graph) {
        Map<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                relations_per_row = annotation.getRelationAnnotations_per_row();

        List<Key_SubjectCol_ObjectCol> processed = new ArrayList<Key_SubjectCol_ObjectCol>();

        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>>
                entry : relations_per_row.entrySet()) {
            Key_SubjectCol_ObjectCol relation_direction = entry.getKey();
            if (processed.contains(relation_direction))
                continue;

            int column1 = relation_direction.getSubjectCol();
            int column2 = relation_direction.getObjectCol();
            Variable relationVariable = relationVariables.get(column1 + "," + column2);
            if (relationVariable == null)
                relationVariable = relationVariables.get(column2 + "," + column1);
            if (relationVariable == null)
                continue;//this should not happen

            Map<Integer, List<CellBinaryRelationAnnotation>> relation_candidates = entry.getValue();

            Key_SubjectCol_ObjectCol relation_direction_reversed = new Key_SubjectCol_ObjectCol(
                    relation_direction.getObjectCol(), relation_direction.getSubjectCol()
            );
            Map<Integer, List<CellBinaryRelationAnnotation>> reverse_relation_candidates =
                    relations_per_row.get(relation_direction_reversed);
            if (reverse_relation_candidates == null)
                reverse_relation_candidates = new HashMap<Integer, List<CellBinaryRelationAnnotation>>();

            processed.add(relation_direction);
            processed.add(relation_direction_reversed);

            for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> ent : relation_candidates.entrySet()) {
                int row = ent.getKey();

                Variable cellVariable1 = cellVariables.get(row + "," + column1);
                if (cellVariable1 != null) {
                    double[] potential1 = new double[cellVariable1.getNumOutcomes() * relationVariable.getNumOutcomes()];
                    for (int i = 0; i < cellVariable1.getNumOutcomes(); i++) {
                        for (int j = 0; j < relationVariable.getNumOutcomes(); j++) {
                            String cellAnnotationId = cellVariable1.getLabelAlphabet().lookupLabel(i).toString();
                            String relationURL = relationVariable.getLabelAlphabet().lookupLabel(j).toString();
                            double score = annotation.getScore_entityAndRelation(cellAnnotationId, relationURL);
                            potential1[i * relationVariable.getNumOutcomes() + j] = score;
                        }
                    }
                    if (isValidPotential(potential1, "cell-relation(" + cellVariable1.getLabel() + ")," + relationVariable.getLabel())) {
                        VarSet varSet1 = new HashVarSet(new Variable[]{cellVariable1, relationVariable});
                        TableFactor factor1 = new TableFactor(varSet1, potential1);
                        graph.addFactor(factor1);
                    }
                }

                Variable cellVariable2 = cellVariables.get(row + "," + column2);
                if (reverse_relation_candidates.get(row) != null && cellVariable2 != null) {
                    double[] potential2 = new double[cellVariable2.getNumOutcomes() * relationVariable.getNumOutcomes()];
                    for (int i = 0; i < cellVariable2.getNumOutcomes(); i++) {
                        for (int j = 0; j < relationVariable.getNumOutcomes(); j++) {
                            String cellAnnotationId = cellVariable2.getLabelAlphabet().lookupLabel(i).toString();
                            String relationURL = relationVariable.getLabelAlphabet().lookupLabel(j).toString();
                            double score = annotation.getScore_entityAndRelation(cellAnnotationId, relationURL);
                            potential2[i * relationVariable.getNumOutcomes() + j] = score;
                        }
                    }
                    if (isValidPotential(potential2, "cell-relation(" + cellVariable2.getLabel() + ")," + relationVariable.getLabel())) {
                        VarSet varSet2 = new HashVarSet(new Variable[]{cellVariable2, relationVariable});
                        TableFactor factor2 = new TableFactor(varSet2, potential2);
                        graph.addFactor(factor2);
                    }
                }
            }
        }
    }*/

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

                if (column1_header_variable != null) {
                    for (int c = 0; c < column1_header_variable.getNumOutcomes(); c++) {
                        String header_concept_url = column1_header_variable.getLabelAlphabet().lookupLabel(c).toString();
                        double score = annotation.getScore_conceptAndRelation(header_concept_url, hbr.getAnnotation_url());
                        if (score > 0) {
                            affinity_scores_column1_and_relation.put(c + "," + index_relation, score);
                        }
                        checkVariableOutcomeUsage(score, column1_header_variable.getLabel() + "." + header_concept_url);
                        checkVariableOutcomeUsage(score, RELATION_VARIABLE + "." + relation_direction.getSubjectCol() + "," + relation_direction.getObjectCol() + "." + hbr.getAnnotation_url() + "." + hbr.getAnnotation_url());
                    }
                }
                if (column2_header_variable != null) {
                    for (int c = 0; c < column2_header_variable.getNumOutcomes(); c++) {
                        String header_concept_url = column2_header_variable.getLabelAlphabet().lookupLabel(c).toString();
                        double score = annotation.getScore_conceptAndRelation(header_concept_url, hbr.getAnnotation_url());
                        if (score > 0) {
                            affinity_scores_column2_and_relation.put(c + "," + index_relation, score);
                        }
                        checkVariableOutcomeUsage(score, column2_header_variable.getLabel() + "." + header_concept_url);
                        checkVariableOutcomeUsage(score, RELATION_VARIABLE + "." + relation_direction.getSubjectCol() + "," + relation_direction.getObjectCol() + "." + hbr.getAnnotation_url());
                    }
                }
            }
            Variable relationVariable = new Variable(candidateIndex_relation);
            relationVariable.setLabel(RELATION_VARIABLE + "." + relation_direction.getSubjectCol() + "," + relation_direction.getObjectCol());
            typeOfVariable.put(relationVariable, RELATION_VARIABLE);
            result.put(relation_direction.getSubjectCol() + "," +
                    relation_direction.getObjectCol(), relationVariable);

            //create potentials
            if (column1_header_variable != null) {
                double[] potential1 = computePotential(affinity_scores_column1_and_relation, column1_header_variable,
                        relationVariable);
                if (isValidPotential(potential1, "header-relation(" + column1_header_variable.getLabel() + ")," + relationVariable.getLabel())) {
                    VarSet varSet1 = new HashVarSet(new Variable[]{column1_header_variable, relationVariable});
                    TableFactor factor1 = new TableFactor(varSet1, potential1);
                    graph.addFactor(factor1);
                }
            }
            if (column2_header_variable != null) {
                double[] potential2 = computePotential(affinity_scores_column2_and_relation, column2_header_variable,
                        relationVariable);
                if (isValidPotential(potential2, "header-relation(" + column2_header_variable.getLabel() + ")," + relationVariable.getLabel())) {
                    VarSet varSet2 = new HashVarSet(new Variable[]{column2_header_variable, relationVariable});
                    TableFactor factor2 = new TableFactor(varSet2, potential2);
                    graph.addFactor(factor2);
                }
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

                        double score = annotation.getScore_entityAndConcept(entId, type[0]);
                        if (score > 0) {
                            affinity_values_between_variable_outcomes.put(
                                    cellVarOutcomeIndex + "," + headerVarOutcomeIndex, score
                            );
                        }
                        checkVariableOutcomeUsage(score, headerVar.getLabel() + "." + type[0]);
                        checkVariableOutcomeUsage(score, cellVar.getLabel() + "." + entId);
                    }
                }

                if (affinity_values_between_variable_outcomes.size() > 0) {
                    double[] potential = computePotential(affinity_values_between_variable_outcomes,
                            cellVar, headerVar);
                    if (isValidPotential(potential, "cell-header(" + cellVar.getLabel() + ")," + headerVar.getLabel())) {
                        VarSet varSet = new HashVarSet(new Variable[]{cellVar, headerVar});
                        TableFactor factor = new TableFactor(varSet, potential);
                        graph.addFactor(factor);
                    }
                }
            }
        }
    }

    private Map<String, Variable> addCellAnnotationFactors(LTableAnnotation annotation, FactorGraph graph) {
        Map<String, Variable> variables = new HashMap<String, Variable>();
        for (int row = 0; row < annotation.getRows(); row++) {
            for (int col = 0; col < annotation.getCols(); col++) {
                CellAnnotation[] candidateEntityAnnotations = annotation.getContentCellAnnotations(row, col);
                if (candidateEntityAnnotations.length == 0)
                    continue;
                String cellPosition = String.valueOf(row) + "," + String.valueOf(col);

                LabelAlphabet candidateIndex_cell = new LabelAlphabet();
                double[] potential = new double[candidateEntityAnnotations.length];
                for (int i = 0; i < candidateEntityAnnotations.length; i++) {
                    CellAnnotation ca = candidateEntityAnnotations[i];
                    candidateIndex_cell.lookupIndex(ca.getAnnotation().getId());

                    potential[i] = ca.getScore_element_map().get(
                            DisambiguationScorer_JI_adapted.SCORE_CELL_FACTOR
                    );
                    checkVariableOutcomeUsage(potential[i], CELL_VARIABLE + "." + cellPosition + "." + ca.getAnnotation().getId());
                }
                Variable variable_cell = new Variable(candidateIndex_cell);
                variable_cell.setLabel(CELL_VARIABLE + "." + cellPosition);
                typeOfVariable.put(variable_cell, CELL_VARIABLE);
                cellVarOutcomePosition.put(variable_cell, new int[]{row, col});

                if (isValidPotential(potential, "cell(" + variable_cell.getLabel() + ")")) {
                    TableFactor factor = new TableFactor(variable_cell, potential);
                    graph.addFactor(factor);
                    variables.put(row + "," + col, variable_cell);
                }
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

            String headerPosition = String.valueOf(col);
            LabelAlphabet candidateIndex_header = new LabelAlphabet();

            double[] potential = new double[candidateConcepts_header.length];
            for (int i = 0; i < candidateConcepts_header.length; i++) {
                HeaderAnnotation ha = candidateConcepts_header[i];
                candidateIndex_header.lookupIndex(ha.getAnnotation_url());

                potential[i] = ha.getScoreElements().get(
                        ClassificationScorer_JI_adapted.SCORE_HEADER_FACTOR
                );
                checkVariableOutcomeUsage(potential[i], HEADER_VARIABLE + "." + headerPosition + "." + ha.getAnnotation_url());

            }
            Variable variable_header = new Variable(candidateIndex_header);
            variable_header.setLabel(HEADER_VARIABLE + "." + headerPosition);
            typeOfVariable.put(variable_header, HEADER_VARIABLE);
            headerVarOutcomePosition.put(variable_header, col);

            if (isValidPotential(potential, "header " + variable_header.getLabel())) {
                TableFactor factor = new TableFactor(variable_header, potential);
                graph.addFactor(factor);
                variables.put(col, variable_header);
            }
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

    private boolean isValidPotential(double[] potential1, String note) {
        int countZero = 0;
        for (int i = 0; i < potential1.length; i++) {
            if (potential1[i] == 0)
                countZero++;
        }
        System.out.println(note + ":" + countZero + "/" + potential1.length);
        if (countZero == potential1.length)
            return false;
        return true;
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
                res[first * dimensionSecondVar + second] = affinity;
            }
        }
        return res;
    }

    private void checkVariableOutcomeUsage(double potential, String key) {
        Boolean hasNonZeroPotential =
                varOutcomeHasNonZeroPotential.
                        get(key);
        if (hasNonZeroPotential == null) {
            hasNonZeroPotential=false;
            varOutcomeHasNonZeroPotential.put(key, hasNonZeroPotential);
        }
        if (potential > 0) {
            if (!hasNonZeroPotential)
                varOutcomeHasNonZeroPotential.put(key, true);
        }
    }

    public void dumpCheckVariableOutcomeUsage() {
        List<String> keys = new ArrayList<String>(varOutcomeHasNonZeroPotential.keySet());
        Collections.sort(keys);
        for (String k : keys) {
            if(!varOutcomeHasNonZeroPotential.get(k))
                System.out.println("\t"+k);
        }
    }

}

