package uk.ac.shef.dcs.sti.core.algorithm.tmp;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.core.scorer.RelationScorer;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.extension.annotations.EntityCandidate;
import uk.ac.shef.dcs.sti.core.extension.constraints.ColumnRelation;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.*;

import java.util.*;
import java.util.List;

/**
 */
public class TColumnColumnRelationEnumerator {

    private AttributeValueMatcher attributeValueMatcher;
    private RelationScorer relationScorer;

    private int suggestedRelationPositionsVisited;

    public TColumnColumnRelationEnumerator(
            AttributeValueMatcher attributeValueMatcher,
            RelationScorer scorer) {
        this.attributeValueMatcher = attributeValueMatcher;
        this.relationScorer = scorer;
    }

    public RelationScorer getRelationScorer(){
        return relationScorer;
    }

    public int runRelationEnumeration(TAnnotation annotations, Table table, int subjectCol,
                                      Constraints constraints) throws STIException {
        suggestedRelationPositionsVisited = 0;
        generateCellCellRelations(annotations, table, subjectCol, constraints);
        //now we have created relation annotations per row, consolidate them to create column-column relation
        enumerateColumnColumnRelation(annotations, table, constraints);
        return annotations.getCellcellRelations().size() + suggestedRelationPositionsVisited;
    }

    /**
     * returns the number of columns that form relation with the subjectCol
     * <p>
     * when new relation created, supporting row info is also added
     */
    protected void generateCellCellRelations(TAnnotation annotations, Table table, int subjectCol) throws STIException {
      generateCellCellRelations(annotations, table, subjectCol, new Constraints());
    }

    /**
     * returns the number of columns that form relation with the subjectCol
     * <p>
     * when new relation created, supporting row info is also added
     */
    private void generateCellCellRelations(TAnnotation annotations, Table table, int subjectCol,
                                           Constraints constraints) throws STIException {
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
            for (TCellAnnotation cellAnnotation : winningCellAnnotations) {
                collectedAttributes.addAll(cellAnnotation.getAnnotation().getAttributes());
            }

            //collect cell values on the same row, from other columns
            Map<Integer, String> cellValuesToMatch = new HashMap<>();
            for (int col : columnDataTypes.keySet()) {
                if (col != subjectCol && !isRelationSuggested(subjectCol, col, constraints.getColumnRelations())) {
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
    }

    private void enumerateColumnColumnRelation(TAnnotation annotations, Table table,
                                               Constraints constraints) throws STIException {
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

        // (added): set relations suggested by the user
        for (ColumnRelation relation : constraints.getColumnRelations()) {
          for (EntityCandidate suggestion : relation.getAnnotation().getChosen()) {
            annotations.addColumnColumnRelation(new TColumnColumnRelationAnnotation(
                new RelationColumns(relation.getPosition().getFirstIndex(), relation.getPosition().getSecondIndex()),
                suggestion.getEntity().getResource(), suggestion.getEntity().getLabel(), suggestion.getScore().getValue()));
          }
        }
    }

    private boolean isRelationSuggested(int subjectCol, int objectCol, Set<ColumnRelation> columnRelations) {
      for (ColumnRelation relation : columnRelations) {
        if (relation.getPosition().getFirstIndex() == subjectCol &&
            relation.getPosition().getSecondIndex() == objectCol &&
            !relation.getAnnotation().getChosen().isEmpty()) {
          suggestedRelationPositionsVisited++;
          return true;
        }
      }
      return false;
    }

}
