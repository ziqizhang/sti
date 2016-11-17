package uk.ac.shef.dcs.sti.core.algorithm.ji;

import org.simmetrics.StringMetric;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.kbproxy.model.Resource;
import uk.ac.shef.dcs.sti.core.scorer.AttributeValueMatcher;
import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.*;
import java.util.*;
import java.util.List;

/**
 * Created by zqz on 05/05/2015.
 */
public class JIAdaptedAttributeMatcher extends AttributeValueMatcher {

    public JIAdaptedAttributeMatcher(double minScoreThreshold, List<String> stopWords,
                                     StringMetric stringMetric) {
        super(minScoreThreshold, stopWords, stringMetric);
    }

    class MatchResult {
        public Resource subjectAnnotation;
        public List<Resource> objectAnnotations;
        public double score;
        public Attribute attribute;

        public MatchResult(Resource subjectAnnotation,
                           List<Resource> objectAnnotations,
                           double score,
                           Attribute attribute) {
            this.subjectAnnotation = subjectAnnotation;
            this.objectAnnotations = objectAnnotations;
            this.score = score;
            this.attribute = attribute;
        }
    }

    protected List<MatchResult> matchCellAnnotations(
            List<TCellAnnotation> subjectCellAnnotations,
            List<TCellAnnotation> objectCellAnnotations,
            DataTypeClassifier.DataType objectColumnDatatype
    ) {
        List<MatchResult> output = new ArrayList<>();

        List<Resource> objectCellEntities = new ArrayList<>();
        for (TCellAnnotation c : objectCellAnnotations)
            objectCellEntities.add(c.getAnnotation());

        if (subjectCellAnnotations.size() != 0 && objectCellAnnotations.size() != 0) {
            for (int s = 0; s < subjectCellAnnotations.size(); s++) { //for each candidate subject entity
                TCellAnnotation sbjEntity = subjectCellAnnotations.get(s);
                List<Attribute> sbjAttributes = sbjEntity.getAnnotation().getAttributes(); //get the facts of that sbj ent
                Map<Integer, DataTypeClassifier.DataType> sbjAttrValueDatatypes = classifyAttributeValueDataType(
                        sbjAttributes
                );

                final Map<Integer, Double> sbjAttrIndex_matchedScores = new HashMap<>();
                //key - index of fact; value- list of candidate entity from the obj cell matched the fact, or null if no candidate entities
                Map<Integer, List<uk.ac.shef.dcs.kbproxy.model.Resource>> sbjAttrIndex_matchedObjCellCandidates =
                        matchSubjectAttributes(objectColumnDatatype, objectCellEntities,
                                sbjAttributes, sbjAttrValueDatatypes, sbjAttrIndex_matchedScores);


                if (sbjAttrIndex_matchedScores.size() == 0) continue;
                //go thru all scores and make selection   within each subjectEntity-objectEntity pair

                List<Integer> qualified = new ArrayList<>(sbjAttrIndex_matchedScores.keySet());
                Collections.sort(qualified, (o1, o2)
                        -> sbjAttrIndex_matchedScores.get(o2).compareTo(sbjAttrIndex_matchedScores.get(o1)));
                Double highestScore = sbjAttrIndex_matchedScores.get(qualified.get(0));
                for (Map.Entry<Integer, Double> e : sbjAttrIndex_matchedScores.entrySet()) {
                    int index = e.getKey();
                    Double score = e.getValue();
                    List<uk.ac.shef.dcs.kbproxy.model.Resource> objects = sbjAttrIndex_matchedObjCellCandidates.get(index);
                    if (score.equals(highestScore) && objects != null && objects.size() > 0) {
                        List<Resource> objEntities = new ArrayList<>();
                        for (Resource r : objects)
                            objEntities.add(r);
                        Attribute attribute = sbjAttributes.get(index);
                        output.add(new MatchResult(sbjEntity.getAnnotation(), objEntities, score, attribute));

                    }
                }
            }// each subjectNE-objectNE pair
        }//each subjectNE
        return output;
    }//if block checking whether the potential subject-object cell pairs are valid


