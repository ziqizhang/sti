package uk.ac.shef.dcs.sti.algorithm.tm.maincol;

import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.shef.dcs.sti.nlp.TermFreqCounter;
import uk.ac.shef.dcs.websearch.bing.v2.APIKeysDepletedException;
import uk.ac.shef.dcs.websearch.bing.v2.BingWebSearch;
import uk.ac.shef.dcs.websearch.bing.v2.BingWebSearchResultParser;
import uk.ac.shef.dcs.websearch.bing.v2.WebSearchResultDoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

/**
 */
public class HeaderWebsearchMatcher_token {
    protected HeaderWebsearchMatcherCache cache;
    protected BingWebSearch searcher;
    protected BingWebSearchResultParser resultParser;
    protected static double TITLE_MULTIPLIER = 2.0; //if a value is found in title of a search result document, it receives higher weight
    protected TermFreqCounter counter = new TermFreqCounter();
    protected List<String> stopWords;

    protected static Logger log = Logger.getLogger(HeaderWebsearchMatcher_token.class.getName());


    public HeaderWebsearchMatcher_token(HeaderWebsearchMatcherCache cache, BingWebSearch searcher,
                                        List<String> stopWords) {
        this.cache = cache;
        this.searcher = searcher;
        resultParser = new BingWebSearchResultParser();
        this.stopWords = stopWords;
    }

    public HeaderWebsearchMatcher_token() {
    }

    protected List<WebSearchResultDoc> findInCache(String queryId) throws IOException, ClassNotFoundException, SolrServerException {
        return cache.retrieve(queryId);
    }


    public Map<String, Double> interpret(List<WebSearchResultDoc> searchResult, String... normalizedValues) {
        Map<String, Double> scores = new HashMap<String, Double>();

        for (WebSearchResultDoc doc : searchResult) {
            String title = doc.getTitle();
            interpret(scores, title, TITLE_MULTIPLIER, normalizedValues);
            String desc = doc.getDescription();
            interpret(scores, desc, 1.0, normalizedValues);
        }

        return scores;
    }

