/*
package uk.ac.shef.oak.any23.xpath;


import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;


public class TokenizeAndStopwordAnalyzer extends ReusableAnalyzerBase {

  private static Version matchVersion;
//  private Analyzer stopAnalyzer;

  

  static final   Set<String> stopWords = new HashSet<String>();
  static{
	  //TODO use customized stopwords
  for (Object s : StandardAnalyzer.STOP_WORDS_SET){
  	stopWords.add(String.valueOf((char[])s));
  }
  
  System.out.println(stopWords);
  
  
  }
  
  public TokenizeAndStopwordAnalyzer(Version matchVersion) throws IOException {
    this.matchVersion = matchVersion;
//	this.stopAnalyzer= new StopAnalyzer(Version.LUCENE_36);
   
  }

  public TokenizeAndStopwordAnalyzer(Version matchVersion, File file) throws IOException {
	    this.matchVersion = matchVersion;
//		this.stopAnalyzer= new StopAnalyzer(Version.LUCENE_36, file);

	    
}

@Override
  protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
    final Tokenizer source = new WhitespaceTokenizer(matchVersion, reader);


    TokenStream  result = new StopFilter(matchVersion, source, this.stopWords);
    
    result= new LengthFilter(result, 3, Integer.MAX_VALUE);

//    result = this.stopAnalyzer.tokenStream(null, reader);
    return new TokenStreamComponents(source, result);
  }
  


public static List<String> tokenizeString(String string) throws IOException {
    List<String> result = new ArrayList<String>();
    if (matchVersion==null){
    	matchVersion= Version.LUCENE_CURRENT;
    }
    TokenizeAndStopwordAnalyzer analyzer = new TokenizeAndStopwordAnalyzer(matchVersion);
    TokenStream stream = analyzer.tokenStream(null, new StringReader(string));
    // get the CharTermAttribute from the TokenStream
    CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
    try {
        stream.reset();
        // print all tokens until stream is exhausted
        while (stream.incrementToken()) {
            result.add(stream.getAttribute(CharTermAttribute.class).toString());

//        System.out.println(termAtt.toString());
        }
        stream.end();
    } finally {
        stream.close();
    }

    return result;
}

  
  public static void main(String[] args) throws IOException {
    // text to tokenize
    final String text = "This is a demo of the TokenStream API";
    
    Version matchVersion = Version.LUCENE_36;
//    TokenizeAndStopwordAnalyzer analyzer = new TokenizeAndStopwordAnalyzer(matchVersion, new File("./resources/TwitterStopword.txt"));
    TokenizeAndStopwordAnalyzer analyzer = new TokenizeAndStopwordAnalyzer(matchVersion);
    TokenStream stream = analyzer.tokenStream(null, new StringReader(text));
    
    // get the CharTermAttribute from the TokenStream
    CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);

    try {
      stream.reset();
    
      // print all tokens until stream is exhausted
      while (stream.incrementToken()) {
        System.out.println(termAtt.toString());
      }
    
      stream.end();
    } finally {
      stream.close();
    }
  }
}*/