    protected List<MatchResult> matchColumnAnnotations(List<TColumnHeaderAnnotation> subjectColumnClazz,
                                                       List<TColumnHeaderAnnotation> objectColumnAnnotations,
                                                       DataTypeClassifier.DataType objectColumnDataType,
                                                       KBProxy kbSearch) throws KBProxyException {
        List<MatchResult> output = new ArrayList<>();
        List<Resource> objectColumnClazz = new ArrayList<>();
        for (TColumnHeaderAnnotation c : objectColumnAnnotations)
            objectColumnClazz.add(c.getAnnotation());

        if (subjectColumnClazz.size() > 0 && objectColumnAnnotations.size() > 0) {
            for (int s = 0; s < subjectColumnClazz.size(); s++) {
                TColumnHeaderAnnotation sbjColumnAnnotation = subjectColumnClazz.get(s);
                List<Attribute> sbjClazzAttributes = kbSearch.findAttributesOfClazz(sbjColumnAnnotation.getAnnotation().getId());
                Map<Integer, DataTypeClassifier.DataType> sbjClazzAttrValueDataTypes = classifyAttributeValueDataType(
                        sbjClazzAttributes
                );
                final Map<Integer, Double> attrIdx_matchedScores = new HashMap<>();
                final Map<Integer, List<Resource>>
                        attrIdx_matchedObjClazz =
                        matchSubjectAttributes(
                                objectColumnDataType, objectColumnClazz,
                                sbjClazzAttributes, sbjClazzAttrValueDataTypes, attrIdx_matchedScores);


                if (attrIdx_matchedScores.size() == 0) continue;
                //go thru all scores and make selection   within each subjectEntity-objectEntity pair
                List<Integer> qualified = new ArrayList<>(attrIdx_matchedScores.keySet());
                Collections.sort(qualified, (o1, o2) -> attrIdx_matchedScores.get(o2).compareTo(attrIdx_matchedScores.get(o1)));
                Double highestScore = attrIdx_matchedScores.get(qualified.get(0));
                for (Map.Entry<Integer, Double> e : attrIdx_matchedScores.entrySet()) {
                    int index = e.getKey();
                    Double score = e.getValue();
                    if (score.equals(highestScore)) {
                        Attribute attribute = sbjClazzAttributes.get(index);
                        output.add(new MatchResult(sbjColumnAnnotation.getAnnotation(), attrIdx_matchedObjClazz.get(index), score, attribute));
                    }
                }
            }//each subjectNE
        }//if block checking whether the potential subject-object cell pairs are valid
        return output;
    }


    private Map<Integer, List<Resource>> matchSubjectAttributes(
            DataTypeClassifier.DataType objectColumnDataType,
            List<Resource> objectCandidates,
            List<Attribute> sbjCandidateAttributes,
            Map<Integer, DataTypeClassifier.DataType> sbjAttrValueDataTypes,
            Map<Integer, Double> attrIdx_matchedScores
    ) {
        Map<Integer, List<Resource>>
                attrIdx_matchedObjCandidates = new HashMap<>();
        //scoring matches for the cell on the row
        for (int index = 0; index < sbjCandidateAttributes.size(); index++) {
            DataTypeClassifier.DataType type_of_attr_value = sbjAttrValueDataTypes.get(index);
            Attribute attr = sbjCandidateAttributes.get(index);
            if (!isValidType(type_of_attr_value)) {
                continue;
            }
            //use only the fact's obj (text) to compare against the header's text
            double maxScore = 0.0;
            Map<Double, List<uk.ac.shef.dcs.kbproxy.model.Resource>> mchScore_objCandidates = new HashMap<>();
            for (int o = 0; o < objectCandidates.size(); o++) {
                uk.ac.shef.dcs.kbproxy.model.Resource r = objectCandidates.get(o);
                String objCandidateURL = r.getId();
                String objCandidateLabel = r.getLabel();

                if (objCandidateURL != null) {
                    double finalScore =
                            score(objCandidateLabel, objectColumnDataType, attr.getValue(), type_of_attr_value, stopWords);
                    if (attr.getValueURI() != null) {
                        double score = objCandidateURL.equals(attr.getValueURI()) ? 1.0 : 0.0;
                        if (score > finalScore) finalScore = score;
                    }
                    List<uk.ac.shef.dcs.kbproxy.model.Resource> candidates = mchScore_objCandidates.get(finalScore);
                    if (candidates == null) candidates = new ArrayList<>();
                    candidates.add(r);
                    mchScore_objCandidates.put(finalScore, candidates);
                    if (maxScore < finalScore) {
                        maxScore = finalScore;
                    }
                }
            }
            if (maxScore > 0 && maxScore > minScoreThreshold) {
                attrIdx_matchedScores.put(index, maxScore);
                attrIdx_matchedObjCandidates.
                        put(index, mchScore_objCandidates.get(maxScore));
            }
        }
        return attrIdx_matchedObjCandidates;
    }


    //if any cell's candidate annotation votes for a candidate relation emerging from that cell's column as subject column
    //create an affinity link from that cell's candidate annotation and that relation annotation
    protected void matchCellAnnotationAndRelation(
            List<TCellAnnotation> subjectCellAnnotations,
            int subjectColumn, int objectColumn,
            TAnnotationJI tableAnnotations) {
        if (subjectCellAnnotations.size() > 0) {
            RelationColumns relationColumns = new RelationColumns(subjectColumn, objectColumn);
            List<TColumnColumnRelationAnnotation> candidateRelations =
                    tableAnnotations.getColumncolumnRelations().get(relationColumns);
            if (candidateRelations != null && candidateRelations.size() > 0) {
                for (int s = 0; s < subjectCellAnnotations.size(); s++) { //for each candidate subject entity
                    TCellAnnotation sbjEntity = subjectCellAnnotations.get(s);
                    List<Attribute> sbjEntityAttr = sbjEntity.getAnnotation().getAttributes(); //get the facts of that sbj ent

                    for (Attribute f : sbjEntityAttr) {
                        for (TColumnColumnRelationAnnotation hbr : candidateRelations) {
                            if (f.getRelationURI().equals(hbr.getRelationURI())) {
                                tableAnnotations.setScoreEntityAndRelation(sbjEntity.getAnnotation().getId(),
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
