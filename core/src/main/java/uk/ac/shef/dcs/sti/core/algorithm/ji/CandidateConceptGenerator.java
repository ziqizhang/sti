package uk.ac.shef.dcs.sti.core.algorithm.ji;

import javafx.util.Pair;
import org.apache.log4j.Logger;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.algorithm.ji.similarity.EntityAndClazzSimilarityScorer;
import uk.ac.shef.dcs.sti.core.algorithm.ji.similarity.SimilarityComputerManager;
import uk.ac.shef.dcs.sti.core.algorithm.smp.ClazzSpecificityCalculator;
import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.*;

/**
 *
 */
public class CandidateConceptGenerator {

    private static final Logger LOG = Logger.getLogger(CandidateConceptGenerator.class.getName());
    private int threads = 10;
    private SimilarityComputerManager simComputer;
    private KBSearch kbSearch;
    private JIClazzScorer clazzScorer;
    private ClazzSpecificityCalculator clazzSpecificityCalculator;

    public CandidateConceptGenerator(KBSearch kbSearch,
                                     JIClazzScorer clazzScorer,
                                     EntityAndClazzSimilarityScorer entityAndConceptScorer,
                                     ClazzSpecificityCalculator clazzSpecificityCalculator,
                                     int threads,
                                     boolean useCache) {
        this.kbSearch = kbSearch;
        this.clazzScorer = clazzScorer;
        this.threads = threads;
        this.clazzSpecificityCalculator = clazzSpecificityCalculator;
        simComputer = new SimilarityComputerManager(useCache,kbSearch,entityAndConceptScorer);
    }

    public void generateInitialColumnAnnotations(TAnnotationJI tableAnnotation, Table table, int col) throws KBSearchException, STIException {
        List<Clazz> distinctTypes = new ArrayList<>();
        Map<String, Set<String>> entityId_and_clazzURLs = new HashMap<>();
        List<Entity> distinctEntities = new ArrayList<>();

        List<TColumnHeaderAnnotation> candidates = new ArrayList<>();
        for (int r = 0; r < table.getNumRows(); r++) {
            TCellAnnotation[] cellAnnotations = tableAnnotation.getContentCellAnnotations(r, col);
            List<Pair<Entity, Map<String, Double>>> entities = new ArrayList<>();
            for (TCellAnnotation tCellAnnotation : cellAnnotations) {
                Entity e = tCellAnnotation.getAnnotation();
                entities.add(new Pair<>(e, tCellAnnotation.getScoreElements()));
                if (!distinctEntities.contains(tCellAnnotation.getAnnotation()))
                    distinctEntities.add(tCellAnnotation.getAnnotation());

                for (Clazz c : e.getTypes()) {
                    Set<String> conceptURLs = entityId_and_clazzURLs.get(e.getId());
                    if (conceptURLs == null)
                        conceptURLs = new HashSet<>();
                    conceptURLs.add(c.getId());
                    entityId_and_clazzURLs.put(e.getId(), conceptURLs);
                }

            }
            candidates =
                    clazzScorer.computeElementScores(entities, candidates, table, Collections.singletonList(r), col);
        }

        //fetch all concept entities and set attributes
        for (TColumnHeaderAnnotation tca : candidates) {
            clazzScorer.computeFinal(tca, table.getNumRows());
            Clazz c = tca.getAnnotation();
            LOG.info("\t\t>> retrieving clazz attributes (may involve querying KB) for " + c);
            List<Attribute> triples = kbSearch.findAttributesOfClazz(c.getId());
            c.setAttributes(triples);
            if (!distinctTypes.contains(c))
                distinctTypes.add(c);
        }

        tableAnnotation.setHeaderAnnotation(col, candidates.toArray(new TColumnHeaderAnnotation[candidates.size()]));

        //go thru every entity-concept pair, compute their scores
        LOG.info("\t\t>> compute entity-clazz semantic similarity (Ent:" + distinctEntities.size() + " Clz:" + distinctTypes.size() + ")");
        Map<String, Double> simScores =
                simComputer.computeSemanticSimilarity(threads, distinctEntities, distinctTypes, true);
        for (Entity entity : distinctEntities) {
            for (Clazz concept : distinctTypes) {
                Double sim = simScores.get(entity.getId() + "," + concept.getId());
                assert sim != null;
                tableAnnotation.setScoreEntityAndConceptSimilarity(entity.getId(), concept.getId(), sim);
            }
        }

        //then update scores for every entity-concept pair where the entity votes for the concept
        LOG.info("\t\t>> compute entity-clazz affinity scores, can involve querying KB for computing clazz specificity (Ent:" + distinctEntities.size() + ")");
        int cc = 0;
        for (Map.Entry<String, Set<String>> entry : entityId_and_clazzURLs.entrySet()) {
            String entityId = entry.getKey();
            Set<String> conceptIds = entry.getValue();
            for (String conceptId : conceptIds) {
                double specificity = clazzSpecificityCalculator.compute(conceptId);
                double simScore = tableAnnotation.getScoreEntityAndConceptSimilarity(entityId, conceptId);
                tableAnnotation.setScoreEntityAndConceptSimilarity(
                        entityId, conceptId, simScore + 1.0 + specificity);
            }
            cc++;
            if(cc%10==0)
                LOG.debug("\t\t>> compute entity-clazz affinity scores, "+cc+"/"+distinctEntities.size()+" complete");
        }
    }


}
