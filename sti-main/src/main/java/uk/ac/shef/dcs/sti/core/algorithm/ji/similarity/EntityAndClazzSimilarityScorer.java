package uk.ac.shef.dcs.sti.core.algorithm.ji.similarity;

import javafx.util.Pair;

import uk.ac.shef.dcs.kbproxy.KBProxy;
import uk.ac.shef.dcs.kbproxy.KBProxyException;
import uk.ac.shef.dcs.kbproxy.model.Attribute;
import uk.ac.shef.dcs.sti.STIConstantProperty;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.kbproxy.model.Clazz;
import uk.ac.shef.dcs.kbproxy.model.Entity;
import uk.ac.shef.dcs.sti.util.CollectionUtils;
import uk.ac.shef.dcs.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqz on 01/05/2015.
 */
public class EntityAndClazzSimilarityScorer {

    private List<String> stopWords;
    private Lemmatizer lemmatizer;

    public EntityAndClazzSimilarityScorer(List<String> stopWords,
                                          String nlpResources) throws IOException {
        if (nlpResources != null)
            lemmatizer = NLPTools.getInstance(nlpResources).getLemmatizer();
        this.stopWords = stopWords;
    }

    public Pair<Double, Boolean> computeEntityConceptSimilarity(Entity entity,
                                                               Clazz concept,
                                                               KBProxy kbSearch,
                                                               boolean useCache) throws KBProxyException {
        double score = -1;
        if (useCache)
            score = kbSearch.findEntityClazzSimilarity(entity.getId(), concept.getId());
        boolean fromCache = false;
        if (score != -1)
            fromCache =true;
        if (score == -1.0) {
            List<Attribute> entityAttributes = entity.getAttributes();
        /* BOW OF THE ENTITY*/
            List<String> entityBow = new ArrayList<>();
            for (Attribute f : entityAttributes) {
                if (!STIConstantProperty.BOW_ENTITY_INCLUDE_INDIRECT_ATTRIBUTE &&
                        !f.isDirect())
                    continue;

                String value = f.getValue();
                if (!StringUtils.isPath(value))
                    entityBow.addAll(StringUtils.toBagOfWords(value, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
                else
                    entityBow.add(value);
            }
            if (lemmatizer != null)
                entityBow = lemmatizer.lemmatize(entityBow);
            entityBow.removeAll(stopWords);

            List<Attribute> clazzAttributes = concept.getAttributes();
            List<String> clazzBow = new ArrayList<>();
            for (Attribute f : clazzAttributes) {
                if (!STIConstantProperty.BOW_ENTITY_INCLUDE_INDIRECT_ATTRIBUTE
                        && !f.isDirect())
                    continue;
                String value = f.getValue();
                if (!StringUtils.isPath(value))
                    clazzBow.addAll(StringUtils.toBagOfWords(value, true, true, STIConstantProperty.BOW_DISCARD_SINGLE_CHAR));
                else
                    clazzBow.add(value);
            }
            if (lemmatizer != null)
                clazzBow = lemmatizer.lemmatize(clazzBow);
            clazzBow.removeAll(stopWords);

            double contextOverlapScore = CollectionUtils.computeFrequencyWeightedDice(
                    entityBow, clazzBow
            );
            //kbSearch.cacheEntityConceptSimilarity(entity_id,concept_url,contextOverlapScore,true);
            return new Pair<>(contextOverlapScore, fromCache);
        } else {
            return new Pair<>(score, fromCache);
        }
    }

}
