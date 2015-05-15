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

    protected FactorBuilderCell factorBuilderCell = new FactorBuilderCell();
    protected FactorBuilderHeader factorBuilderHeader = new FactorBuilderHeader();
    protected FactorBuilderHeaderAndRelation factorBuilderHeaderAndRelation = new FactorBuilderHeaderAndRelation();
    protected Map<Variable, String> typeOfVariable = new HashMap<Variable, String>();

    public FactorGraph build(LTableAnnotation_JI_Freebase annotation, boolean relationLearning, String tableId) {
        FactorGraph graph = new FactorGraph();
        //cell text and entity label
        Map<String, Variable> cellAnnotations = factorBuilderCell.addFactors(annotation, graph,
                typeOfVariable);
        //column header and type label
        Map<Integer, Variable> columnHeaders = factorBuilderHeader.addFactors(annotation, graph,
                typeOfVariable);
        //column type and cell entities
        new FactorBuilderHeaderAndCell().addFactors(cellAnnotations,
                columnHeaders,
                annotation,
                graph,tableId);
        //relation and pair of column types
        if (relationLearning) {
            Map<String, Variable> relations = factorBuilderHeaderAndRelation.addFactors(
                    columnHeaders,
                    annotation,
                    graph,
                    typeOfVariable,tableId
            );

            //relation and entity pairs
            new FactorBuilderCellAndRelation().addFactors(
                    relations,
                    cellAnnotations,
                    annotation,
                    graph,
                    factorBuilderHeaderAndRelation.getRelationVarOutcomeDirection(),
                    tableId
            );
        }
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


}

