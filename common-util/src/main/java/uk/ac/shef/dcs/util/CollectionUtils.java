package uk.ac.shef.dcs.util;

import javafx.util.Pair;

import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/05/13
 * Time: 16:38
 */
public class CollectionUtils {

    public static double dice(String string1, String string2)
    {
        string1=string1.toLowerCase();
        string2=string2.toLowerCase();

        List<char[]> bigram1=bigram(string1);
        List<char[]> bigram2=bigram(string2);
        List<char[]> copy = new ArrayList<char[]>(bigram2);
        int matches = 0;
        for (int i = bigram1.size(); --i >= 0;)
        {
            char[] bigram = bigram1.get(i);
            for (int j = copy.size(); --j >= 0;)
            {
                char[] toMatch = copy.get(j);
                if (bigram[0] == toMatch[0] && bigram[1] == toMatch[1])
                {
                    copy.remove(j);
                    matches += 2;
                    break;
                }
            }
        }
        return (double) matches / (bigram1.size() + bigram2.size());
    }

    private static List<char[]> bigram(String input)
    {
        ArrayList<char[]> bigram = new ArrayList<char[]>();
        for (int i = 0; i < input.length() - 1; i++)
        {
            char[] chars = new char[2];
            chars[0] = input.charAt(i);
            chars[1] = input.charAt(i+1);
            bigram.add(chars);
        }
        return bigram;
    }

    public static double diceCoefficientOptimized(String s, String t)
    {
        s=s.toLowerCase();
        t=t.toLowerCase();
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
        return (double)matches/(n+m);
    }

    public static boolean array_list_contains(List<String[]> parents, String[] child) {
        for (String[] p : parents) {
            if (p.length == child.length) {
                boolean equals = true;
                //look at every position in the array
                int allEqual=0;
                for (int i = 0; i < p.length; i++) {
                    if (p[i] == null && child[i] == null) {
                        allEqual++;
                    } else if (p[i] != null && child[i] != null && p[i].equals(child[i])) {
                        allEqual++;
                    }
                }
                if (allEqual==p.length)
                    return true;
            }
        }
        return false;
    }


    public static int intersection(List<String> l1, List<String> l2) {
        List<String> tmp = new ArrayList<String>();
        if (l1.size() < l2.size()) {
            tmp.addAll(l1);
            tmp.retainAll(l2);
        } else {
            tmp.addAll(l2);
            tmp.retainAll(l1);
        }
        return tmp.size();
    }

    public static int union(List<String> l1, List<String> l2) {
        Set<String> tmp = new HashSet<String>();
        tmp.addAll(l1);
        tmp.addAll(l2);
        return tmp.size();
    }

    public static double scoreOverlap_jaccard_keepFrequency(Collection<String> list1, Collection<String> list2) {
        List<String> union = new ArrayList<String>();
        union.addAll(list1);
        union.addAll(list2);

        List<String> intersection = new ArrayList<String>(union);
        intersection.retainAll(list1);
        intersection.retainAll(list2);

        if (intersection.size() == 0)
            return 0.0;
        double score = (double) intersection.size() / union.size();
        return score;

    }

    public static double scoreOverlap_jaccard(Collection<String> c1, Collection<String> c2) {
        Set<String> union = new HashSet<String>();
        union.addAll(c1);
        union.addAll(c2);

        Set<String> intersection = new HashSet<String>(c1);
        intersection.retainAll(c1);
        intersection.retainAll(c2);

        if (intersection.size() == 0)
            return 0.0;
        double score = (double) intersection.size() / union.size();
        return score;

    }

    public static double scoreOverlap_weight(Collection<String> c1, Collection<String> c2) {
        Set<String> intersection = new HashSet<String>(c1);
        List<String> small = new ArrayList<String>();
        List<String> large = new ArrayList<String>();

        if (c1.size() > c2.size()) {
            small.addAll(c1);
            large.addAll(c2);
        } else {
            small.addAll(c2);
            large.addAll(c1);
        }
        int smallSize = small.size();
        int largeSize = large.size();

        small.retainAll(c1);
        small.retainAll(c2);
        large.retainAll(c1);
        large.retainAll(c2);

        double score = (double) small.size() / smallSize + (double) large.size() / largeSize;
        return score/*/2.0*/;
    }

    public static double scoreOverlap_dice(Collection<String> c1, Collection<String> c2) {
        Set<String> intersection = new HashSet<String>(c1);
        intersection.retainAll(c1);
        intersection.retainAll(c2);

        if (intersection.size() == 0)
            return 0.0;
        double score = 2 * (double) intersection.size() / (c1.size() + c2.size());
        return score;

    }

    public static double scoreOverlap_dice_keepFrequency(Collection<String> c1, Collection<String> c2) {
        List<String> union = new ArrayList<String>();
        union.addAll(c1);
        union.addAll(c2);

        List<String> intersection = new ArrayList<String>(union);
        intersection.retainAll(c1);
        intersection.retainAll(c2);

        if (intersection.size() == 0)
            return 0.0;
        double score = 2 * (double) intersection.size() / (c1.size() + c2.size());
        return score;

    }

    public static double scoreOverlap_cosine(Collection<String> c1, Collection<String> c2) {
        Set<String> intersection = new HashSet<String>(c1);
        intersection.retainAll(c1);
        intersection.retainAll(c2);

        if (intersection.size() == 0)
            return 0.0;
        double score = (double) intersection.size() / Math.sqrt((double) (c1.size() * c2.size()));
        return score;

    }
                                                   //entity         //context
    public static double scoreCoverage_against_b(List<String> a, List<String> b) {
        List<String> c = new ArrayList<String>(b);

        c.retainAll(a);
        if(c.size()==0)
            return 0.0;
        double score = (double) c.size() / b.size();
        return score;
    }

    public static boolean containsPair(List<Pair<String, String>> entities_on_the_row, Pair<String, String> toAdd) {
        for(Pair<String, String> entry: entities_on_the_row){
            if(entry.getKey().equals(toAdd.getKey())&&entry.getValue().equals(toAdd.getValue()))
                return true;
        }
        return false;  //To change body of created methods use File | Settings | File Templates.
    }
}
