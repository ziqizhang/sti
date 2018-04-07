package uk.ac.shef.dcs.sti.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 31/10/12
 * Time: 21:29
 */
public class XPathUtils {

    /**
     * @param tag   e.g., tr
     * @param xpath e.g., /html/body/table[1]/tr[2]/td[2]
     * @return /html/body/table[1]/tr[2]
     */
    public static String trimXPathLastTag(String tag, String xpath) {
        int lastTagIndex = xpath.lastIndexOf(tag);
        if (lastTagIndex != -1) {
            int end = xpath.indexOf("/", lastTagIndex);
            if (end == -1) {
                return xpath;
            }
            return xpath.substring(0, end);
        }
        return null;
    }


}
