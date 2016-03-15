package uk.ac.shef.dcs.sti.algorithm.tm.maincol;

import javafx.util.Pair;
import uk.ac.shef.dcs.sti.nlp.Lemmatizer;
import uk.ac.shef.dcs.sti.nlp.NLPTools;
import uk.ac.shef.dcs.sti.rep.LTable;
import uk.ac.shef.dcs.sti.rep.LTableContext;
import uk.ac.shef.dcs.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * produces matching statistics of a table's header and its contexts. can only match headers that are NOT STOPWORDS
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * the scoring algorithm is implemented in this class
 */
class HeaderAndContextMatcher {

    private double minimum_context_score = 0.5;
    private int maximum_context_to_match = 5; //except title, caption, match a maximum of 5 context blocks around the table
    private double major_context_weight_multiplier = 2.0; //major context blocks, i.e., titles, captions, plural word matches
    // are considered more important. if matched, their scores is multiplied by this factor
    private Lemmatizer lemmatizer;
    private List<String> stopwords;

    public HeaderAndContextMatcher(String nlpResource) throws IOException {
        this.lemmatizer = NLPTools.getInstance(nlpResource).getLemmatizer();
        try {
            stopwords = FileUtils.readList(nlpResource + File.separator + "stoplist.txt", false);
        } catch (IOException e) {
            stopwords = new ArrayList<>();
        }

    }

    //returns a map between column index and matching score
    public Map<Integer, Double> match(LTable table, int... col_indexes) {
        Map<Integer, Double> scores = new HashMap<Integer, Double>();

        //process headers to match against
        Map<Integer, List<String>> headerKeywords = new HashMap<Integer, List<String>>();
        for (int col_id : col_indexes) {
            List<String> searchWords = new ArrayList<String>();

            String keyword = table.getColumnHeader(col_id).getHeaderText();
            //searchWords.add(lemmatizer.getLemma(keyword, "NN"));

            //then add capitalised, noun-stop words
            String[] candidates = keyword.split("\\s+");
            for (int index = 0; index < candidates.length; index++) {
                String w = candidates[index];
                /*if (StringUtils.isCapitalized(w)) {
                    w = lemmatizer.getLemma(w, "NN");
                    if (!stopwords.contains(w))
                        searchWords.add(w);
                }*/
                //if (StringUtils.isCapitalized(w)) {
                    w = lemmatizer.getLemma(w, "NN");
                    if (!stopwords.contains(w))
                        searchWords.add(w);
                //}
            }
            headerKeywords.put(col_id, searchWords);
        }

        //List<String> stop = new ArrayList<String>(stopwords);
        //stop.removeAll(headerKeywords.values());

        //process contexts to generate word lookup maps
        List<LTableContext> contexts = table.getContexts();
        Collections.sort(contexts);

        int countContextBlocks = 0;
        double score = 0.0;
        for (LTableContext ctx : contexts) {
            if (countContextBlocks == maximum_context_to_match)
                break;
            if (ctx.getRankScore() < minimum_context_score)
                continue;

            //collect distinct words from this context, their frequency, and plural form frequency
            Map<String, Pair<Integer, Integer>> wordFreq = new
                    HashMap<>();
            StringTokenizer tokenizer = new StringTokenizer(ctx.getText());
            while (tokenizer.hasMoreTokens()) {
                String tok = tokenizer.nextToken();
                String canonical = lemmatizer.getLemma(tok, "NN");

                Pair<Integer, Integer> countings = wordFreq.get(canonical);
                if (countings == null) {
                    countings = new Pair<>(0,0);
                }
                int k = countings.getKey() + 1;
                int v = countings.getValue();
                if (!tok.toLowerCase().equals(canonical))
                    v=v + 1;
                countings = new Pair<>(k,v);

                wordFreq.put(canonical, countings);
            }

            //compute matching scores for each header keyword against this context block
            for (Map.Entry<Integer, List<String>> headerKey : headerKeywords.entrySet()) {
                List<String> words = headerKey.getValue();
                if (words == null) continue;

                //firstly lets try the full header text, i.e., element 1 in "words"
                for (String word : words) {
                    Pair<Integer, Integer> freq = wordFreq.get(word);
                    if (freq == null)
                        continue;

                    score = score + freq.getKey(); //if header keyword matches this word, its score is incremented by its frequency
                    score = score + freq.getValue();//if the matched word is plural, the score is further modified
                    if (ctx.getType().equals(LTableContext.TableContextType.CAPTION)
                            || ctx.getText().equals(LTableContext.TableContextType.PAGETITLE)) {
                        score = score * major_context_weight_multiplier;
                    }
                    Double prevScore = scores.get(headerKey.getKey());
                    prevScore = prevScore == null ? 0 : prevScore;
                    prevScore = prevScore + score;
                    scores.put(headerKey.getKey(), prevScore);
                }
            }

            //special context blocks boost the scores
            if (ctx.getType().equals(LTableContext.TableContextType.CAPTION)
                    || ctx.getText().equals(LTableContext.TableContextType.PAGETITLE)) {
            } else {
                countContextBlocks++;
            }
        }

        return scores;
    }

    public double getMinimum_context_score() {
        return minimum_context_score;
    }

    public void setMinimum_context_score(double minimum_context_score) {
        this.minimum_context_score = minimum_context_score;
    }

    public int getMaximum_context_to_match() {
        return maximum_context_to_match;
    }

    public void setMaximum_context_to_match(int maximum_context_to_match) {
        this.maximum_context_to_match = maximum_context_to_match;
    }

    public double getMajor_context_weight_multiplier() {
        return major_context_weight_multiplier;
    }

    public void setMajor_context_weight_multiplier(double major_context_weight_multiplier) {
        this.major_context_weight_multiplier = major_context_weight_multiplier;
    }
}
