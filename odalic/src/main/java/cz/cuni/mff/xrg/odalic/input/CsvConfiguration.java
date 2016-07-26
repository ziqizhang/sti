package cz.cuni.mff.xrg.odalic.input;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.apache.commons.csv.CSVFormat;

import com.google.common.base.Preconditions;

/**
 * Configuration of the CSV file for the parser.
 * 
 * @author Jan Váňa
 * @author Václav Brodec
 */
@Immutable
public final class CsvConfiguration implements Serializable {

  private static final long serialVersionUID = 5335583951933923025L;

  private final Charset charset;
  private final char delimiter;
  private final boolean headerPresent;
  private final boolean emptyLinesIgnored;
  private final boolean headerCaseIgnored;
  private final Character quoteCharacter;
  private final Character escapeCharacter;
  private final Character commentMarker;


  public CsvConfiguration(Charset charset, char delimiter, boolean headerPresent,
      boolean emptyLinesIgnored, boolean headerCaseIgnored, @Nullable Character quoteCharacter,
      @Nullable Character escapeCharacter, @Nullable Character commentMarker) {
    Preconditions.checkNotNull(charset);

    this.charset = charset;
    this.delimiter = delimiter;
    this.headerPresent = headerPresent;
    this.emptyLinesIgnored = emptyLinesIgnored;
    this.headerCaseIgnored = headerCaseIgnored;
    this.quoteCharacter = quoteCharacter;
    this.escapeCharacter = escapeCharacter;
    this.commentMarker = commentMarker;
  }


  public CsvConfiguration() {
    charset = StandardCharsets.UTF_8;
    delimiter = ';';
    headerPresent = true;
    emptyLinesIgnored = true;
    headerCaseIgnored = false;
    quoteCharacter = null;
    escapeCharacter = null;
    commentMarker = null;
  }


  /**
   * @return the character set
   */
  public Charset getCharset() {
    return charset;
  }


  /**
   * @return the delimiter
   */
  public char getDelimiter() {
    return delimiter;
  }


  /**
   * @return the header present
   */
  public boolean isHeaderPresent() {
    return headerPresent;
  }


  /**
   * @return the empty lines ignored
   */
  public boolean isEmptyLinesIgnored() {
    return emptyLinesIgnored;
  }


  /**
   * @return the header case ignored
   */
  public boolean isHeaderCaseIgnored() {
    return headerCaseIgnored;
  }


  /**
   * @return the quote character
   */
  @Nullable
  public Character getQuoteCharacter() {
    return quoteCharacter;
  }


  /**
   * @return the escape character
   */
  @Nullable
  public Character getEscapeCharacter() {
    return escapeCharacter;
  }


  /**
   * @return the comment marker
   */
  @Nullable
  public Character getCommentMarker() {
    return commentMarker;
  }


  CSVFormat toApacheConfiguration() {
    CSVFormat format = CSVFormat.newFormat(delimiter).withAllowMissingColumnNames()
        .withIgnoreEmptyLines(emptyLinesIgnored).withIgnoreHeaderCase(headerCaseIgnored);

    if (quoteCharacter != null) {
      format = format.withQuote(quoteCharacter);
    }

    if (headerPresent) {
      format = format.withHeader();
    }

    if (escapeCharacter != null) {
      format = format.withEscape(escapeCharacter);
    }

    if (commentMarker != null) {
      format = format.withCommentMarker(commentMarker);
    }

    return format;
  }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((charset == null) ? 0 : charset.hashCode());
    result = prime * result + ((commentMarker == null) ? 0 : commentMarker.hashCode());
    result = prime * result + delimiter;
    result = prime * result + (emptyLinesIgnored ? 1231 : 1237);
    result = prime * result + ((escapeCharacter == null) ? 0 : escapeCharacter.hashCode());
    result = prime * result + (headerCaseIgnored ? 1231 : 1237);
    result = prime * result + (headerPresent ? 1231 : 1237);
    result = prime * result + ((quoteCharacter == null) ? 0 : quoteCharacter.hashCode());
    return result;
  }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CsvConfiguration other = (CsvConfiguration) obj;
    if (charset == null) {
      if (other.charset != null) {
        return false;
      }
    } else if (!charset.equals(other.charset)) {
      return false;
    }
    if (commentMarker == null) {
      if (other.commentMarker != null) {
        return false;
      }
    } else if (!commentMarker.equals(other.commentMarker)) {
      return false;
    }
    if (delimiter != other.delimiter) {
      return false;
    }
    if (emptyLinesIgnored != other.emptyLinesIgnored) {
      return false;
    }
    if (escapeCharacter == null) {
      if (other.escapeCharacter != null) {
        return false;
      }
    } else if (!escapeCharacter.equals(other.escapeCharacter)) {
      return false;
    }
    if (headerCaseIgnored != other.headerCaseIgnored) {
      return false;
    }
    if (headerPresent != other.headerPresent) {
      return false;
    }
    if (quoteCharacter == null) {
      if (other.quoteCharacter != null) {
        return false;
      }
    } else if (!quoteCharacter.equals(other.quoteCharacter)) {
      return false;
    }
    return true;
  }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CsvConfiguration [charset=" + charset + ", delimiter=" + delimiter + ", headerPresent="
        + headerPresent + ", emptyLinesIgnored=" + emptyLinesIgnored + ", headerCaseIgnored="
        + headerCaseIgnored + ", quoteCharacter=" + quoteCharacter + ", escapeCharacter="
        + escapeCharacter + ", commentMarker=" + commentMarker + "]";
  }
}
