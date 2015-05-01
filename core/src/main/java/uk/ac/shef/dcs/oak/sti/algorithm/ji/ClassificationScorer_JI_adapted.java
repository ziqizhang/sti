package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.rep.*;
import uk.ac.shef.dcs.oak.triplesearch.EntityCandidate;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.CosineSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by zqz on 01/05/2015.
 */
public class ClassificationScorer_JI_adapted  {
    public static String SCORE_HEADER_FACTOR ="score_factor_graph-header";
    public static String SCORE_LEV = "stringsim_lev";
    public static String SCORE_JACCARD = "stringsim_jaccard";
    public static String SCORE_COSINE="stringsim_cosine";

    private AbstractStringMetric cosine;
    private AbstractStringMetric jaccard;
    private AbstractStringMetric lev;

    public ClassificationScorer_JI_adapted() {
        cosine = new CosineSimilarity();
        jaccard = new JaccardSimilarity();
        lev =new Levenshtein();
    }

    public Map<String, Double> score(HeaderAnnotation candidate, LTableColumnHeader header
                                     ) {

        double levScore = calculateStringSimilarity(header.getHeaderText(), candidate, lev);
        //dice between NE and cell text
        double jaccardScore = calculateStringSimilarity(header.getHeaderText(), candidate, jaccard);
        double cosineScore = calculateStringSimilarity(header.getHeaderText(), candidate, cosine);

        Map<String, Double> score_elements = new HashMap<String, Double>();
        score_elements.put(SCORE_HEADER_FACTOR, levScore+jaccardScore+cosineScore);
        score_elements.put(SCORE_COSINE,cosineScore);
        score_elements.put(SCORE_JACCARD,jaccardScore);
        score_elements.put(SCORE_LEV, levScore);
        return score_elements;
    }

    private double calculateStringSimilarity(String text, HeaderAnnotation candidate, AbstractStringMetric lev) {
        double baseScore = lev.getSimilarity(text, candidate.getAnnotation_label());
        return baseScore;
    }

    public double compute_final_score(Map<String, Double> scoreMap) {
        scoreMap.put(CellAnnotation.SCORE_FINAL, scoreMap.get(SCORE_HEADER_FACTOR));
        return scoreMap.get(SCORE_HEADER_FACTOR);
    }
}

