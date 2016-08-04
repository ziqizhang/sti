package uk.ac.shef.dcs.sti.parser;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/10/12
 * Time: 15:50
 */
public abstract class ContentValidator {

    protected static final String[] ORDINALS = {"first", "1st", "second", "2nd", "third", "3rd", "fourth", "4th",
            "fifth", "5th", "sixth", "6th", "seventh", "7th", "eighth", "8th", "ninth", "9th", "tenth", "10th",
            "eleventh", "11th", "twelfth", "12th", "teenth", "13th", "tieth"};


    /**
     * @param tc
     * @return true if phrase contains >50% numeric tokens (including ordinals)
     */
    public static boolean isNumericContent(String tc) {
        String[] parts = tc.toLowerCase().split("\\s+");
        int countNumericParts = 0;
        for (String p : parts) {
            boolean isPartNumeric = false;
            for (String ord : ORDINALS) {
                if (p.endsWith(ord)) {
                    countNumericParts++;
                    isPartNumeric = true;
                    break;
                }
            }
            if (!isPartNumeric) {
                boolean skip = false;
                int countDigits = 0;
                for (int i = 0; i < p.length(); i++) {
                    if (Character.isDigit(tc.charAt(i))) {
                        countDigits++;
                    }
                    if (Character.isLetter(tc.charAt(i))) {
                        skip = true;
                        break;
                    }
                }
                if (!skip && countDigits > tc.length() * 0.5)
                    countNumericParts++;
            }
        }

        return countNumericParts > parts.length * 0.5;
    }

    public static boolean isEmptyString(String string) {
        string = string.replaceAll("[^a-zA-Z0-9]", ""); //only consider english
        return string.length() == 0;
    }

    public static boolean isEmptyMediaWikiString(String string) {
        if (string.startsWith("{{") && string.endsWith("}}"))
            return true;
        return isEmptyString(string);
    }

    public static boolean isWikiInternalLink(String uri) {
        if (uri == null)
            return false;
        if (uri.startsWith("/")) //pointing to a wikipedia article
            return true;
        else if (uri.startsWith("http") || uri.startsWith("www")) {
            if (uri.indexOf("en.wikipedia") != -1)
                return true;
            return false;
        }
        return false;
    }


}
