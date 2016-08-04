package uk.ac.shef.dcs.sti.util;

import uk.ac.shef.dcs.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 */
public class DataTypeClassifier implements Serializable {
    private static List<String> MONTHS = new ArrayList<String>(
            Arrays.asList("january", "jan",
                    "february", "feb",
                    "march", "mar",
                    "april", "apr",
                    "may",
                    "june", "jun",
                    "july", "jul",
                    "august", "aug",
                    "september", "sep",
                    "october", "oct",
                    "november", "nov",
                    "december", "dec"));
    private static Pattern[] DATES = new Pattern[]{
            /*german, / - no .*/        //Pattern.compile("^(?ni:(?=\\d)((?'year'((1[6-9])|([2-9]\\d))\\d\\d)(?'sep'[/.-])(?'month'0?[1-9]|1[012])\\2(?'day'((?<!(\\2((0?[2469])|11)\\2))31)|(?<!\\2(0?2)\\2)(29|30)|((?<=((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|(16|[2468][048]|[3579][26])00)\\2\\3\\2)29)|((0?[1-9])|(1\\d)|(2[0-8])))(?:(?=\\x20\\d)\\x20|$))?((?<time>((0?[1-9]|1[012])(:[0-5]\\d){0,2}(\\x20[AP]M))|([01]\\d|2[0-3])(:[0-5]\\d){1,2}))?)$"), //german
            Pattern.compile("([0-9]{4})[\\.\\/\\-]([0-9]{1,2})[\\.\\/\\-]([0-9]{1,2})"),
            /*uk, / - no .*/        Pattern.compile("^(?=\\d)(?:(?!(?:(?:0?[5-9]|1[0-4])(?:\\.|-|\\/)10(?:\\.|-|\\/)(?:1582))|(?:(?:0?[3-9]|1[0-3])(?:\\.|-|\\/)0?9(?:\\.|-|\\/)(?:1752)))(31(?!(?:\\.|-|\\/)(?:0?[2469]|11))|30(?!(?:\\.|-|\\/)0?2)|(?:29(?:(?!(?:\\.|-|\\/)0?2(?:\\.|-|\\/))|(?=\\D0?2\\D(?:(?!000[04]|(?:(?:1[^0-6]|[2468][^048]|[3579][^26])00))(?:(?:(?:\\d\\d)(?:[02468][048]|[13579][26])(?!\\x20BC))|(?:00(?:42|3[0369]|2[147]|1[258]|09)\\x20BC))))))|2[0-8]|1\\d|0?[1-9])([-.\\/])(1[012]|(?:0?[1-9]))\\2((?=(?:00(?:4[0-5]|[0-3]?\\d)\\x20BC)|(?:\\d{4}(?:$|(?=\\x20\\d)\\x20)))\\d{4}(?:\\x20BC)?)(?:$|(?=\\x20\\d)\\x20))?((?:(?:0?[1-9]|1[012])(?::[0-5]\\d){0,2}(?:\\x20[aApP][mM]))|(?:[01]\\d|2[0-3])(?::[0-5]\\d){1,2})?$"), //uk
            /*yy/.-mm/.-dd*/        Pattern.compile("^(?:(?:(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00)))(\\/|-|\\.)(?:0?2\\1(?:29)))|(?:(?:(?:1[6-9]|[2-9]\\d)?\\d{2})(\\/|-|\\.)(?:(?:(?:0?[13578]|1[02])\\2(?:31))|(?:(?:0?[1,3-9]|1[0-2])\\2(29|30))|(?:(?:0?[1-9])|(?:1[0-2]))\\2(?:0?[1-9]|1\\d|2[0-8]))))$"),
            /*29.2.04 | 29/02-2004 | 3.4.05*/        Pattern.compile("^(((0?[1-9]|[12]\\d|3[01])[\\.\\-\\/](0?[13578]|1[02])[\\.\\-\\/]((1[6-9]|[2-9]\\d)?\\d{2}))|((0?[1-9]|[12]\\d|30)[\\.\\-\\/](0?[13456789]|1[012])[\\.\\-\\/]((1[6-9]|[2-9]\\d)?\\d{2}))|((0?[1-9]|1\\d|2[0-8])[\\.\\-\\/]0?2[\\.\\-\\/]((1[6-9]|[2-9]\\d)?\\d{2}))|(29[\\.\\-\\/]0?2[\\.\\-\\/]((1[6-9]|[2-9]\\d)?(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00)|00)))$"),
            /*mm/dd/yyyy*/Pattern.compile("^([0]?[1-9]|[1][0-2])[./-]([0]?[1-9]|[1|2][0-9]|[3][0|1])[./-]([0-9]{4}|[0-9]{2})$")
    };


