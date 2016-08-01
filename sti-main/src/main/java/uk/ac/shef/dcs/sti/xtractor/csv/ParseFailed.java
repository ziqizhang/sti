package uk.ac.shef.dcs.sti.xtractor.csv;

/**
 *
 * @author Škoda Petr
 */
public class ParseFailed extends Exception {

    public ParseFailed(String message) {
        super(message);
    }

    public ParseFailed(String message, Throwable cause) {
        super(message, cause);
    }

}
