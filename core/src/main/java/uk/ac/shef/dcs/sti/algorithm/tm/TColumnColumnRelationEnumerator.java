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
     * returns the main subject column id
     * if -1, it means cannot interpret this table relations as no column can be considered main column for interpretation to work
     * note that this method can change the subject column
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
            //key=col id; objobj contains the property that matched with the highest computeElementScores (string, double)
            Map<Integer, List<Pair<Attribute, Double>>> cellMatchScores =
                    attributeValueMatcher.match(collectedAttributes, cellValuesToMatch, columnDataTypes);

            todo continue from here...
            for (Map.Entry<Integer, List<Pair<Attribute, Double>>> e : cellMatchScores.entrySet()) {
                Key_SubjectCol_ObjectCol subobj_key = new Key_SubjectCol_ObjectCol(subjectCol, e.getKey());

                List<Pair<Attribute, Double>> matched_candidates = e.getValue();
                for (Pair<Attribute, Double> matched : matched_candidates) {
                    String annotation = matched.getKey().getRelation();
                    String annotation_label = ""; //todo:currently we do not get the label!!!
                    /*String[] matchedValue = new String[4];
                    matchedValue[0] = matched.getKey()[0]; //property name
                    matchedValue[1] = matched.getKey()[1]; //property value
                    matchedValue[2] = matched.getKey()[2];   //property id (if any)
                    matchedValue[3] = matched.getKey()[3]; //if property is direct*/

                    List<Attribute> matchedValues = new ArrayList<>();
                    matchedValues.add(matched.getKey());
                    CellBinaryRelationAnnotation relationAnnotation =
                            new CellBinaryRelationAnnotation(
                                    subobj_key, row, annotation, annotation_label, matchedValues, matched.getValue()
                            );
                    annotations.addRelationAnnotation_per_row(relationAnnotation);
                }
            }
        }

        //now we have created relation annotations per row, consolidate for the column-pair
        for (Map.Entry<Key_SubjectCol_ObjectCol, Map<Integer, List<CellBinaryRelationAnnotation>>> entry :
                annotations.getRelationAnnotations_per_row().entrySet()) {
            //for every sub=obj pair
            Key_SubjectCol_ObjectCol key = entry.getKey();
            Map<Integer, List<CellBinaryRelationAnnotation>> value = entry.getValue();

            //Map<String, HeaderBinaryRelationAnnotation> consolidated_across_column_relation_annotations = new HashMap<String, HeaderBinaryRelationAnnotation>();
            //go through every row, update header binary relation scores

            Set<HeaderBinaryRelationAnnotation> prev_relation_annotations = new HashSet<HeaderBinaryRelationAnnotation>();

            for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> e : value.entrySet()) {
                prev_relation_annotations = relationScorer.score(e.getValue(),
                        prev_relation_annotations,
                        key.getSubjectCol(), key.getObjectCol(),
                        table);
            }

            for (HeaderBinaryRelationAnnotation hbr : prev_relation_annotations) {
                relationScorer.compute_final_score(hbr, table.getNumRows());
                for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> e : value.entrySet()) {
                    for (CellBinaryRelationAnnotation cbr : e.getValue()) {
                        if (hbr.getAnnotation_url().equals(cbr.getAnnotation_url())) {
                            hbr.addSupportingRow(e.getKey());
                        }
                    }
                }
            }
            List<HeaderBinaryRelationAnnotation> sorted = new ArrayList<HeaderBinaryRelationAnnotation>(prev_relation_annotations);
            Collections.sort(sorted);
            for (HeaderBinaryRelationAnnotation hbr : sorted)
                annotations.addRelationAnnotation_across_column(hbr);

        }


        return annotations.getRelationAnnotations_per_row().size();

    }

}
