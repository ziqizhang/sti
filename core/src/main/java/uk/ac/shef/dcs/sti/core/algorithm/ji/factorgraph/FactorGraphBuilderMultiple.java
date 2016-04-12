package uk.ac.shef.dcs.sti.core.algorithm.ji.factorgraph;

import cc.mallet.grmm.types.FactorGraph;
import cc.mallet.grmm.types.Variable;
import uk.ac.shef.dcs.sti.core.algorithm.ji.TAnnotationJIFreebase;
import uk.ac.shef.dcs.sti.core.model.RelationColumns;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;

import java.util.*;

/**
 * Created by zqz on 15/05/2015.
 */
public class FactorGraphBuilderMultiple extends FactorGraphBuilder {
    public FactorGraphBuilderMultiple(boolean patchScores) {
        super(patchScores);
    }

    public List<FactorGraph> buildDisconnectedGraphs(TAnnotationJIFreebase annotation,
                                                     boolean relationLearning,
                                                     String tableId) {
        List<FactorGraph> out=new ArrayList<FactorGraph>();
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
            new FactorBuilderHeaderAndCell().addFactors(cellAnnotations,
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
                new FactorBuilderCellAndRelation().addFactors(
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

    private Map<String, Set<Integer>> computeDisconnectedTableColumns(TAnnotation annotation,
                                                                      boolean relationLearning) {
        Map<String, Set<Integer>> result = new HashMap<String, Set<Integer>>();
        int counter = 0;
        String key = null;
        if (relationLearning) {
            List<RelationColumns> relationColumnses = new ArrayList<RelationColumns>(
                    annotation.getColumncolumnRelations().keySet()
            );
            Collections.sort(relationColumnses, new Comparator<RelationColumns>() {
                @Override
                public int compare(RelationColumns o1, RelationColumns o2) {
                    int c= Integer.valueOf(o1.getSubjectCol()).compareTo(o2.getSubjectCol());
                    if(c==0)
                        return Integer.valueOf(o1.getObjectCol()).compareTo(o2.getObjectCol());
                    return c;
                }
            });
            for (RelationColumns rel : relationColumnses) {
                Set<Integer> components = findContainingGraph(result, rel.getSubjectCol(), rel.getObjectCol());
                if (components == null) {
                    components = new HashSet<Integer>();
                    key = "part" + counter;
                    counter++;
                }
                components.add(rel.getSubjectCol());
                components.add(rel.getObjectCol());

                result.put(key, components);
            }
            return result;
        } else {
            for (int c = 0; c < annotation.getCols(); c++) {
                if (annotation.getHeaderAnnotation(c).length != 0) {
                    Set<Integer> cols = new HashSet<Integer>();
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
