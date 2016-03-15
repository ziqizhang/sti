package uk.ac.shef.dcs.oak.sti.algorithm.baseline;

import javafx.util.Pair;
import uk.ac.shef.dcs.oak.sti.algorithm.tm.RelationTextMatch_Scorer;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseFreebaseFilter;
import uk.ac.shef.dcs.oak.sti.rep.*;

import java.util.*;

/**
scorer simply counts # of rows that uses the relation
 */
public class Baseline_BinaryRelationInterpreter {
    private RelationTextMatch_Scorer cellTextMatcher;

    public Baseline_BinaryRelationInterpreter(
            RelationTextMatch_Scorer cellTextMatcher) {
        this.cellTextMatcher = cellTextMatcher;
    }

    //returns the main subject column id
    //if -1, it means cannot interpret this table relations as no column can be considered main column for interpretation to work
    //so as a result of this method, the main subject column can change
    //
    //when new relation created, supporting row info is also added
    public int interpret(LTableAnnotation annotations, LTable table, int sub_column) {

        //mainColumnIndexes contains indexes of columns that are possile NEs
        Map<Integer, DataTypeClassifier.DataType> colTypes
                = new HashMap<Integer, DataTypeClassifier.DataType>();
        for (int c = 0; c < table.getNumCols(); c++) {
            DataTypeClassifier.DataType type =
                    table.getColumnHeader(c).getTypes().get(0).getCandidateType();
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
            List<String[]> facts = /*candidateFinder.find_triplesForEntity(final_annotation.getAnnotation())*/final_annotation.getAnnotation().getTriples();
            facts = KnowledgeBaseFreebaseFilter.filterRelations(facts);
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
                    String[] matchedValue = new String[3];
                    matchedValue[0] = matched.getKey()[0]; //property name
                    matchedValue[1] = matched.getKey()[1]; //property value
                    matchedValue[2] = matched.getKey()[2];   //property id (if any)

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

            Map<String, Double> scores = new HashMap<String, Double>();
            for (Map.Entry<Integer, List<CellBinaryRelationAnnotation>> e : value.entrySet()) {
                List<CellBinaryRelationAnnotation> candidates = e.getValue();
                for(CellBinaryRelationAnnotation cbr: candidates){
                    Double score = scores.get(cbr.getAnnotation_url());
                    score=score==null?0.0:score;
                    score+=1.0;
                    scores.put(cbr.getAnnotation_url(), score);
                }
            }


            List<HeaderBinaryRelationAnnotation> sorted = new ArrayList<HeaderBinaryRelationAnnotation>();
            for(Map.Entry<String, Double> e: scores.entrySet()){
                String url = e.getKey();
                Double score =e.getValue();
                HeaderBinaryRelationAnnotation hbr = new HeaderBinaryRelationAnnotation(
                        key,
                        url, "",score
                );
                sorted.add(hbr);
            }
            Collections.sort(sorted);
            for(HeaderBinaryRelationAnnotation hbr: sorted)
                annotations.addRelationAnnotation_across_column(hbr);

        }


        return annotations.getRelationAnnotations_per_row().size();

    }


}
