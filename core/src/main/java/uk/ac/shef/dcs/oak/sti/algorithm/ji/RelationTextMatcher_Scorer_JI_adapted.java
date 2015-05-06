package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import javafx.scene.control.Cell;
import uk.ac.shef.dcs.oak.sti.algorithm.smp.RelationTextMatch_Scorer;
import uk.ac.shef.dcs.oak.sti.kb.KBSearcher;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.sti.misc.UtilRelationMatcher;
import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.util.ObjObj;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.io.IOException;
import java.util.*;

/**
 * Created by zqz on 05/05/2015.
 */
public class RelationTextMatcher_Scorer_JI_adapted extends RelationTextMatch_Scorer {

    public RelationTextMatcher_Scorer_JI_adapted(Collection<String> stopWords, AbstractStringMetric stringSimilarityMetric, double minimum_match_score) {
        super(stopWords, stringSimilarityMetric, minimum_match_score);
    }

    public void match_cellPairs(int row,
                                List<CellAnnotation> subjectCellAnnotations, int subjectColumn,
                                List<CellAnnotation> objectCellAnnotations, int objectColumn,
                                LTableContentCell objectCellText,
                                DataTypeClassifier.DataType object_column_type,
                                LTableAnnotation_JI_Freebase tableAnnotation
    ) {
        if (subjectCellAnnotations.size() != 0) {
            if (subjectCellAnnotations.size() > 0 && UtilRelationMatcher.isValidType(object_column_type)) {
                for (int s = 0; s < subjectCellAnnotations.size(); s++) { //for each candidate subject entity
                    CellAnnotation sbjEntity = subjectCellAnnotations.get(s);
                    List<String[]> sbjEntityFacts = sbjEntity.getAnnotation().getFacts(); //get the facts of that sbj ent
                    Map<Integer, DataTypeClassifier.DataType> fact_data_types = classifyFactObjDataType(
                            sbjEntityFacts
                    );

                    String objText = objectCellText.getText();
                    final Map<Integer, Double> factIdx_matchedScores = new HashMap<Integer, Double>();
                    //key - index of fact; value- list of candidate entity from the obj cell matched the fact, or null if no candidate entities
                    Map<Integer, List<CellAnnotation>> factIdx_matchedObjCellCandidates=new HashMap<Integer, List<CellAnnotation>>();
                    //now go thru each fact
                    for (int index = 0; index < sbjEntityFacts.size(); index++) {
                        DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
                        String[] fact = sbjEntityFacts.get(index);
                        if (!UtilRelationMatcher.isValidType(type_of_fact_value)) {
                            continue;
                        }
                        //match the object cell text
                        double score = UtilRelationMatcher.score(objText, object_column_type, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                        //this fact may match multiple candidate NEs for the object cell
                        Map<Double, List<CellAnnotation>> mctScore_objCellCandidates = new HashMap<Double, List<CellAnnotation>>();
                        for (int o = 0; o < objectCellAnnotations.size(); o++) {
                            CellAnnotation objectEntity = objectCellAnnotations.get(o);
                            double scoreAgainstObjEntityLabel = UtilRelationMatcher.score(
                                    objectEntity.getAnnotation().getName(), object_column_type, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                            List<CellAnnotation> candidates = mctScore_objCellCandidates.get(scoreAgainstObjEntityLabel);
                            if (candidates == null) candidates = new ArrayList<CellAnnotation>();
                            candidates.add(objectEntity);
                            mctScore_objCellCandidates.put(scoreAgainstObjEntityLabel, candidates);
                            if (score < scoreAgainstObjEntityLabel)
                                score = scoreAgainstObjEntityLabel;
                        }
                        if (score > 0 && score > minimum_match_score) {
                            factIdx_matchedScores.put(index, score);
                            factIdx_matchedObjCellCandidates.put(index, mctScore_objCellCandidates.get(score));
                        }
                    }

                    if (factIdx_matchedScores.size() == 0) continue;
                    //go thru all scores and make selection   within each subjectEntity-objectEntity pair
                    List<Integer> qualified = new ArrayList<Integer>(factIdx_matchedScores.keySet());
                    Collections.sort(qualified, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer o1, Integer o2) {
                            return factIdx_matchedScores.get(o2).compareTo(factIdx_matchedScores.get(o1));
                        }
                    });
                    Double highestScore = factIdx_matchedScores.get(qualified.get(0));
                    for (Map.Entry<Integer, Double> e : factIdx_matchedScores.entrySet()) {
                        int index = e.getKey();
                        Double score = e.getValue();
                        if (score.equals(highestScore)) {
                            String[] fact = sbjEntityFacts.get(index);
                            createCandidateAnnotation(tableAnnotation,
                                    row,subjectColumn, objectColumn,
                                    fact,score, sbjEntity,
                                    factIdx_matchedObjCellCandidates.get(index));
                        }
                    }
                }// each subjectNE-objectNE pair
            }//each subjectNE
        }//if block checking whether the potential subject-object cell pairs are valid

    }

