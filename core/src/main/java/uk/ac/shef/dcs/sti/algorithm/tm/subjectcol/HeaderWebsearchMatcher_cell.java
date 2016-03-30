package uk.ac.shef.dcs.sti.algorithm.tm.subjectcol;

import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.websearch.bing.v2.BingSearch;
import uk.ac.shef.dcs.websearch.WebSearchResultDoc;

import java.util.*;

/**
 */
public class HeaderWebsearchMatcher_cell extends WSScorer {
    public HeaderWebsearchMatcher_cell(SolrCache cache, BingSearch searcher, List<String> stopwords) {
        super(cache, searcher,stopwords);
    }

    public Map<String, Double> score(List<WebSearchResultDoc> searchResult, String... normalizedValues) {
        Map<String, Double> scores = new HashMap<String, Double>();

        for (WebSearchResultDoc doc : searchResult) {
            String title = doc.getTitle();
            score(scores, title, TITLE_WEIGHT_SCALAR, normalizedValues);
            String desc = doc.getDescription();
            score(scores, desc, 1.0, normalizedValues);
        }

        //update scores, if long terms include any short terms, long terms promoted by length_s/length_l * score_s
        List<String> keys = new ArrayList<String>(scores.keySet());
        Collections.sort(keys, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return new Integer(o1.split("\\s+").length).compareTo(o2.split("\\s+").length);
            }
        });
        int prev = 0;
        String prevTok = null;
        for (String current : keys) {
            current = current.trim();
            int toks = current.split("\\s+").length;
            if (prev == 0) {
                prev = toks;
                prevTok = current;
                continue;
            }
            if (toks > prev) {
                for (String small : keys) {
                    if (small.equals(current))
                        break;
                    List<String> currentToks = new ArrayList<String>(Arrays.asList(current.split("\\s+")));
                    List<String> smallToks = new ArrayList<String>(Arrays.asList(small.split("\\s+")));
                    smallToks.retainAll(currentToks);  //if one includes another
                    double ratio = (double) smallToks.size() / currentToks.size();
                    if (ratio > 0) {
                        double smallScore = scores.get(prevTok);
                        double add = smallScore * ratio;
                        scores.put(current, scores.get(current) + add);
                    }
                }
            }
            prevTok = current;
        }

        return scores;
    }

    private void score(Map<String, Double> scores, String context, double context_weight_multiplier, String... normalizedValues) {
        context = normalize(context);

        Map<String, Set<Integer>> offsets = new HashMap<String, Set<Integer>>();
        for (String v : normalizedValues) {
            //v=normalize(v);
            if (v.length() < 1) continue;
            Set<Integer> os = counter.countOffsets(v, context);
            offsets.put(v, os);
        }

        //first of all, understand inclusion
        for (String a : offsets.keySet()) {
            for (String b : offsets.keySet()) {
                if (a.equals(b)) continue;
                Set<Integer> offsets_a = offsets.get(a);
                Set<Integer> offsets_b = offsets.get(b);
                if (offsets_a.size() == 0 || offsets_b.size() == 0) continue;
                if (a.contains(b)) {
                    removeOverlapOffsets(offsets.get(b), offsets.get(a), a.length());
                    //offs ets.get(b).removeAll(offsets.get(a)); //string a contains b, so freq of b should be decreased by the freq of a
                } else if (b.contains(a)) {
                    removeOverlapOffsets(offsets.get(a), offsets.get(b), b.length());
                    //offsets.get(a).removeAll(offsets.get(b));
                }
            }
        }

        //second of all, understand ordering
        final Map<String, Integer> firstAppearance = new HashMap<String, Integer>();
        for (Map.Entry<String, Set<Integer>> entry : offsets.entrySet()) {
            if (entry.getValue().size() == 0)
                continue;
            int min = Integer.MAX_VALUE;
            for (Integer i : entry.getValue()) {
                if (i < min)
                    min = i;
            }
            firstAppearance.put(entry.getKey(), min);
        }
        List<String> candidates = new ArrayList<String>(firstAppearance.keySet());
        Collections.sort(candidates, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return new Integer(firstAppearance.get(o1)).compareTo(firstAppearance.get(o2));
            }
        });


        //last of all, compute scores
        double ordering_multiplier = 1.0 / candidates.size();

        for (int o = 0; o < candidates.size(); o++) {
            String candidate = candidates.get(o);
            int freq = offsets.get(candidate).size();
            if (freq == 0)
                continue;



            double ordering_weight_multiplier;
            if(!WORD_ORDER_MATTERS)
                ordering_weight_multiplier=1.0;
            double score_to_increment = offsets.get(candidate).size() * ordering_weight_multiplier * context_weight_multiplier;

            Double score = scores.get(candidate);
            score = score == null ? 0.0 : score;
            score = score + score_to_increment;
            scores.put(candidate, score);
        }

    }



    public  String createSolrCacheQuery(String... values) {
        StringBuilder sb = new StringBuilder();
        for (String v : values)
            sb.append(v).append(" ");
        return sb.toString().trim();
    }


    /*public static void main(String[] args) throws APIKeysDepletedException, IOException {
        String[] accountKeys = new String[]{"fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0="};
        WSScorer matcher = new WSScorer(
                new HeaderWebsearchMatcherCache("D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr",
                        "collection1"),
                new BingSearch(accountKeys), null
        );
        matcher.score("House of Cards", "Peter David");
        matcher.score("University of Sheffield", "Sheffield", "United Kingdom");
        matcher.score("House of Cards", "Peter David");
    }*/

}
