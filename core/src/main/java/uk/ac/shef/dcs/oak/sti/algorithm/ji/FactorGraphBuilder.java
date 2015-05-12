package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import cc.mallet.grmm.types.*;
import cc.mallet.grmm.types.Variable;
import cc.mallet.types.LabelAlphabet;
import uk.ac.shef.dcs.oak.sti.rep.*;

import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class FactorGraphBuilder {

    private FactorBuilderCell factorBuilderCell = new FactorBuilderCell();
    private FactorBuilderHeader factorBuilderHeader = new FactorBuilderHeader();
    private FactorBuilderHeaderAndRelation factorBuilderHeaderAndRelation = new FactorBuilderHeaderAndRelation();
    private Map<Variable, String> typeOfVariable = new HashMap<Variable, String>();

    private Map<String, Boolean> varOutcomeHasNonZeroPotential = new HashMap<String, Boolean>();

    public FactorGraph build(LTableAnnotation_JI_Freebase annotation, LTable table) {
        FactorGraph graph = new FactorGraph();
        //cell text and entity label
        Map<String, Variable> cellAnnotations = factorBuilderCell.addFactors(annotation, graph,
                typeOfVariable, varOutcomeHasNonZeroPotential);
        //column header and type label
        Map<Integer, Variable> columnHeaders = factorBuilderHeader.addFactors(annotation, graph,
                typeOfVariable, varOutcomeHasNonZeroPotential);
        //column type and cell entities
        new FactorBuilderHeaderAndCell().addFactors(cellAnnotations,
                columnHeaders,
                annotation,
                graph, varOutcomeHasNonZeroPotential);
        //relation and pair of column types
        Map<String, Variable> relations = factorBuilderHeaderAndRelation.addFactors(
                columnHeaders,
                annotation,
                graph,
                typeOfVariable, varOutcomeHasNonZeroPotential
        );

        //relation and entity pairs
        new FactorBuilderCellAndRelation().addFactors(
                relations,
                cellAnnotations,
                annotation,
                graph,varOutcomeHasNonZeroPotential
        );
        return graph;
    }

    public String getTypeOfVariable(Variable variable) {
        return typeOfVariable.get(variable);
    }

    public int[] getCellPosition(Variable variable) {
        return factorBuilderCell.cellVarOutcomePosition.get(variable);
    }

    public int getHeaderPosition(Variable variable) {
        return factorBuilderHeader.headerVarOutcomePosition.get(variable);
    }

    public Key_SubjectCol_ObjectCol getRelationDirection(String varOutcomeLabel) {
        return factorBuilderHeaderAndRelation.relationVarOutcomeDirection.get(varOutcomeLabel);
    }

    public void dumpCheckVariableOutcomeUsage() {
        List<String> keys = new ArrayList<String>(varOutcomeHasNonZeroPotential.keySet());
        Collections.sort(keys);
        for (String k : keys) {
            if (!varOutcomeHasNonZeroPotential.get(k))
                System.out.println("\tmissed: " + k);
        }
    }

}

