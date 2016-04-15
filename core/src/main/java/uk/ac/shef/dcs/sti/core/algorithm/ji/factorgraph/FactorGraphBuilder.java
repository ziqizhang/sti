package uk.ac.shef.dcs.sti.core.algorithm.ji.factorgraph;

import cc.mallet.grmm.types.*;
import cc.mallet.grmm.types.Variable;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.ji.TAnnotationJI;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;

import java.util.*;

/**
 * Created by zqz on 01/05/2015.
 */
public class FactorGraphBuilder {

    protected FactorBuilderCell factorBuilderCell = new FactorBuilderCell();
    protected FactorBuilderHeader factorBuilderHeader = new FactorBuilderHeader();
    protected FactorBuilderHeaderAndRelation factorBuilderHeaderAndRelation = new FactorBuilderHeaderAndRelation();
    protected FactorBuilderHeaderAndCell factorBuilderHeaderAndCell=new FactorBuilderHeaderAndCell();
    protected FactorBuilderCellAndRelation factorBuilderCellAndRelation = new FactorBuilderCellAndRelation();


    protected Map<Variable, String> typeOfVariable = new HashMap<>();

    /**
     * the table is firstly split into several maximum connected components (because it is possible
     * that some columns do not have relation with other columns, thus forming a disconnected factor graph)
     * then one graph is created for each of such maximum connected components
     * @param annotation
     * @param relationLearning
     * @param tableId
     * @return
     * @throws STIException
     */
    public List<FactorGraph> build(TAnnotationJI annotation, boolean relationLearning, String tableId) throws STIException{
        List<FactorGraph> out=new ArrayList<>();
        Map<String, Set<Integer>> subGraphs=computeDisconnectedTableColumns(annotation, relationLearning);
        for(Map.Entry<String, Set<Integer>> ent: subGraphs.entrySet()) {
            Set<Integer> columns=ent.getValue();
            FactorGraph graph = new FactorGraph();
            //cell text and entity label
            Map<String, Variable> cellAnnotations = factorBuilderCell.addFactors(annotation, graph,
                    typeOfVariable, columns);
            //column header and type label
            Map<Integer, Variable> columnHeaders = factorBuilderHeader.addFactors(annotation, graph,
                    typeOfVariable, columns);
            //column type and cell entities
            factorBuilderHeaderAndCell.addFactors(cellAnnotations,
                    columnHeaders,
                    annotation,
                    graph, tableId,
                    columns);
            //relation and pair of column types
            if (relationLearning) {
                Map<String, Variable> relations = factorBuilderHeaderAndRelation.addFactors(
                        columnHeaders,
                        annotation,
                        graph,
                        typeOfVariable, tableId, columns
                );

                //relation and entity pairs
                factorBuilderCellAndRelation.addFactors(
                        relations,
                        cellAnnotations,
                        annotation,
                        graph,
                        factorBuilderHeaderAndRelation.getRelationVarOutcomeDirection(), tableId, columns
                );
            }

            out.add(graph);
        }

       return out;
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

    public RelationColumns getRelationDirection(String varOutcomeLabel) {
        return factorBuilderHeaderAndRelation.relationVarOutcomeDirection.get(varOutcomeLabel);
    }


    private Map<String, Set<Integer>> computeDisconnectedTableColumns(TAnnotation annotation,
                                                                      boolean relationLearning) {
        Map<String, Set<Integer>> result = new HashMap<>();
        int counter = 0;
        String key = null;
        if (relationLearning) {
            List<RelationColumns> relationColumns = new ArrayList<>(
                    annotation.getColumncolumnRelations().keySet()
            );
            if(relationColumns.size()>0) {
                Collections.sort(relationColumns, (o1, o2) -> {
                    int c = Integer.valueOf(o1.getSubjectCol()).compareTo(o2.getSubjectCol());
                    if (c == 0)
                        return Integer.valueOf(o1.getObjectCol()).compareTo(o2.getObjectCol());
                    return c;
                });
                for (RelationColumns rel : relationColumns) {
                    Set<Integer> components = findContainingGraph(result, rel.getSubjectCol(), rel.getObjectCol());
                    if (components == null) {
                        components = new HashSet<>();
                        key = "part" + counter;
                        counter++;
                    }
                    components.add(rel.getSubjectCol());
                    components.add(rel.getObjectCol());

                    result.put(key, components);
                }
            }else{//no relation, must be a single column
                for (int c = 0; c < annotation.getCols(); c++) {
                    if (annotation.getHeaderAnnotation(c).length != 0) {
                        Set<Integer> cols = new HashSet<>();
                        cols.add(c);
                        result.put(String.valueOf(c), cols);
                    }
                }
            }
            return result;
        } else {
            for (int c = 0; c < annotation.getCols(); c++) {
                if (annotation.getHeaderAnnotation(c).length != 0) {
                    Set<Integer> cols = new HashSet<>();
                    cols.add(c);
                    result.put(String.valueOf(c), cols);
                }
            }
            return result;
        }
    }

    private Set<Integer> findContainingGraph(Map<String, Set<Integer>> parts, int col1, int col2) {
        for (Set<Integer> values : parts.values()) {
            if (values.contains(col1) || values.contains(col2))
                return values;
        }
        return null;
    }
}

