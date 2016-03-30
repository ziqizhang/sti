package uk.ac.shef.dcs.sti.algorithm.tm;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.freebase.FreebaseSearchResultFilter;
import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.rep.*;

import java.util.*;

/**
 */
public class BinaryRelationInterpreter {

    private RelationTextMatch_Scorer cellTextMatcher;
    private HeaderBinaryRelationScorer relation_scorer;

    public BinaryRelationInterpreter(
            RelationTextMatch_Scorer cellTextMatcher,
            HeaderBinaryRelationScorer scorer) {
        this.cellTextMatcher = cellTextMatcher;
        this.relation_scorer = scorer;
    }

    //returns the main subject column id
    //if -1, it means cannot score this table relations as no column can be considered main column for interpretation to work
    //so as a result of this method, the main subject column can change
    //
    //when new relation created, supporting row info is also added
    public int interpret(TAnnotation annotations, Table table, int sub_column) {

        //mainColumnIndexes contains indexes of columns that are possile NEs
        Map<Integer, DataTypeClassifier.DataType> colTypes
                = new HashMap<Integer, DataTypeClassifier.DataType>();
        for (int c = 0; c < table.getNumCols(); c++) {
            DataTypeClassifier.DataType type =
                    table.getColumnHeader(c).getTypes().get(0).getType();
            if (type.equals(DataTypeClassifier.DataType.ORDERED_NUMBER))
                continue; //ordered numbered columns are not interesting
            else
                colTypes.put(c, type);

        }

        //for each row, get the annotation for that (row, col)
        for (int row = 0; row < table.getNumRows(); row++) {
            //get annotation for this cell
            CellAnnotation[] candidate_annotations_for_cell =
                    annotations.getContentCellAnnotations(row, sub_column);
            if (candidate_annotations_for_cell == null || candidate_annotations_for_cell.length == 0)
                continue;
            CellAnnotation final_annotation = candidate_annotations_for_cell[0];


            //fetch facts of that entity
            /*if(final_annotation.getAnnotation().getId().equals("/m/0nlpl"))
                System.out.println();*/
            List<String[]> facts = /*candidateFinder.find_triplesForEntity(final_annotation.getAnnotation())*/
                    final_annotation.getAnnotation().getTriples();
            facts= FreebaseSearchResultFilter.filterRelations(facts);
            Map<Integer, String> values_to_match_on_the_row = new HashMap<Integer, String>();
            for (int col : colTypes.keySet()) {
                if (col != sub_column) {
                    String value_in_the_cell = table.getContentCell(row, col).getText();
                    values_to_match_on_the_row.put(col, value_in_the_cell);
                }
            }

            //perform matching  and scoring
            //key=col id; objobj contains the property that matched with the highest score (string, double)
            Map<Integer, List<Pair<String[], Double>>> matched_scores_for_the_row =
                    cellTextMatcher.match(facts, values_to_match_on_the_row, colTypes);

            for (Map.Entry<Integer, List<Pair<String[], Double>>> e : matched_scores_for_the_row.entrySet()) {
                Key_SubjectCol_ObjectCol subobj_key = new Key_SubjectCol_ObjectCol(sub_column, e.getKey());

                List<Pair<String[], Double>> matched_candidates = e.getValue();
                for (Pair<String[], Double> matched : matched_candidates) {
                    String annotation = matched.getKey()[0];
                    String annotation_label = ""; //todo:currently we do not get the label!!!
                    String[] matchedValue = new String[4];
                    matchedValue[0] = matched.getKey()[0]; //property name
                    matchedValue[1] = matched.getKey()[1]; //property value
                    matchedValue[2] = matched.getKey()[2];   //property id (if any)
                    matchedValue[3] = matched.getKey()[3]; //if property is direct

                    List<String[]> matchedValues = new ArrayList<String[]>();
                    matchedValues.add(matchedValue);
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
                prev_relation_annotations = relation_scorer.score(e.getValue(),
                        prev_relation_annotations,
                        key.getSubjectCol(), key.getObjectCol(),
                        table);
            }

            for (HeaderBinaryRelationAnnotation hbr : prev_relation_annotations) {
                relation_scorer.compute_final_score(hbr, table.getNumRows());
                for(Map.Entry<Integer, List<CellBinaryRelationAnnotation>> e: value.entrySet()){
                    for(CellBinaryRelationAnnotation cbr : e.getValue()){
                        if(hbr.getAnnotation_url().equals(cbr.getAnnotation_url())){
                            hbr.addSupportingRow(e.getKey());
                        }
                    }
                }
            }
            List<HeaderBinaryRelationAnnotation> sorted = new ArrayList<HeaderBinaryRelationAnnotation>(prev_relation_annotations);
            Collections.sort(sorted);
            for(HeaderBinaryRelationAnnotation hbr: sorted)
                annotations.addRelationAnnotation_across_column(hbr);

        }


        return annotations.getRelationAnnotations_per_row().size();

    }

}
