package uk.ac.shef.dcs.sti.nlp;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class TermFreqCounter {

    /**
     * Count number of occurrences of a string in a context.
     *
     * @param noun    the string to be counted
     * @param context the text in which the string can be found
     * @return int array which contains offsets of occurrences in the text.
     */
    public Set<Integer> countOffsets(String noun, String context) {
        Set<Integer> offsets = new HashSet<Integer>();
        int next;
        int start = 0;
        while (start <= context.length()) {
            next = context.indexOf(noun, start);
            char prefix = next - 1 < 0 ? ' ' : context.charAt(next - 1);
            char suffix = next + noun.length() >= context.length() ? ' ' : context.charAt(next + noun.length());
            if (next != -1 && isValidChar(prefix) && isValidChar(suffix)) {
                offsets.add(next);
            }
            if (next == -1) break;
            start = next + noun.length();
        }


        return offsets;
    }

    private boolean isValidChar(char c) {
        return !Character.isLetter(c) && !Character.isDigit(c);
    }
}
