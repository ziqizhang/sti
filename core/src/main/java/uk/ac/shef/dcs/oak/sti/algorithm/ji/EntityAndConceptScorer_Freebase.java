package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import javafx.util.Pair;
import uk.ac.shef.dcs.oak.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseFreebaseFilter;
import uk.ac.shef.dcs.oak.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.oak.sti.nlp.NLPTools;
import uk.ac.shef.dcs.oak.triplesearch.rep.Clazz;
import uk.ac.shef.dcs.oak.triplesearch.rep.Entity;
import uk.ac.shef.dcs.oak.util.CollectionUtils;
import uk.ac.shef.dcs.oak.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqz on 01/05/2015.
 */
public class EntityAndConceptScorer_Freebase {

    private List<String> stopWords;
    private Lemmatizer lemmatizer;


    public EntityAndConceptScorer_Freebase(List<String> stopWords,
                                            String nlpResources) throws IOException {
        if (nlpResources != null)
            lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();

        this.stopWords = stopWords;
    }

    protected static double FREEBASE_TOTAL_TOPICS=47560900; //total # of topics on freebase as by 1 May 2015


    public double computeEntityConceptSimilarity_fresh(List<String[]> entity_triples,
                                                       List<String[]> concept_triples
                                                       ){/* BOW OF THE ENTITY*/
        List<String> bag_of_words_for_entity = new ArrayList<String>();
        for (String[] f : entity_triples) {
            if (!TableMinerConstants.USE_NESTED_RELATION_AND_FACTS_FOR_ENTITY_FEATURE && f[3].equals("y"))
                continue;
            if (KnowledgeBaseFreebaseFilter.ignoreFactFromBOW(f[0]))
                continue;
            String value = f[1];
            if (!StringUtils.isPath(value))
                bag_of_words_for_entity.addAll(StringUtils.toBagOfWords(value, true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
            else
                bag_of_words_for_entity.add(value);
        }
        if (lemmatizer != null)
            bag_of_words_for_entity = lemmatizer.lemmatize(bag_of_words_for_entity);
        bag_of_words_for_entity.removeAll(stopWords);

        List<String> bag_of_words_for_concept = new ArrayList<String>();
        for (String[] f : concept_triples) {
            if (!TableMinerConstants.USE_NESTED_RELATION_AND_FACTS_FOR_ENTITY_FEATURE && f[3].equals("y"))
                continue;
            if (KnowledgeBaseFreebaseFilter.ignoreFactFromBOW(f[0]))
                continue;
            String value = f[1];
            if (!StringUtils.isPath(value))
                bag_of_words_for_concept.addAll(StringUtils.toBagOfWords(value, true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
            else
                bag_of_words_for_concept.add(value);
        }
        if (lemmatizer != null)
            bag_of_words_for_concept = lemmatizer.lemmatize(bag_of_words_for_concept);
        bag_of_words_for_concept.removeAll(stopWords);

        double contextOverlapScore = CollectionUtils.scoreOverlap_dice_keepFrequency(
                bag_of_words_for_entity, bag_of_words_for_concept
        );
        //kbSearcher.saveSimilarity(entity_id,concept_url,contextOverlapScore,true);
        return contextOverlapScore;
    }
    public Pair<Double, String> computeEntityConceptSimilarity(Entity entity,
                                                                 Clazz concept,
                                                 KnowledgeBaseSearcher kbSearcher,
                                                 boolean useCache) throws IOException {
        double score = -1;
        if(useCache)
            score=kbSearcher.find_similarity(entity.getId(), concept.getId());
        String fromCache="no";
        if(score!=-1)
            fromCache="cache";
        if(score==-1.0) {
            List<String[]> entity_triples = entity.getTriples();
        /* BOW OF THE ENTITY*/
            List<String> bag_of_words_for_entity = new ArrayList<String>();
            for (String[] f : entity_triples) {
                if (!TableMinerConstants.USE_NESTED_RELATION_AND_FACTS_FOR_ENTITY_FEATURE && f[3].equals("y"))
                    continue;
                if (KnowledgeBaseFreebaseFilter.ignoreFactFromBOW(f[0]))
                    continue;
                String value = f[1];
                if (!StringUtils.isPath(value))
                    bag_of_words_for_entity.addAll(StringUtils.toBagOfWords(value, true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
                else
                    bag_of_words_for_entity.add(value);
            }
            if (lemmatizer != null)
                bag_of_words_for_entity = lemmatizer.lemmatize(bag_of_words_for_entity);
            bag_of_words_for_entity.removeAll(stopWords);

            List<String[]> concept_triples = concept.getTriples();
            List<String> bag_of_words_for_concept = new ArrayList<String>();
            for (String[] f : concept_triples) {
                if (!TableMinerConstants.USE_NESTED_RELATION_AND_FACTS_FOR_ENTITY_FEATURE && f[3].equals("y"))
                    continue;
                if (KnowledgeBaseFreebaseFilter.ignoreFactFromBOW(f[0]))
                    continue;
                String value = f[1];
                if (!StringUtils.isPath(value))
                    bag_of_words_for_concept.addAll(StringUtils.toBagOfWords(value, true, true, TableMinerConstants.DISCARD_SINGLE_CHAR_IN_BOW));
                else
                    bag_of_words_for_concept.add(value);
            }
            if (lemmatizer != null)
                bag_of_words_for_concept = lemmatizer.lemmatize(bag_of_words_for_concept);
            bag_of_words_for_concept.removeAll(stopWords);

            double contextOverlapScore = CollectionUtils.scoreOverlap_dice_keepFrequency(
                    bag_of_words_for_entity, bag_of_words_for_concept
            );
            //kbSearcher.saveSimilarity(entity_id,concept_url,contextOverlapScore,true);
            return new Pair<>(contextOverlapScore,fromCache);
        }
        else{
            return new Pair<>(score, fromCache);
        }
    }

    public double computeConceptSpecificity(String concept_url, KnowledgeBaseSearcher kbSearcher) throws IOException {
        double conceptGranularity = kbSearcher.find_granularityForConcept(concept_url);
        if(conceptGranularity<0)
            return 0.0;
        return 1-Math.sqrt(conceptGranularity/FREEBASE_TOTAL_TOPICS);
    }
}
