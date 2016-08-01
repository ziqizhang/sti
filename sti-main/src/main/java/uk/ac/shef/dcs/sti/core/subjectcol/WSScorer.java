package uk.ac.shef.dcs.sti.core.subjectcol;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import uk.ac.shef.dcs.sti.nlp.TermFreqCounter;
import uk.ac.shef.dcs.util.SolrCache;
import uk.ac.shef.dcs.websearch.WebSearch;
import uk.ac.shef.dcs.websearch.bing.v2.APIKeysDepletedException;
import uk.ac.shef.dcs.websearch.WebSearchResultDoc;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 */
public class WSScorer {
    protected SolrCache cache;
    protected WebSearch searcher;
    protected static final double TITLE_WEIGHT_SCALAR = 2.0; //if a value is found in title of a search result document, it receives higher weight
    protected static final boolean WORD_ORDER_MATTERS = false;
    protected TermFreqCounter counter = new TermFreqCounter();
    protected List<String> stopWords;

    protected static final Logger LOG = Logger.getLogger(WSScorer.class.getName());


    public WSScorer(SolrCache cache, WebSearch searcher,
                    List<String> stopWords) {
        this.cache = cache;
        this.searcher = searcher;
        this.stopWords = stopWords;
    }

    @SuppressWarnings("unchecked")
    protected List<WebSearchResultDoc> findInCache(String queryId) throws IOException, ClassNotFoundException, SolrServerException {
        return (List<WebSearchResultDoc>)cache.retrieve(queryId);
    }

    /**
     * Score each string value using WS
     *
     * @param values
     * @return
     * @throws APIKeysDepletedException
     * @throws IOException
     */
    public Map<String, Double> score(String... values) throws APIKeysDepletedException, IOException {
        String queryId = createSolrCacheQuery(values);
        //1. check cache
        List<WebSearchResultDoc> result = null;
        try {
            result = findInCache(queryId);
        } catch (Exception e) {
            // e.printStackTrace();
        }

        //2. if not in cache, perform web search, computeElementScores results, and cache results
        if (result == null/*||result.size()==0*/) {
            Date start = new Date();
            try {
                InputStream is = searcher.search(queryId);
                List<WebSearchResultDoc> searchResult = searcher.getResultParser().parse(is);
                result = searchResult == null ? new ArrayList<>() : searchResult;

                cache.cache(queryId, result, true);
            } catch (Exception e) {
                LOG.warn("Caching Web Search Results failed: " + e);
            }
            LOG.debug("\tQueryBing:" + (new Date().getTime() - start.getTime()));
        }

        return score(result, values);
    }

    //take each normlised value, computeElementScores it against each search result document and sum up the scores
    protected Map<String, Double> score(List<WebSearchResultDoc> searchResult, String... normalizedValues) {
        Map<String, Double> scores = new HashMap<>();

        for (WebSearchResultDoc doc : searchResult) {
            String title = doc.getTitle();
            score(scores, title, TITLE_WEIGHT_SCALAR, normalizedValues);
            String desc = doc.getDescription();
            score(scores, desc, 1.0, normalizedValues);
        }

        return scores;
    }



