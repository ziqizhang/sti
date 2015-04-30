package uk.ac.shef.dcs.oak.sti.table.util;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.util.Arrays;

/**
 * Created by zqz on 28/04/2015.
 */
public class DiceSimilarity extends AbstractStringMetric {
    @Override
    public String getShortDescriptionString() {
        return null;
    }

    @Override
    public String getLongDescriptionString() {
        return null;
    }

    @Override
    public String getSimilarityExplained(String s, String s2) {
        return null;
    }

    @Override
    public float getSimilarityTimingEstimated(String s, String s2) {
        return 0;
    }

    @Override
    public float getSimilarity(String s, String t) {
        // Verifying the input:
        if (s == null || t == null)
            return 0;
        // Quick check to catch identical objects:
        if (s == t)
            return 1;
        // avoid exception for single character searches
        if (s.length() < 2 || t.length() < 2)
            return 0;

        // Create the bigrams for string s:
        final int n = s.length()-1;
        final int[] sPairs = new int[n];
        for (int i = 0; i <= n; i++)
            if (i == 0)
                sPairs[i] = s.charAt(i) << 16;
            else if (i == n)
                sPairs[i-1] |= s.charAt(i);
            else
                sPairs[i] = (sPairs[i-1] |= s.charAt(i)) << 16;

        // Create the bigrams for string t:
        final int m = t.length()-1;
        final int[] tPairs = new int[m];
        for (int i = 0; i <= m; i++)
            if (i == 0)
                tPairs[i] = t.charAt(i) << 16;
            else if (i == m)
                tPairs[i-1] |= t.charAt(i);
            else
                tPairs[i] = (tPairs[i-1] |= t.charAt(i)) << 16;

        // Sort the bigram lists:
        Arrays.sort(sPairs);
        Arrays.sort(tPairs);

        // Count the matches:
        int matches = 0, i = 0, j = 0;
        while (i < n && j < m)
        {
            if (sPairs[i] == tPairs[j])
            {
                matches += 2;
                i++;
                j++;
            }
            else if (sPairs[i] < tPairs[j])
                i++;
            else
                j++;
        }
        return (float)matches/(n+m);
    }

    @Override
    public float getUnNormalisedSimilarity(String s, String s2) {
        return 0;
    }
}
