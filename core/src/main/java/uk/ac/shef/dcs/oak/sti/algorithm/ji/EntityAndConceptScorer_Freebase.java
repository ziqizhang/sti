package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.experiment.TableMinerConstants;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseSearcher;
import uk.ac.shef.dcs.oak.sti.kb.KnowledgeBaseFreebaseFilter;
import uk.ac.shef.dcs.oak.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.oak.sti.nlp.NLPTools;
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

    public double score(String entity_id, String concept_url, KnowledgeBaseSearcher kbSearcher) throws IOException {
        double specificity = computeConceptSpecificity(concept_url, kbSearcher);
        double similarity = computeEntityConceptSimilarity(entity_id, concept_url, kbSearcher);
        return specificity+similarity+1.0;
    }

    private double computeEntityConceptSimilarity(String entity_id, String concept_url, KnowledgeBaseSearcher kbSearcher) throws IOException {
        List<String[]> entity_triples=kbSearcher.find_triplesForEntity_filtered(entity_id);
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

        List<String[]> concept_triples=kbSearcher.find_triplesForConcept_filtered(concept_url);
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
        return contextOverlapScore;
    }

    private double computeConceptSpecificity(String concept_url, KnowledgeBaseSearcher kbSearcher) throws IOException {
        double conceptGranularity = kbSearcher.find_granularityForConcept(concept_url);
        return 1-Math.sqrt(conceptGranularity/FREEBASE_TOTAL_TOPICS);
    }
}
