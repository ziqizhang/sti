package cz.cuni.mff.xrg.odalic.feedbacks.input;

import org.apache.commons.csv.CSVFormat;

/**
 * Created by Jan on 17.07.2016.
 */
public class CsvConfiguration {
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public boolean isIgnoreEmptyLines() {
        return ignoreEmptyLines;
    }

    public void setIgnoreEmptyLines(boolean ignoreEmptyLines) {
        this.ignoreEmptyLines = ignoreEmptyLines;
    }

    public boolean isIgnoreHeaderCase() {
        return ignoreHeaderCase;
    }

    public void setIgnoreHeaderCase(boolean ignoreHeaderCase) {
        this.ignoreHeaderCase = ignoreHeaderCase;
    }

    public Character getQuoteCharacter() {
        return quoteCharacter;
    }

    public void setQuoteCharacter(Character quoteCharacter) {
        this.quoteCharacter = quoteCharacter;
    }

    public Character getEscapeCharacter() {
        return escapeCharacter;
    }

    public void setEscapeCharacter(Character escapeCharacter) {
        this.escapeCharacter = escapeCharacter;
    }

    public Character getCommentMarker() {
        return commentMarker;
    }

    public void setCommentMarker(Character commentMarker) {
        this.commentMarker = commentMarker;
    }

    private String encoding;
    private char delimiter;
    private boolean hasHeader;
    private boolean ignoreEmptyLines;
    private boolean ignoreHeaderCase;
    private Character quoteCharacter;
    private Character escapeCharacter;
    private Character commentMarker;

    public CsvConfiguration() {
        delimiter = ';';
        hasHeader = true;
        ignoreHeaderCase = false;
        ignoreEmptyLines = true;
        encoding = "UTF-8";
    }

    CSVFormat toApacheConfiguration() {
        CSVFormat format = CSVFormat
                .newFormat(delimiter)
                .withAllowMissingColumnNames()
                .withIgnoreEmptyLines(ignoreEmptyLines)
                .withIgnoreHeaderCase(ignoreHeaderCase);

        if (quoteCharacter != null) {
            format = format.withQuote(quoteCharacter);
        }

        if (hasHeader) {
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
}
