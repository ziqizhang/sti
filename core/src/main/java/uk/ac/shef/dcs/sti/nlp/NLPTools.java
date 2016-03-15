package uk.ac.shef.dcs.sti.nlp;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**

 */
public class NLPTools {
    private static NLPTools _ref;
    private POSTagger _posTagger;
    private Chunker _npChunker;
    private SentenceDetector _sentDetect;
    private Tokenizer _tokenizer;
    private Lemmatizer _lemmatizer;

    private NLPTools(String nlpResources) throws IOException {
        _lemmatizer=new Lemmatizer(nlpResources + File.separator + "lemmatizer");
        POSModel posModel = new POSModel(new FileInputStream(nlpResources+"/en-pos-maxent.bin"));
        _posTagger = new POSTaggerME(posModel);

        ChunkerModel chunkerModel = new ChunkerModel(new FileInputStream(nlpResources+"/en-chunker.bin"));
        _npChunker = new ChunkerME(chunkerModel);

        TokenizerModel tokenizerModel = new TokenizerModel(new FileInputStream(nlpResources+"/en-token.bin"));
        _tokenizer = new TokenizerME(tokenizerModel);

        SentenceModel sentModel = new SentenceModel(new FileInputStream(nlpResources+"/en-sent.bin"));
        _sentDetect = new SentenceDetectorME(sentModel);
    }

    public static NLPTools getInstance(String nlpResources) throws IOException {
        if(_ref ==null) _ref=new NLPTools(nlpResources);
        return _ref;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public POSTagger getPosTagger() {
        return _posTagger;
    }

    public Chunker getPhraseChunker() {
        return _npChunker;
    }

    public SentenceDetector getSentenceSplitter() {
        return _sentDetect;
    }

    public Tokenizer getTokeniser() {
        return _tokenizer;
    }

    public Lemmatizer getLemmatizer() {
        return _lemmatizer;
    }

    public void setLemmatizer(Lemmatizer _lemmatizer) {
        this._lemmatizer = _lemmatizer;
    }
}
