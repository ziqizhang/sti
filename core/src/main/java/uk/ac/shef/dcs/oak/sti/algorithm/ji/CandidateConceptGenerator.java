package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseFreebaseFilter;
import uk.ac.shef.dcs.oak.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public class CandidateConceptGenerator {
    private KnowledgeBaseSearcher kbSearcher;
    private ClassificationScorer_JI_adapted conceptScorer;
    private EntityAndConceptScorer_Freebase entityAndConceptScorer;

    public CandidateConceptGenerator(KnowledgeBaseSearcher kbSearcher,
                                     ClassificationScorer_JI_adapted conceptScorer,
                                     EntityAndConceptScorer_Freebase entityAndConceptScorer)
    {
        this.kbSearcher = kbSearcher;
        this.conceptScorer=conceptScorer;
        this.entityAndConceptScorer=entityAndConceptScorer;
    }

    public void generateCandidateConcepts(LTableAnnotation_JI_Freebase tableAnnotation, LTable table, int col) throws IOException {
        Map<String, String> distinctTypes = new HashMap<String, String>();
        Map<String, List<String>> entityId_and_conceptURLs = new HashMap<String, List<String>>();
        Map<String, String> distinctEntities = new HashMap<String, String>();
        for (int r = 0; r < table.getNumRows(); r++) {
            CellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, col);
            if (cellAnnotations.length > 0) {
                for (CellAnnotation ca : cellAnnotations) {
                    EntityCandidate e = ca.getAnnotation();
                    distinctEntities.put(e.getId(), e.getName());
                    for(String[] type: KnowledgeBaseFreebaseFilter.filterTypes(e.getTypes())){
                        String url = type[0];
                        String label = type[1];
                        distinctTypes.put(url, label);
                        List<String> conceptURLs = entityId_and_conceptURLs.get(e.getId());
                        if(conceptURLs==null)
                            conceptURLs=new ArrayList<String>();
                        if(!conceptURLs.contains(url))
                            conceptURLs.add(url);
                        entityId_and_conceptURLs.put(e.getId(), conceptURLs);
                    }
                }
            }
        }

        //go thru every distinct type, create header annotation candidate
        HeaderAnnotation[] headerAnnotations = new HeaderAnnotation[distinctTypes.size()];
        int count=0;
        for (Map.Entry<String, String> concept : distinctTypes.entrySet()) {
            HeaderAnnotation ha = new HeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                    concept.getKey(), concept.getValue(), 0.0);
            Map<String, Double> score_elements=conceptScorer.score(ha, table.getColumnHeader(col));
            conceptScorer.compute_final_score(score_elements);
            ha.setFinalScore(score_elements.get(ClassificationScorer_JI_adapted.SCORE_HEADER_FACTOR));
            ha.setScoreElements(score_elements);
            headerAnnotations[count]=ha;
            count++;
        }
        Arrays.sort(headerAnnotations);
        tableAnnotation.setHeaderAnnotation(col, headerAnnotations);

        //go thru every entity-concept pair, compute their scores
        System.out.print("-E_and_C_scores-(tot.Ent:"+distinctEntities.size() + "-tot.Cpt:"+distinctTypes.size()+">");
        int cc=0;
        for(String entityId: distinctEntities.keySet()){
            for(String conceptId: distinctTypes.keySet()){
                cc++;
                double sim = entityAndConceptScorer.computeEntityConceptSimilarity(entityId, conceptId, kbSearcher);
                tableAnnotation.setScore_entityAndConcept(entityId, conceptId, sim);
                if(cc%50==0) System.out.print(cc + ",");
            }
        }
        System.out.println(")");
        //then update scores for every entity-concept pair where the entity votes for the concept
        System.out.print("-E_and_C_scores_update-(tot.Ent:"+distinctEntities.size() +">");
        cc=0;
        for(Map.Entry<String, List<String>> entry: entityId_and_conceptURLs.entrySet()){
            String entityId = entry.getKey();
            List<String> conceptIds = entry.getValue();
            System.out.print(cc + "="+conceptIds.size()+",");
            for(String conceptId : conceptIds){
                double specificity = entityAndConceptScorer.computeConceptSpecificity(conceptId, kbSearcher);
                double simScore = tableAnnotation.getScore_entityAndConcept(entityId, conceptId);
                tableAnnotation.setScore_entityAndConcept(entityId, conceptId, simScore+1.0+specificity);
            }
            cc++;
        }
        System.out.println(")");
    }
}