    private void interpret(Map<String, Double> scores, String context, double context_weight_multiplier, String... normalizedValues) {
        context = normalizeString(context);

        Map<String, Set<Integer>> offsets = new HashMap<String, Set<Integer>>();
        for (String v : normalizedValues) {
            //v=normalizeString(v);
            if (v.length() < 1) continue;
            Set<Integer> os = counter.countOffsets(v, context);
            offsets.put(v, os);
        }

        Map<String, Set<String>> original_to_composing_tokens = new HashMap<String, Set<String>>();
        Map<String, Set<Integer>> composing_token_offsets = new HashMap<String, Set<Integer>>();
        //then the composing tokens for each normalizedValue
        for (String v : normalizedValues) {
            String[] toks = v.split("\\s+");
            if (toks.length == 1)
                continue;
            Set<String> composing_toks = new HashSet<String>();
            for (String t : toks) {
                t = t.trim();
                if (isInvalidToken(t)) continue;
                composing_toks.add(t);
                if (offsets.get(t) != null) {
                    composing_token_offsets.put(t, offsets.get(t));
                } else if (composing_token_offsets.get(t) == null) {
                    Set<Integer> os = counter.countOffsets(t, context);
                    composing_token_offsets.put(t, os);
                }
            }
            original_to_composing_tokens.put(v, composing_toks);
        }
        //removing inclusion among cell values
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
        //removing inclusion of composing tokens of cell values
        for (String a : offsets.keySet()) {
            for (String b : composing_token_offsets.keySet()) {
                if (a.equals(b)) continue;
                Set<Integer> offsets_a = offsets.get(a);
                Set<Integer> offsets_b = composing_token_offsets.get(b);
                if (offsets_a.size() == 0 || offsets_b == null || offsets_b.size() == 0) continue;
                removeOverlapOffsets(offsets_b, offsets_a, a.length());
                //offs ets.get(b).removeAll(offsets.get(a)); //string a contains b, so freq of b should be decreased by the freq of a
            }
        }


        //second of all, understand ordering
        final Map<String, Integer> firstAppearance_original = new HashMap<String, Integer>();
        for (Map.Entry<String, Set<Integer>> entry : offsets.entrySet()) {
            if (entry.getValue().size() == 0)
                continue;
            int min = Integer.MAX_VALUE;
            for (Integer i : entry.getValue()) {
                if (i < min)
                    min = i;
            }
            firstAppearance_original.put(entry.getKey(), min);
        }
        final Map<String, Integer> firstAppearance_components = new HashMap<String, Integer>();
        for (Map.Entry<String, Set<Integer>> entry : composing_token_offsets.entrySet()) {
            if (entry.getValue().size() == 0)
                continue;
            int min = Integer.MAX_VALUE;
            for (Integer i : entry.getValue()) {
                if (i < min)
                    min = i;
            }
            firstAppearance_components.put(entry.getKey(), min);
        }

        List<String> candidates_original = new ArrayList<String>(firstAppearance_original.keySet());
        Collections.sort(candidates_original, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return new Integer(firstAppearance_original.get(o1)).compareTo(firstAppearance_original.get(o2));
            }
        });

        List<String> candidates_components = new ArrayList<String>(firstAppearance_components.keySet());
        Collections.sort(candidates_components, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return new Integer(firstAppearance_components.get(o1)).compareTo(firstAppearance_components.get(o2));
            }
        });


        //last of all, compute scores
        double ordering_multiplier_original = 1.0 / candidates_original.size();
        double ordering_multiplier_components = 1.0 / candidates_components.size();

        for (int o = 0; o < candidates_original.size(); o++) {
            String candidate = candidates_original.get(o);
            if (offsets.get(candidate) != null) {
                int freq = offsets.get(candidate).size();
                if (freq > 0) {  //exact cell value found
                    double ordering_weight_multiplier = (candidates_original.size() - o) * ordering_multiplier_original;
                    double score_to_increment = offsets.get(candidate).size() * /*ordering_weight_multiplier*/1.0 * context_weight_multiplier;

                    Double score = scores.get(candidate);
                    score = score == null ? 0.0 : score;
                    score = score + score_to_increment;
                    scores.put(candidate, score);
                }
            }
        }
        for (int o = 0; o < candidates_components.size(); o++) {
            String candidate = candidates_components.get(o);
            if (composing_token_offsets.get(candidate) != null) {
                int freq = composing_token_offsets.get(candidate).size();
                if (freq > 0) {  //exact cell value found
                    double ordering_weight_multiplier = (candidates_components.size() - o) * ordering_multiplier_components;
                    if(!MainColumnFinder.use_ordering)
                        ordering_weight_multiplier=1.0;
                    double score_to_increment = composing_token_offsets.get(candidate).size()
                            * ordering_weight_multiplier * context_weight_multiplier;

                    //find corresponding parent cell value
                    for (Map.Entry<String, Set<String>> e : original_to_composing_tokens.entrySet()) {
                        if (e.getValue().contains(candidate)) {
                            score_to_increment = modify_score_by_composing_tokens(
                                    score_to_increment, e.getKey()
                            );
                            Double score = scores.get(e.getKey());
                            score = score == null ? 0.0 : score;
                            score = score + score_to_increment;
                            scores.put(e.getKey(), score);
                        }
                    }
                }
            }
        }

    }

    private boolean isInvalidToken(String t) {
        if (t.length() < 2 || stopWords.contains(t.toLowerCase()))
            return true;
        try {
            Long.valueOf(t.trim());
            return true;
        } catch (Exception e) {
        }
        return false;
    }

    /*private double modify_score_by_composing_tokens(
            double score_to_increment,
            String original,
            Map<String, Set<String>> original_to_composing_tokens,
            Map<String, Set<Integer>> composing_token_freq) {
        Set<String> composing = original_to_composing_tokens.get(original);
        int length = original.split("\\s+").length;
        if (length < 2) return score_to_increment;
        double base = score_to_increment;
        if (composing != null) {
            for (String c : composing) {
                Set<Integer> freq = composing_token_freq.get(c);
                int frequency = freq == null ? 0 : freq.size();
                base += frequency * (1.0 / (length*//* * length*//*));
            }
        }

        return base;
    }*/
    private double modify_score_by_composing_tokens(
            double score_to_increment_by_component,
            String original) {
        int length = original.split("\\s+").length;
        if (length < 2) return 0;
        return score_to_increment_by_component * (1.0 / (length/* * length*/));
    }

    public Map<String, Double> score(String... values) throws APIKeysDepletedException, IOException {
        String queryId = createQueryID(values);
        //1. check cache
        List<WebSearchResultDoc> result = null;
        try {
            result = findInCache(queryId);
        } catch (Exception e) {
            // e.printStackTrace();
        }

        //2. if not in cache, perform web search, interpret results, and cache results
        if (result == null/*||result.size()==0*/) {
            Date start = new Date();
            InputStream is = searcher.search(queryId);
            List<WebSearchResultDoc> searchResult = resultParser.parse(is);
            result = searchResult == null ? new ArrayList<WebSearchResultDoc>() : searchResult;
            try {
                cache.cache(queryId, result, true);
            } catch (SolrServerException e) {
                log.warning("Caching Web Search Results failed: " + e);
            }
            log.info("\tQueryBing:" + (new Date().getTime() - start.getTime()));
        }

        return interpret(result, values);
    }

    public String normalizeString(String value) {
        return value.replaceAll("[^\\p{L}\\s\\d]", " ").replaceAll("\\s+", " ").trim().toLowerCase();
    }

    //if a pair (start, end) where start is a value from toRemove and end=start+length, is found in removeFrom, it is removed
    //from "removeFrom"
    protected void removeOverlapOffsets(Set<Integer> removeFrom, Set<Integer> toRemove, int length) {
        Iterator<Integer> it = removeFrom.iterator();
        while (it.hasNext()) {
            Integer s = it.next();

            for (int st : toRemove) {
                if (st <= s && s < st + length) {
                    it.remove();
                    break;
                }
            }
        }
    }

    public static String createQueryID(String... values) {
        StringBuilder sb = new StringBuilder();
        for (String v : values)
            sb.append(v).append(" ");
        return sb.toString().trim();
    }


    public static void main(String[] args) throws APIKeysDepletedException, IOException {
        String[] accountKeys = new String[]{"fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0="};
        HeaderWebsearchMatcher_token matcher = new HeaderWebsearchMatcher_token(
                new HeaderWebsearchMatcherCache("D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr",
                        "collection1"),
                new BingWebSearch(accountKeys),
                new ArrayList<String>()
        );
        matcher.score("House of Cards", "Peter David");
        matcher.score("University of Sheffield", "Sheffield", "United Kingdom");
        matcher.score("House of Cards", "Peter David");
        matcher.cache.shutdown();
    }

}
