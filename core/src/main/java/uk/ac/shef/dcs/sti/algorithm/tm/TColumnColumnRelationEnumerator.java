package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.rep.Attribute;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.rep.*;

import java.util.*;

/**
 */
public class TColumnColumnRelationEnumerator {

    private TMPAttributeValueMatcher attributeValueMatcher;
    private RelationScorer relationScorer;

    public TColumnColumnRelationEnumerator(
            TMPAttributeValueMatcher attributeValueMatcher,
            RelationScorer scorer) {
        this.attributeValueMatcher = attributeValueMatcher;
        this.relationScorer = scorer;
    }

    /**
     * returns the number of columns that form relation with the subjectCol
     * <p>
     * when new relation created, supporting row info is also added
     */
    public int runRelationEnumeration(TAnnotation annotations, Table table, int subjectCol) {
        //select columns that are likely to form a relation with subject column
        Map<Integer, DataTypeClassifier.DataType> columnDataTypes
                = new HashMap<>();
        for (int c = 0; c < table.getNumCols(); c++) {
            DataTypeClassifier.DataType type =
                    table.getColumnHeader(c).getTypes().get(0).getType();
            if (type.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
                continue; //ordered numbered columns are not interesting
            else
                columnDataTypes.put(c, type);
        }

        //for each row, get the annotation for that (row, col)
        for (int row = 0; row < table.getNumRows(); row++) {
            //get the winning annotation for this cell
            List<TCellAnnotation> winningCellAnnotations = annotations.getWinningContentCellAnnotation(row, subjectCol);

            //collect attributes from where candidate relations are created
            List<Attribute> collectedAttributes = new ArrayList<>();
            for(TCellAnnotation cellAnnotation: winningCellAnnotations) {
                collectedAttributes.addAll(cellAnnotation.getAnnotation().getAttributes());
            }

            //collect cell values on the same row, from other columns
            Map<Integer, String> cellValuesToMatch = new HashMap<>();
            for (int col : columnDataTypes.keySet()) {
                if (col != subjectCol) {
                    String cellValue = table.getContentCell(row, col).getText();
                    cellValuesToMatch.put(col, cellValue);
                }
            }

            //perform matching  and scoring
            //key=col id; value: contains the attr that matched with the highest score against cell in that column
            Map<Integer, List<Pair<Attribute, Double>>> cellMatchScores =
                    attributeValueMatcher.match(collectedAttributes, cellValuesToMatch, columnDataTypes);

            for (Map.Entry<Integer, List<Pair<Attribute, Double>>> e : cellMatchScores.entrySet()) {
                RelationColumns subCol_to_objCol = new RelationColumns(subjectCol, e.getKey());

                List<Pair<Attribute, Double>> matchedAttributes = e.getValue();
                for (Pair<Attribute, Double> entry : matchedAttributes) {
                    String relationURI = entry.getKey().getRelationURI();
                    String relationLabel = ""; //todo:currently we do not get the label!!!
                    List<Attribute> matchedValues = new ArrayList<>();
                    matchedValues.add(entry.getKey());
                    TCellCellRelationAnotation cellcellRelation =
                            new TCellCellRelationAnotation(
                                    subCol_to_objCol, row, relationURI, relationLabel, matchedValues, entry.getValue()
                            );
                    annotations.addCellCellRelation(cellcellRelation);
                }
            }
        }

        //now we have created relation annotations per row, consolidate them to create column-column relation
        enumerateColumnColumnRelation(annotations, table);
        return annotations.getCellcellRelations().size();
    }

    private void enumerateColumnColumnRelation(TAnnotation annotations, Table table){
        for (Map.Entry<RelationColumns, Map<Integer, List<TCellCellRelationAnotation>>> entry :
                annotations.getCellcellRelations().entrySet()) {
            RelationColumns key = entry.getKey(); //relation's direction
            //map containing row and the cellcellrelations for that row
            Map<Integer, List<TCellCellRelationAnotation>> value = entry.getValue();

            //go through every row, update header binary relation scores
            List<TColumnColumnRelationAnnotation> columnColumnRelationAnnotations = new ArrayList<>();
            for (Map.Entry<Integer, List<TCellCellRelationAnotation>> e : value.entrySet()) {
                columnColumnRelationAnnotations = relationScorer.computeElementScores(e.getValue(),
                        columnColumnRelationAnnotations,
                        key.getSubjectCol(),
                        key.getObjectCol(),
                        table);
            }

            //now collect element scores to create final score, also generate supporting rows
            for (TColumnColumnRelationAnnotation relation : columnColumnRelationAnnotations) {
                relationScorer.computeFinal(relation, table.getNumRows());
                for (Map.Entry<Integer, List<TCellCellRelationAnotation>> e : value.entrySet()) {
                    for (TCellCellRelationAnotation cbr : e.getValue()) {
                        if (relation.getRelationURI().equals(cbr.getRelationURI())) {
                            relation.addSupportingRow(e.getKey());
                            break;
                        }
                    }
                }
            }
            List<TColumnColumnRelationAnnotation> sorted =
                    new ArrayList<>(columnColumnRelationAnnotations);
            Collections.sort(sorted);
            for (TColumnColumnRelationAnnotation hbr : sorted)
                annotations.addColumnColumnRelation(hbr);

        }
    }

}
