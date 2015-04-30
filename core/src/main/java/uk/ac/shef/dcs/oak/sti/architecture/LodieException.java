package uk.ac.shef.dcs.oak.sti.architecture;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 22/10/12
 * Time: 12:42
 */
public class LodieException extends Exception {
    public LodieException(String s, Exception e) {
        super(s, e);
    }

    public LodieException(String s) {
        super(s);
    }

}
