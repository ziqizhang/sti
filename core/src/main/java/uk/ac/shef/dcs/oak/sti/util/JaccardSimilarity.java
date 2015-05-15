package uk.ac.shef.dcs.oak.sti.util;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.tokenisers.InterfaceTokeniser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zqz on 15/05/2015.
 */
public final class JaccardSimilarity extends AbstractStringMetric implements Serializable {

    /**
     * a constant for calculating the estimated timing cost.
     */
    private final float ESTIMATEDTIMINGCONST = 1.4e-4f;

    /**
     * private tokeniser for tokenisation of the query strings.
     */
    private final InterfaceTokeniser tokeniser;

    /**
     * constructor - default.
     */
    public JaccardSimilarity() {
        tokeniser = new TokeniserWhitespace();
    }

    /**
     * constructor.
     *
     * @param tokeniserToUse - the tokeniser to use should a different tokeniser be required
     */
    public JaccardSimilarity(final InterfaceTokeniser tokeniserToUse) {
        tokeniser = tokeniserToUse;
    }

    /**
     * returns the string identifier for the metric .
     *
     * @return the string identifier for the metric
     */
    public String getShortDescriptionString() {
        return "JaccardSimilarity";
    }

    /**
     * returns the long string identifier for the metric.
     *
     * @return the long string identifier for the metric
     */
    public String getLongDescriptionString() {
        return "Implements the Jaccard Similarity algorithm providing a similarity measure between two strings";
    }

    /**
     * gets a div class xhtml similarity explaining the operation of the metric.
     *
     * @param string1 string 1
     * @param string2 string 2
     *
     * @return a div class html section detailing the metric operation.
     */
    public String getSimilarityExplained(String string1, String string2) {
        //todo this should explain the operation of a given comparison
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * gets the estimated time in milliseconds it takes to perform a similarity timing.
     *
     * @param string1 string 1
     * @param string2 string 2
     *
     * @return the estimated time in milliseconds taken to perform the similarity measure
     */
    public float getSimilarityTimingEstimated(final String string1, final String string2) {
        //timed millisecond times with string lengths from 1 + 50 each increment
        //0     0.02    0.03    0.05    0.07    0.11    0.14    0.18    0.23    0.27    0.34    0.38    0.45    0.51    0.59    0.67    0.75    0.83    0.94    1       1.15    1.22    1.49    1.46    1.93    1.69    2.11    1.95    2.42    2.21    2.87    2.51    3.27    2.86    3.69    3.22    3.9     3.5     4.74    3.9     4.95    4.23    5.49    4.72    5.8     5.21    6.38    5.64    7.25    5.97    7.81    6.55    8.46    7       9.27    7.52    10.15   8.12    10.15   8.46
        final float str1Tokens = tokeniser.tokenizeToArrayList(string1).size();
        final float str2Tokens = tokeniser.tokenizeToArrayList(string2).size();
        return (str1Tokens * str2Tokens) * ESTIMATEDTIMINGCONST;
    }

    /**
     * gets the similarity of the two strings using JaccardSimilarity.
     *
     * @param string1
     * @param string2
     * @return a value between 0-1 of the similarity
     */
    public float getSimilarity(final String string1, final String string2) {
/*
Each instance is represented as a Jaccard vector similarity function. The Jaccard between two vectors X and Y is

(X*Y) / (|X||Y|-(X*Y))

where (X*Y) is the inner product of X and Y, and |X| = (X*X)^1/2, i.e. the Euclidean norm of X.

This can more easily be described as ( |X & Y| ) / ( | X or Y | )
*/
        //todo this needs checking
        final ArrayList<String> str1Tokens = tokeniser.tokenizeToArrayList(string1);
        final ArrayList<String> str2Tokens = tokeniser.tokenizeToArrayList(string2);

        final Set<String> allTokens = new HashSet<String>();
        allTokens.addAll(str1Tokens);
        final int termsInString1 = allTokens.size();
        final Set<String> secondStringTokens = new HashSet<String>();
        secondStringTokens.addAll(str2Tokens);
        final int termsInString2 = secondStringTokens.size();

        //now combine the sets
        allTokens.addAll(secondStringTokens);
        final int commonTerms = (termsInString1 + termsInString2) - allTokens.size();

        //return JaccardSimilarity
        return (float) (commonTerms) / (float) (allTokens.size());
    }

    /**
     * gets the un-normalised similarity measure of the metric for the given strings.
     *
     * @param string1
     * @param string2
     * @return returns the score of the similarity measure (un-normalised)
     */
    public float getUnNormalisedSimilarity(String string1, String string2) {
        return getSimilarity(string1, string2);
    }
}
