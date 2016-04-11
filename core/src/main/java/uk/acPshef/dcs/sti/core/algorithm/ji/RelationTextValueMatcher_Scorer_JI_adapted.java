package uk.ac.shef.dcs.sti.core.algorithm.ji;

import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.sti.core.model.*;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.util.*;
import java.util.List;

/**
 * Created by zqz on 05/05/2015.
 */
public class RelationTextValueMatcher_Scorer_JI_adapted extends AttributeValueMatcher {

    public RelationTextValueMatcher_Scorer_JI_adapted(double minScoreThreshold, List<String> stopWords,
                                                      AbstractStringMetric stringMetric) {
        super(minScoreThreshold, stopWords,stringMetric);
    }

    public void match_cellPairs(int row,
                                List<TCellAnnotation> subjectCellAnnotations, int subjectColumn,
                                List<TCellAnnotation> objectCellAnnotations, int objectColumn,
                                DataTypeClassifier.DataType object_column_type,
                                TAnnotation_JI_Freebase tableAnnotation
    ) {
        if (subjectCellAnnotations.size() != 0 && objectCellAnnotations.size() != 0) {
            for (int s = 0; s < subjectCellAnnotations.size(); s++) { //for each candidate subject entity
                TCellAnnotation sbjEntity = subjectCellAnnotations.get(s);
                List<Attribute> sbjEntityFacts = sbjEntity.getAnnotation().getAttributes(); //get the facts of that sbj ent
                Map<Integer, DataTypeClassifier.DataType> fact_data_types = classifyAttributeValueDataType(
                        sbjEntityFacts
                );

                final Map<Integer, Double> factIdx_matchedScores = new HashMap<Integer, Double>();
                //key - index of fact; value- list of candidate entity from the obj cell matched the fact, or null if no candidate entities
                Map<Integer, List<TCellAnnotation>> factIdx_matchedObjCellCandidates = new HashMap<Integer, List<TCellAnnotation>>();
                //now go thru each fact
                for (int index = 0; index < sbjEntityFacts.size(); index++) {
                    DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
                    Attribute fact = sbjEntityFacts.get(index);
                    if (!isValidType(type_of_fact_value)) {
                        continue;
                    }
                    double maxScore = 0.0; //maximum computeElementScores between this fact's obj and any candidate entity in the obj cell
                    //this fact may computeElementScores multiple candidate NEs for the object cell
                    Map<Double, List<TCellAnnotation>> mctScore_objCellCandidates = new HashMap<Double, List<TCellAnnotation>>();
                    for (int o = 0; o < objectCellAnnotations.size(); o++) {
                        TCellAnnotation objectEntity = objectCellAnnotations.get(o);
                        double scoreAgainstObjEntityLabel = score(
                                objectEntity.getAnnotation().getLabel(), object_column_type, fact.getValue(), type_of_fact_value, stopWords);
                        double scoreAgainstObjEntityId = 0.0;
                        if (fact.getValueURI() != null)
                            scoreAgainstObjEntityId = objectEntity.getAnnotation().getId().equals(fact.getValueURI()) ? 1.0 : 0.0;

                        double finalScore = scoreAgainstObjEntityId > scoreAgainstObjEntityLabel ? scoreAgainstObjEntityId :
                                scoreAgainstObjEntityLabel;

                        List<TCellAnnotation> candidates = mctScore_objCellCandidates.get(finalScore);
                        if (candidates == null) candidates = new ArrayList<TCellAnnotation>();
                        candidates.add(objectEntity);
                        mctScore_objCellCandidates.put(finalScore, candidates);
                        if (maxScore < finalScore)
                            maxScore = finalScore;
                    }
                    if (maxScore > 0 && maxScore > minScoreThreshold) {
                        factIdx_matchedScores.put(index, maxScore);
                        factIdx_matchedObjCellCandidates.put(index, mctScore_objCellCandidates.get(maxScore));
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
                    List<TCellAnnotation> objEntities = factIdx_matchedObjCellCandidates.get(index);
                    if (score.equals(highestScore) && objEntities != null && objEntities.size() > 0) {
                        Attribute fact = sbjEntityFacts.get(index);
                        createCandidateAnnotation(tableAnnotation,
                                row, subjectColumn, objectColumn,
                                fact, score, sbjEntity, objEntities);
                    }
                }
            }// each subjectNE-objectNE pair
        }//each subjectNE
    }//if block checking whether the potential subject-object cell pairs are valid


    public void match_headerPairs(List<TColumnHeaderAnnotation> subjectHeaderColumnCandidates,
                                  int sbjCol,
                                  List<TColumnHeaderAnnotation> objectHeaderColumnCandidates,
                                  int objCol,
                                  DataTypeClassifier.DataType objectColumnDataType,
                                  TAnnotation_JI_Freebase annotation,
                                  KBSearch kbSearch) throws KBSearchException {
        if (subjectHeaderColumnCandidates.size() > 0 && objectHeaderColumnCandidates.size() > 0) {
            for (int s = 0; s < subjectHeaderColumnCandidates.size(); s++) {
                TColumnHeaderAnnotation sbjCandidates = subjectHeaderColumnCandidates.get(s);
                List<Attribute> sbjCandidateFacts = kbSearch.findAttributesOfClazz(sbjCandidates.getAnnotation().getId());
                Map<Integer, DataTypeClassifier.DataType> factObjDataTypes = classifyAttributeValueDataType(
                        sbjCandidateFacts
                );
                final Map<Integer, Double> factIdx_matchedScores = new HashMap<Integer, Double>();
                final Map<Integer, List<String>>
                        factIdx_matchedObjHeaderCandidates = new HashMap<Integer, List<String>>();
                scoreAgainstSbjFacts(
                        objectColumnDataType, objectHeaderColumnCandidates,
                        sbjCandidateFacts, factObjDataTypes, factIdx_matchedScores, factIdx_matchedObjHeaderCandidates);


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
                        createCandidateAnnotation(sbjCandidateFacts.get(index),
                                sbjCandidates, factIdx_matchedObjHeaderCandidates.get(index),
                                annotation, sbjCol, objCol
                        );
                    }
                }
            }//each subjectNE
        }//if block checking whether the potential subject-object cell pairs are valid

    }

    private void createCandidateAnnotation(TAnnotation_JI_Freebase tableAnnotation,
                                           int row, int subjectColumn, int objectColumn,
                                           Attribute fact,
                                           double score,
                                           TCellAnnotation sbjEntity,
                                           List<TCellAnnotation> matchedObjCellCandidates) {
        tableAnnotation.addCellCellRelation(new TCellCellRelationAnotation(
                new RelationColumns(subjectColumn, objectColumn), row, fact.getRelationURI(), fact.getRelationURI(),
                new ArrayList<>(), score
        ));
        //subject entity, its concepts and relation
        populateEntityPairAndRelationScore(tableAnnotation, sbjEntity.getAnnotation().getId(),
                fact.getRelationURI(), matchedObjCellCandidates, subjectColumn, objectColumn);
        populateConceptPairAndRelationScore_instanceEvidence(tableAnnotation, sbjEntity,
                row,
                fact.getRelationURI(), matchedObjCellCandidates, subjectColumn, objectColumn, score);
    }

    private void populateConceptPairAndRelationScore_instanceEvidence(TAnnotation_JI_Freebase tableAnnotation,
                                                                      TCellAnnotation sbjEntity,
                                                                      int entityRow,
                                                                      String relationURL,
                                                                      List<TCellAnnotation> matchedObjCellCandidates,
                                                                      int relationFrom, int relationTo,
                                                                      double maxScore) {
        //todo: false relation added to highly general types (person, religious_leader_title), maybe use only most specific type of sbj, obj
        for (Clazz sbjType : sbjEntity.getAnnotation().getTypes()) {
            for (TCellAnnotation objEntity : matchedObjCellCandidates) {
                for (Clazz objType : objEntity.getAnnotation().getTypes()) {
                    if (sbjType.getId().equals(objType.getId())) continue;
                    tableAnnotation.setScore_conceptPairAndRelation_instanceEvidence(entityRow,
                            sbjType.getId(),
                            TColumnColumnRelationAnnotation.toStringExpanded(relationFrom, relationTo, relationURL),
                            objType.getId(),
                            maxScore);
                }
            }
        }
    }

    private void populateEntityPairAndRelationScore(TAnnotation_JI_Freebase tableAnnotation,
                                                    String entityId, String relationURL, List<TCellAnnotation> objEntities,
                                                    int relationFrom, int relationTo
    ) {
        for (TCellAnnotation objEntity : objEntities)
            tableAnnotation.setScore_entityPairAndRelation(entityId,
                    objEntity.getAnnotation().getId(),
                    TColumnColumnRelationAnnotation.toStringExpanded(
                            relationFrom, relationTo, relationURL), 1.0);
    }

    private void scoreAgainstSbjFacts(
            DataTypeClassifier.DataType objectColumnDataType,
            List<TColumnHeaderAnnotation> objectHeaderColumnCandidates,
            List<Attribute> sbjCandidateFacts,
            Map<Integer, DataTypeClassifier.DataType> fact_data_types,
            Map<Integer, Double> factIdx_matchedScores,
            Map<Integer, List<String>>
                    factIdx_matchedObjHeaderCandidates
    ) {
        //scoring matches for the cell on the row
        for (int index = 0; index < sbjCandidateFacts.size(); index++) {
            DataTypeClassifier.DataType type_of_fact_value = fact_data_types.get(index);
            Attribute fact = sbjCandidateFacts.get(index);
            if (!isValidType(type_of_fact_value)) {
                continue;
            }
            //use only the fact's obj (text) to compare against the header's text
            double maxScore = 0.0;
            Map<Double, List<String>> mchScore_objHeaderCandidates = new HashMap<Double, List<String>>();
            for (int o = 0; o < objectHeaderColumnCandidates.size(); o++) {
                String objHeaderConceptURL = objectHeaderColumnCandidates.get(o).getAnnotation().getId();
                String objHeaderConceptLabel = objectHeaderColumnCandidates.get(o).getAnnotation().getLabel();

                if (objHeaderConceptURL != null) {
                    double scoreAgainstObjHeaderConcept =
                            score(objHeaderConceptLabel, objectColumnDataType, fact.getValue(), type_of_fact_value, stopWords);
                    if (fact.getValueURI() != null) {
                        double score = objHeaderConceptURL.equals(fact.getValueURI()) ? 1.0 : 0.0;
                        if (score > scoreAgainstObjHeaderConcept) scoreAgainstObjHeaderConcept = score;
                    }
                    List<String> candidates = mchScore_objHeaderCandidates.get(scoreAgainstObjHeaderConcept);
                    if (candidates == null) candidates = new ArrayList<>();
                    candidates.add(objHeaderConceptURL);
                    mchScore_objHeaderCandidates.put(scoreAgainstObjHeaderConcept, candidates);
                    if (maxScore < scoreAgainstObjHeaderConcept) {
                        maxScore = scoreAgainstObjHeaderConcept;
                    }
                }
            }
            if (maxScore > 0 && maxScore > minScoreThreshold) {
                factIdx_matchedScores.put(index, maxScore);
                factIdx_matchedObjHeaderCandidates.
                        put(index, mchScore_objHeaderCandidates.get(maxScore));
            }
        }
    }

    private void createCandidateAnnotation(Attribute fact,
                                           TColumnHeaderAnnotation sbjCandidate,
                                           List<String> objectConcepts,
                                           TAnnotation_JI_Freebase annotation,
                                           int col1,
                                           int col2) {
        String relation_key = TColumnColumnRelationAnnotation.toStringExpanded(col1, col2, fact.getRelationURI());
        String subjectConcept = sbjCandidate.getAnnotation().getId();
        if (objectConcepts != null) {
            for (String oc : objectConcepts) {
                annotation.setScore_conceptPairAndRelation_conceptEvidence(subjectConcept, relation_key, oc, 1.0);
            }
        }

        List<TColumnColumnRelationAnnotation> candidateRelations =
                annotation.getColumncolumnRelations().get(
                        new RelationColumns(col1, col2)
                );
        if (candidateRelations == null) candidateRelations = new ArrayList<TColumnColumnRelationAnnotation>();
        boolean contains = false;
        for (TColumnColumnRelationAnnotation hbr : candidateRelations) {
            if (hbr.getRelationURI().equals(fact.getRelationURI())) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            annotation.addColumnColumnRelation(new TColumnColumnRelationAnnotation(
                    new RelationColumns(col1, col2), fact.getRelationURI(), fact.getRelationURI(), 0.0
            ));
        }
    }

    public void match_sbjCellsAndRelation(
            List<TCellAnnotation> subjectCellAnnotations,
            int subjectColumn, int objectColumn,
            TAnnotation_JI_Freebase tableAnnotations) {
        if (subjectCellAnnotations.size() > 0) {
            RelationColumns relationColumns = new RelationColumns(subjectColumn, objectColumn);
            List<TColumnColumnRelationAnnotation> candidateRelations =
                    tableAnnotations.getColumncolumnRelations().get(relationColumns);
            if (candidateRelations != null && candidateRelations.size() > 0) {
                for (int s = 0; s < subjectCellAnnotations.size(); s++) { //for each candidate subject entity
                    TCellAnnotation sbjEntity = subjectCellAnnotations.get(s);
                    List<Attribute> sbjEntityFacts = sbjEntity.getAnnotation().getAttributes(); //get the facts of that sbj ent

                    for (Attribute f : sbjEntityFacts) {
                        for (TColumnColumnRelationAnnotation hbr : candidateRelations) {
                            if (f.getRelationURI().equals(hbr.getRelationURI())) {
                                tableAnnotations.setScore_entityAndRelation(sbjEntity.getAnnotation().getId(),
                                        TColumnColumnRelationAnnotation.toStringExpanded(relationColumns.getSubjectCol(), relationColumns.getObjectCol(), f.getRelationURI()), 1.0);
                                break;
                            }
                        }
                    }
                }
            }// each subjectNE-objectNE pair
        }//each subjectNE
    }//if block checking whether the potential subject-object cell pairs are valid
}