    //a helper method to determine if an array of values are likely to be a series of ordered numbers
    //input must be only alpha numeric characters
    public static boolean isOrderedNumber(String... alphanumValues) {
        int count_errors = 0;

        int prev = Integer.MIN_VALUE;
        for (String v : alphanumValues) {
            if (v.length() == 0)
                continue;
            try {
                int n = Integer.valueOf(v);
                if (prev == Integer.MIN_VALUE) {
                    prev = n;
                    continue;
                } else {
                    if (n != prev + 1 && n != prev) {
                        count_errors++;
                    }
                    prev = n;
                }
            } catch (NumberFormatException nfe) {
                return false;
            }
        }

        int tolerance = (int) (0.2 * alphanumValues.length);
        tolerance = tolerance < 2 ? tolerance = 2 : tolerance;
        if (count_errors > tolerance || count_errors > (alphanumValues.length * 0.5))
            return false;
        return true;
    }

    //input must be alpha numeric characters only
    public static DataType classify(String text) {
        //is it a date?
        String original = text;
        text = text.trim();
        for (Pattern p : DATES) {
            if (p.matcher(text).matches())
                return DataType.DATE;
        }

        //is it a single, long string?
        String[] tokens = text.split("\\s+");
        if (tokens.length == 1 && tokens[0].length() > 25) {
            return DataType.LONG_STRING;
        }
        text = StringUtils.toAlphaNumericWhitechar(text).trim();

        //is it empty
        if (text.length() < 1)
            return DataType.EMPTY;
        tokens = /*StringUtils.splitToAlphaNumericTokens(text,false).toArray(new String[0]);//*/text.split("\\s+");

        //is it date (we do not use regex, but only looks for months terms)
        int countMonthTerms = 0;
        for (String t : tokens) {
            if (MONTHS.contains(t.toLowerCase()))
                countMonthTerms++;
        }
        if (countMonthTerms > tokens.length * 0.2) //the phrase has n tokens and months is at least 1/5 of all tokens
            return DataType.DATE;

        //is it numeric
        boolean isNumeric = StringUtils.isNumericArray(tokens);
        if (isNumeric) {
            for (String tok : tokens) {
                if (tok.equals("AD") || tok.equals("BC") || tok.equals("A.D.") || tok.equals("B.C."))
                    return DataType.DATE;
            }
            try {
                Integer i = Integer.valueOf(text.trim());
                if (i >= 1800 && i < 2050) {
                    return DataType.DATE;
                }
            } catch (NumberFormatException nfe) {
            }

            return DataType.NUMBER;
        }
        /*  int countOrdinal=0;
   for(String tok: tokens){
       if(StringUtils.isOrdinalNumber(tok.toLowerCase()))
           countOrdinal++;
   }
   if(countOrdinal>= (tokens.length-countOrdinal) )
       return DataType.NUMBER;*/


        //is it short text
        if (tokens.length < 10) {
            //is it NE
            int countCap = 0, countLower = 0;
            for (int i = 0; i < tokens.length; i++) {
                if (StringUtils.isCapitalized(tokens[i]))
                    countCap++;
                else if (Character.isLowerCase(tokens[i].charAt(0))) {
                    countLower++;
                }
            }
            //must start with a capitalised token (ignoring digits at beginning)
            boolean capStart = false, capEnd = false;
            //if (tokens.length == 1) {
            if (StringUtils.isCapitalized(tokens[0]))
                capStart = true;
            else {
                if (isCapitalizedIDString(tokens[0]))
                    capStart = true;
            }
            //}
            if (StringUtils.isCapitalized(tokens[tokens.length - 1]))
                capEnd = true;
            else {
                if (isCapitalizedIDString(tokens[tokens.length - 1]))
                    capEnd = true;
            }

            //then must have more capitalised tokens than otherwise
            if (capStart && countCap >= (tokens.length - countCap))
                return DataType.NAMED_ENTITY;
            else if (capStart && capEnd)
                return DataType.NAMED_ENTITY;

            if (!isLikelySentence(original))
                return DataType.SHORT_TEXT;
            else
                return DataType.LONG_TEXT;
        }


        //is it long text
        if (tokens.length >= 15 || (tokens.length >= 5 && isLikelySentence(original)))
            return DataType.LONG_TEXT;
        return DataType.UNKNOWN;
    }

    private static boolean isLikelySentence(String text) {
        return text.endsWith(".") || text.endsWith("?")
                || text.endsWith("!");
    }

    public static boolean isCapitalizedIDString(String text) {
        boolean allAlphabetCapped = true; //every character in this token must be cap'ed, e.g., "1927F3I5LM"
        int countAlphabetic = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isAlphabetic(text.charAt(i))) {
                countAlphabetic++;
                if (!Character.isUpperCase(text.charAt(i))) {
                    allAlphabetCapped = false;
                    break;
                }
            }
        }
        if (countAlphabetic > 0)
            return allAlphabetCapped;
        return false;
    }


    public enum DataType implements Serializable {

        UNKNOWN("unknown"),
        EMPTY("empty"),
        ORDERED_NUMBER("onumber"), //like ranking
        NUMBER("number"), //any numeric values
        DATE("date"), //any date-like values
        SHORT_TEXT("stext"), //string of less than 10 tokens
        LONG_TEXT("ltext"),  //paragraphs of texts
        LONG_STRING("lstring"), //a single string that is very long (longer than 25 characters)
        NAMED_ENTITY("ne");  //strings likely to be named entities

        private String value;
        private static final long serialVersionUID = -1208425578110405913L;

        DataType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }


    }

}
