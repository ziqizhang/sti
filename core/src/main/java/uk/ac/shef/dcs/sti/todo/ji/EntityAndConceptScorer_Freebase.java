package uk.ac.shef.dcs.sti.todo.ji;

import javafx.util.Pair;
import uk.ac.shef.dcs.kbsearch.KBSearchException;
import uk.ac.shef.dcs.kbsearch.model.Attribute;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.kbsearch.KBSearch;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.kbsearch.model.Clazz;
import uk.ac.shef.dcs.kbsearch.model.Entity;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;

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
            if (!STIConstantProperty.ENTITYBOW_INCLUDE_INDIRECT_ATTRIBUTE && f[3].equals("y"))
                continue;
            String value = f[1];
            if (!StringUtils.isPath(value))
                bag_of_words_for_entity.addAll(StringUtils.toBagOfWords(value, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
            else
                bag_of_words_for_entity.add(value);
        }
        if (lemmatizer != null)
            bag_of_words_for_entity = lemmatizer.lemmatize(bag_of_words_for_entity);
        bag_of_words_for_entity.removeAll(stopWords);

        List<String> bag_of_words_for_concept = new ArrayList<String>();
        for (String[] f : concept_triples) {
            if (!STIConstantProperty.ENTITYBOW_INCLUDE_INDIRECT_ATTRIBUTE && f[3].equals("y"))
                continue;
            String value = f[1];
            if (!StringUtils.isPath(value))
                bag_of_words_for_concept.addAll(StringUtils.toBagOfWords(value, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
            else
                bag_of_words_for_concept.add(value);
        }
        if (lemmatizer != null)
            bag_of_words_for_concept = lemmatizer.lemmatize(bag_of_words_for_concept);
        bag_of_words_for_concept.removeAll(stopWords);

        double contextOverlapScore = CollectionUtils.computeFrequencyWeightedDice(
                bag_of_words_for_entity, bag_of_words_for_concept
        );
        //kbSearch.cacheEntityConceptSimilarity(entity_id,concept_url,contextOverlapScore,true);
        return contextOverlapScore;
    }
    public Pair<Double, String> computeEntityConceptSimilarity(Entity entity,
                                                                 Clazz concept,
                                                 KBSearch kbSearch,
                                                 boolean useCache) throws KBSearchException {
        double score = -1;
        if(useCache)
            score= kbSearch.findEntityConceptSimilarity(entity.getId(), concept.getId());
        String fromCache="no";
        if(score!=-1)
            fromCache="cache";
        if(score==-1.0) {
            List<Attribute> entity_triples = entity.getAttributes();
        /* BOW OF THE ENTITY*/
            List<String> bag_of_words_for_entity = new ArrayList<>();
            for (Attribute f : entity_triples) {
                if (!STIConstantProperty.ENTITYBOW_INCLUDE_INDIRECT_ATTRIBUTE &&
                        !f.isDirect())
                    continue;

                String value = f.getValue();
                if (!StringUtils.isPath(value))
                    bag_of_words_for_entity.addAll(StringUtils.toBagOfWords(value, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
                else
                    bag_of_words_for_entity.add(value);
            }
            if (lemmatizer != null)
                bag_of_words_for_entity = lemmatizer.lemmatize(bag_of_words_for_entity);
            bag_of_words_for_entity.removeAll(stopWords);

            List<Attribute> concept_triples = concept.getAttributes();
            List<String> bag_of_words_for_concept = new ArrayList<>();
            for (Attribute f : concept_triples) {
                if (!STIConstantProperty.ENTITYBOW_INCLUDE_INDIRECT_ATTRIBUTE
                        && !f.isDirect())
                    continue;
                String value = f.getValue();
                if (!StringUtils.isPath(value))
                    bag_of_words_for_concept.addAll(StringUtils.toBagOfWords(value, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
                else
                    bag_of_words_for_concept.add(value);
            }
            if (lemmatizer != null)
                bag_of_words_for_concept = lemmatizer.lemmatize(bag_of_words_for_concept);
            bag_of_words_for_concept.removeAll(stopWords);

            double contextOverlapScore = CollectionUtils.computeFrequencyWeightedDice(
                    bag_of_words_for_entity, bag_of_words_for_concept
            );
            //kbSearch.cacheEntityConceptSimilarity(entity_id,concept_url,contextOverlapScore,true);
            return new Pair<>(contextOverlapScore,fromCache);
        }
        else{
            return new Pair<>(score, fromCache);
        }
    }

    public double computeConceptSpecificity(String concept_url, KBSearch kbSearch) throws KBSearchException {
        double conceptGranularity = kbSearch.findGranularityOfClazz(concept_url);
        if(conceptGranularity<0)
            return 0.0;
        return 1-Math.sqrt(conceptGranularity/FREEBASE_TOTAL_TOPICS);
    }
}
