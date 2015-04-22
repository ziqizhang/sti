/*
package uk.ac.shef.oak.any23.xpath;


import java.io.IOException;

import org.apache.lucene.analysis.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public final class LengthFilter extends FilteringTokenFilter {

  private final int min;
  private final int max;
  
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

  */
/**
   * Build a filter that removes words that are too long or too
   * short from the text.
   *//*

  public LengthFilter(boolean enablePositionIncrements, TokenStream in, int min, int max) {
    super(enablePositionIncrements, in);
    this.min = min;
    this.max = max;
  }
  
  */
/**
   * Build a filter that removes words that are too long or too
   * short from the text.
   * @deprecated Use {@link #LengthFilter(boolean, TokenStream, int, int)} instead.
   *//*

  @Deprecated
  public LengthFilter(TokenStream in, int min, int max) {
    this(false, in, min, max);
  }

  @Override
  public boolean accept() throws IOException {
    final int len = termAtt.length();
    return (len >= min && len <= max);
  }
}
*/
