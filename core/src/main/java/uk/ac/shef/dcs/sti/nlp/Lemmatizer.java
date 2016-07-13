package uk.ac.shef.dcs.sti.nlp;

import dragon.nlp.tool.lemmatiser.EngLemmatiser;

import java.util.*;

/**
 */
public class Lemmatizer {

    private EngLemmatiser lemmatizer;
    private Map<String, Integer> tagLookUp = new HashMap<>();

    public Lemmatizer(String dict) {
        init(dict);
    }

    private void init(String dict) {
        lemmatizer = new EngLemmatiser(dict, false, true);
        tagLookUp.put("NN", 1);
        tagLookUp.put("NNS", 1);
        tagLookUp.put("NNP", 1);
        tagLookUp.put("NNPS", 1);
        tagLookUp.put("VB", 2);
        tagLookUp.put("VBG", 2);
        tagLookUp.put("VBD", 2);
        tagLookUp.put("VBN", 2);
        tagLookUp.put("VBP", 2);
        tagLookUp.put("VBZ", 2);
        tagLookUp.put("JJ", 3);
        tagLookUp.put("JJR", 3);
        tagLookUp.put("JJS", 3);
        tagLookUp.put("RB", 4);
        tagLookUp.put("RBR", 4);
        tagLookUp.put("RBS", 4);
    }

    public String getLemma(String value, String pos) {
        int POS = tagLookUp.get(pos);
        if (POS == 0)
            return lemmatizer.lemmatize(value);
        else
            return lemmatizer.lemmatize(value, POS);
    }

    public List<String> lemmatize(Collection<String> words){
        List<String> lemmas = new ArrayList<>();
        for(String w: words){
            String lem = getLemma(w, "NN");
            if(lem.trim().length()<1)
                continue;
            lemmas.add(lem);
        }
        return lemmas;
    }

}
