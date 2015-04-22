package uk.ac.shef.dcs.oak.triplesearch;

import java.io.UnsupportedEncodingException;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 20/09/12
 * Time: 09:59
 */
public class TripleSearchException extends Exception {
    public TripleSearchException(String s, Exception e) {
        super(s,e);
    }

    public TripleSearchException(String s) {
        super(s);
    }
}
