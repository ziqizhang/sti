package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.kb.KBSearcher;
import uk.ac.shef.dcs.oak.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.misc.KB_InstanceFilter;
import uk.ac.shef.dcs.oak.sti.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.LTable;
import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.io.IOException;
import java.util.*;

/**
 *
 */
public class CandidateConceptGenerator {
    private KBSearcher kbSearcher;
    private ClassificationScorer_JI_adapted conceptScorer;
    private EntityAndConceptScorer entityAndConceptScorer;

    public static final String SMP_SCORE_ENTITY_VOTE = "smp_score_entity_vote";
    public static final String SMP_SCORE_GRANULARITY = "smp_score_granularity";

    public CandidateConceptGenerator(KBSearcher kbSearcher,
                                     ClassificationScorer_JI_adapted conceptScorer,
                                     EntityAndConceptScorer entityAndConceptScorer)
    {
        this.kbSearcher = kbSearcher;
        this.conceptScorer=conceptScorer;
        this.entityAndConceptScorer=entityAndConceptScorer;
    }

    public void generateCandidateConcepts(LTableAnnotation tableAnnotation, LTable table, int col) throws IOException {
        //todo: collect all candidates from cells
        //todo: score all candidates
        Set<String> distinctTypes = new HashSet<String>();
        Map<String, List<String>> entityId_and_conceptURLs = new HashMap<String, List<String>>();
        for (int r = 0; r < table.getNumRows(); r++) {
            CellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, col);
            if (cellAnnotations.length > 0) {
                for (CellAnnotation ca : cellAnnotations) {
                    EntityCandidate e = ca.getAnnotation();
                    for(String type: e.getTypeIds()){
                        if(KB_InstanceFilter.ignoreType(type, type)) continue;
                        distinctTypes.add(type);
                        List<String> conceptURLs = entityId_and_conceptURLs.get(e.getId());
                        if(conceptURLs==null)
                            conceptURLs=new ArrayList<String>();
                        if(!conceptURLs.contains(type))
                            conceptURLs.add(type);
                        entityId_and_conceptURLs.put(e.getId(), conceptURLs);
                    }
                }
            }
        }

                    //a header annotation will only have granularity score if there are more than one candidate with the same vote score
           /* HeaderAnnotation[] headerAnnotations = new HeaderAnnotation[result_votes.size()];
            int i = 0;
            for (ObjObj<String, Double> oo : result_votes) {
                HeaderAnnotation ha = new HeaderAnnotation(table.getColumnHeader(col).getHeaderText(),
                        oo.getMainObject(), oo.getMainObject(), oo.getOtherObject());
                ha.getScoreElements().put(SMP_SCORE_ENTITY_VOTE, oo.getOtherObject());
                Double granularity_score = result_granularity.get(oo.getMainObject());
                granularity_score = granularity_score == null ? 0 : granularity_score;
                ha.getScoreElements().put(SMP_SCORE_GRANULARITY, granularity_score);
                ha.getScoreElements().put(HeaderAnnotation.FINAL, oo.getOtherObject());
                headerAnnotations[i] = ha;
                i++;
            }
            tableAnnotation.setHeaderAnnotation(col, headerAnnotations);
        }*/
    }
}