    public void match_headerPairs(List<HeaderAnnotation> subjectHeaderColumnCandidates,
                                  int col1,
                                  List<HeaderAnnotation> objectHeaderColumnCandidates,
                                  int col2,
                                  LTableColumnHeader subjectColumnHeader,
                                  LTableColumnHeader objectColumnHeader,
                                  DataTypeClassifier.DataType objectColumnDataType,
                                  LTableAnnotation_JI_Freebase annotation,
                                  KBSearcher kbSearcher) throws IOException {
        if (subjectHeaderColumnCandidates.size() != 0) {
            if (subjectHeaderColumnCandidates.size() > 0 && UtilRelationMatcher.isValidType(objectColumnDataType)) {
                for (int s = 0; s < subjectHeaderColumnCandidates.size(); s++) {
                    HeaderAnnotation sbjCandidates = subjectHeaderColumnCandidates.get(s);
                    List<String[]> sbjCandidateFacts = kbSearcher.find_triplesForEntity(sbjCandidates.getAnnotation_url());
                    Map<Integer, DataTypeClassifier.DataType> fact_data_types = classifyFactObjDataType(
                            sbjCandidateFacts
                    );

                    String objHeaderText = objectColumnHeader.getHeaderText();
                    //scoring matches for the cell on the row
                    final Map<Integer, Double> fact_matched_scores = new HashMap<Integer, Double>();
                    final Map<Integer, List<String>>
                            fact_matched_objectHeaderCandidateURL = new HashMap<Integer, List<String>>();
                    for (int index = 0; index < sbjCandidateFacts.size(); index++) {
                        DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
                        String[] fact = sbjCandidateFacts.get(index);
                        if (!UtilRelationMatcher.isValidType(type_of_fact_value)) {
                            continue;
                        }

                        double scoreWithHeader = UtilRelationMatcher.score(objHeaderText,
                                objectColumnDataType, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                        Map<Double, List<String>> matchingScore_candidateConcept = new HashMap<Double, List<String>>();
                        for (int o = 0; o < objectHeaderColumnCandidates.size(); o++) {
                            String objHeaderConceptLabel = objectHeaderColumnCandidates.get(o).getAnnotation_label();
                            if (objHeaderConceptLabel != null) {
                                double scoreAgainstObjHeaderConceptLabel = UtilRelationMatcher.score(objHeaderConceptLabel, objectColumnDataType, fact[1], type_of_fact_value, stopWords, stringSimilarityMetric);
                                List<String> candidates = matchingScore_candidateConcept.get(scoreAgainstObjHeaderConceptLabel);
                                if(candidates==null) candidates=new ArrayList<String>();
                                candidates.add(objHeaderConceptLabel);
                                matchingScore_candidateConcept.put(scoreAgainstObjHeaderConceptLabel, candidates);
                                if (scoreWithHeader < scoreAgainstObjHeaderConceptLabel) {
                                    scoreWithHeader = scoreAgainstObjHeaderConceptLabel;
                                }
                            }
                        }
                        if (scoreWithHeader > 0 && scoreWithHeader > minimum_match_score) {
                            fact_matched_scores.put(index, scoreWithHeader);
                            fact_matched_objectHeaderCandidateURL.
                                    put(index, matchingScore_candidateConcept.get(scoreWithHeader));
                        }
                    }

                    if (fact_matched_scores.size() == 0) continue;
                    //go thru all scores and make selection   within each subjectEntity-objectEntity pair
                    List<Integer> qualified = new ArrayList<Integer>(fact_matched_scores.keySet());
                    Collections.sort(qualified, new Comparator<Integer>() {
                        @Override
                        public int compare(Integer o1, Integer o2) {
                            return fact_matched_scores.get(o2).compareTo(fact_matched_scores.get(o1));
                        }
                    });
                    Double highestScore = fact_matched_scores.get(qualified.get(0));
                    for (Map.Entry<Integer, Double> e : fact_matched_scores.entrySet()) {
                        int index = e.getKey();
                        Double score = e.getValue();
                        if (score.equals(highestScore)) {
                            String[] fact = sbjCandidateFacts.get(index);
                            String relation = fact[0];
                            String subjectConcept = sbjCandidates.getAnnotation_url();
                            List<String> objectConcepts = fact_matched_objectHeaderCandidateURL.get(index);
                            if (objectConcepts != null) {
                                for(String oc: objectConcepts){
                                    annotation.setScore_conceptAndRelation_conceptEvidence(oc, relation, 1.0);
                                }
                            }
                            annotation.setScore_conceptAndRelation_conceptEvidence(
                                    subjectConcept, relation, 1.0
                            );

                            List<HeaderBinaryRelationAnnotation> candidateRelations=
                                    annotation.getRelationAnnotations_across_columns().get(
                                    new Key_SubjectCol_ObjectCol(col1, col2)
                            );
                            boolean contains=false;
                            for(HeaderBinaryRelationAnnotation hbr: candidateRelations){
                                if(hbr.equals(relation)){
                                    contains=true;
                                    break;
                                }
                            }
                            if(!contains)
                                candidateRelations.add(new HeaderBinaryRelationAnnotation(
                                        new Key_SubjectCol_ObjectCol(col1, col2),relation,relation,0.0
                                ));
                        }
                    }
                }//each subjectNE
            }//if block checking whether the potential subject-object cell pairs are valid
        }
    }

    private void createCandidateAnnotation(LTableAnnotation_JI_Freebase tableAnnotation,
                                           int row, int subjectColumn, int objectColumn,
                                           String[] fact,
                                           double score,
                                           CellAnnotation sbjEntity,
                                           List<CellAnnotation> matchedObjCellCandidates){
        tableAnnotation.addRelationAnnotation_per_row(new CellBinaryRelationAnnotation(
                new Key_SubjectCol_ObjectCol(subjectColumn, objectColumn), row, fact[0], fact[0],
                new ArrayList<String[]>(), score
        ));
        //subject entity, its concepts and relation
        populateEntityAndRelationScore(tableAnnotation, sbjEntity.getAnnotation().getId(), fact[0], score);
        populateConceptAndRelationScore(tableAnnotation, sbjEntity,
                row, subjectColumn,
                fact[0], score);
        //object entity (if any), its concepts and relation
        if (matchedObjCellCandidates!=null) {
            for (CellAnnotation ca : matchedObjCellCandidates) {
                populateEntityAndRelationScore(tableAnnotation, ca.getAnnotation().getId(),
                        fact[0], score);
                populateConceptAndRelationScore(tableAnnotation, ca, row, objectColumn,
                        fact[0],score);
            }
        }
    }

    private void populateConceptAndRelationScore(LTableAnnotation_JI_Freebase tableAnnotation,
                                                 CellAnnotation entity,
                                                 int entityRow, int entityColumn,
                                                 String relationURL,
                                                 double maxScore) {
        for (String[] type : entity.getAnnotation().getTypes()) {
            if(!KB_InstanceFilter.ignoreType(type[0], type[1]))
                tableAnnotation.setScore_conceptAndRelation_instanceEvidence(entityRow,
                    entityColumn, type[0], relationURL, maxScore);
        }
    }

    private void populateEntityAndRelationScore(LTableAnnotation_JI_Freebase tableAnnotation,
                                                String entityId, String relationURL, double maxScore) {
        tableAnnotation.setScore_entityAndRelation(entityId, relationURL, maxScore);
    }
}
