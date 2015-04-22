package uk.ac.shef.dcs.oak.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 07/11/12
 * Time: 10:36
 */
public class RegexUtils {
    public static final Pattern NAMESPACE = Pattern.compile("((xmlns:.*?=[\\\"].*?[\\\"])|(xmlns=['].*?[']))");

    public static List<String> findNameSpaces(String input) {
        Matcher matcher = NAMESPACE.matcher(input);
        List<String> found = new ArrayList<String>();

        while (matcher.find()) {
            try {
                String matched = input.substring(matcher.start(), matcher.end());
                matched = matched.substring(matched.indexOf("=") + 1).trim();
                int start = 0, end = matched.length();
                if (matched.startsWith("\"") || matched.startsWith("'"))
                    start = start + 1;
                if (matched.endsWith("\"") || matched.endsWith("'"))
                    end = end - 1;
                matched = matched.substring(start, end).trim();
                found.add(matched);
            } catch (IndexOutOfBoundsException ioe) {
            }
        }
        return found;
    }

}