    private void score(Map<String, Double> scores, String context, double contextWeightScalar, String... normalizedValues) {
        context = normalize(context);

        Map<String, Set<Integer>> offsets = new HashMap<>();
        for (String v : normalizedValues) {
            //v=normalize(v);
            if (v.length() < 1) continue; //isValidAttribute 1 char tokens
            Set<Integer> os = counter.countOffsets(v, context);
            offsets.put(v, os);
        }

        Map<String, Set<String>> phrase_to_composing_tokens = new HashMap<>();
        Map<String, Set<Integer>> composing_token_offsets = new HashMap<>();
        //then the composing tokens for each normalizedValue
        for (String v : normalizedValues) {
            String[] toks = v.split("\\s+");
            if (toks.length == 1)
                continue;
            Set<String> composing_toks = new HashSet<>();
            for (String t : toks) {
                //t = t.trim();
                if (ignore(t)) continue;
                composing_toks.add(t);
                if (offsets.get(t) != null) {
                    composing_token_offsets.put(t, offsets.get(t));
                } else if (composing_token_offsets.get(t) == null) {
                    Set<Integer> os = counter.countOffsets(t, context);
                    composing_token_offsets.put(t, os);
                }
            }
            phrase_to_composing_tokens.put(v, composing_toks);
        }
        //discount double counting due to string inclusion between cells
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
        //discount double counting due to string inclusion between a cell's value and its composing tokens
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
        final Map<String, Integer> firstOccurrenceOfPhrase = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : offsets.entrySet()) {
            if (entry.getValue().size() == 0)
                continue;
            int min = Integer.MAX_VALUE;
            for (Integer i : entry.getValue()) {
                if (i < min)
                    min = i;
            }
            firstOccurrenceOfPhrase.put(entry.getKey(), min);
        }
        final Map<String, Integer> firstOccurrenceOfComposingTokens = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : composing_token_offsets.entrySet()) {
            if (entry.getValue().size() == 0)
                continue;
            int min = Integer.MAX_VALUE;
            for (Integer i : entry.getValue()) {
                if (i < min)
                    min = i;
            }
            firstOccurrenceOfComposingTokens.put(entry.getKey(), min);
        }

        List<String> original_phrases = new ArrayList<>(firstOccurrenceOfPhrase.keySet());
        Collections.sort(original_phrases, (o1, o2)
                -> new Integer(firstOccurrenceOfPhrase.get(o1)).compareTo(firstOccurrenceOfPhrase.get(o2)));

        List<String> composing_tokens = new ArrayList<>(firstOccurrenceOfComposingTokens.keySet());
        Collections.sort(composing_tokens, (o1, o2)
                -> new Integer(firstOccurrenceOfComposingTokens.get(o1)).compareTo(firstOccurrenceOfComposingTokens.get(o2)));


        //last of all, compute scores
        double ordering_multiplier_original = 1.0 / original_phrases.size();
        double order_scalar_composing_tokens = 1.0 / composing_tokens.size();

        for (int o = 0; o < original_phrases.size(); o++) {
            String phrase = original_phrases.get(o);
            if (offsets.get(phrase) != null) {
                int freq = offsets.get(phrase).size();
                if (freq > 0) {  //exact cell value found
                    //double ordering_weight_multiplier = (original_phrases.size() - o) * ordering_multiplier_original;
                    double score_to_increment = offsets.get(phrase).size() * /*ordering_weight_multiplier*/1.0 * contextWeightScalar;

                    Double score = scores.get(phrase);
                    score = score == null ? 0.0 : score;
                    score = score + score_to_increment;
                    scores.put(phrase, score);
                }
            }
        }
        for (int o = 0; o < composing_tokens.size(); o++) {
            String candidate = composing_tokens.get(o);
            if (composing_token_offsets.get(candidate) != null) {
                int freq = composing_token_offsets.get(candidate).size();
                if (freq > 0) {  //exact cell value found
                    double order_weight_scalar = (composing_tokens.size() - o) * order_scalar_composing_tokens;
                    if(!WORD_ORDER_MATTERS)
                        order_weight_scalar=1.0;
                    double score_to_increment = composing_token_offsets.get(candidate).size()
                            * order_weight_scalar * contextWeightScalar;

                    //find corresponding parent cell value
                    for (Map.Entry<String, Set<String>> e : phrase_to_composing_tokens.entrySet()) {
                        if (e.getValue().contains(candidate)) {
                            score_to_increment = boostScoreByComposingTokens(
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

    private boolean ignore(String t) {
        if (stopWords.contains(t))
            return true;
        try {
            Long.valueOf(t); //isValidAttribute numbers
            return true;
        } catch (Exception e) {
        }
        return false;
    }


    private double boostScoreByComposingTokens(
            double score_to_increment_by_component,
            String original) {
        int length = original.split("\\s+").length;
        if (length < 2) return 0;
        return score_to_increment_by_component * (1.0 / (length/* * length*/));
    }


    protected String normalize(String value) {
        return value.replaceAll("[^\\p{L}\\s\\d]", " ").replaceAll("\\s+", " ").trim().toLowerCase();
    }

    //remove short phrase's start offsets if it is part of a longer phrase
    protected void removeOverlapOffsets(Set<Integer> shorterPhraseStarts, Set<Integer> longerPhraseStarts, int longerPhraseLength) {
        Iterator<Integer> it = shorterPhraseStarts.iterator();
        while (it.hasNext()) {
            Integer s = it.next();
            for (int st : longerPhraseStarts) {
                if (st <= s && s < st + longerPhraseLength) { //the shorter phrase's start is included in the longer phrase boundary
                    it.remove();
                    break;
                }
            }
        }
    }

    protected String createSolrCacheQuery(String... values) {
        StringBuilder sb = new StringBuilder();
        for (String v : values)
            sb.append(v).append(" ");
        return sb.toString().trim();
    }


   /* public static void main(String[] args) throws APIKeysDepletedException, IOException {
        String[] accountKeys = new String[]{"fXhmgvVQnz1aLBti87+AZlPYDXcQL0G9L2dVAav+aK0="};
        WSScorer matcher = new WSScorer(
                new HeaderWebsearchMatcherCache("D:\\Work\\lodiedata\\tableminer_cache\\solrindex_cache\\zookeeper\\solr",
                        "collection1"),
                new BingSearch(accountKeys),
                new ArrayList<String>()
        );
        matcher.computeElementScores("House of Cards", "Peter David");
        matcher.computeElementScores("University of Sheffield", "Sheffield", "United Kingdom");
        matcher.computeElementScores("House of Cards", "Peter David");
        matcher.cache.closeConnection();
    }*/

}
