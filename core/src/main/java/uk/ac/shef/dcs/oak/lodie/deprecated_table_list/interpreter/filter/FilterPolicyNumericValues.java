package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.filter;

import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContentCell;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 11/03/13
 * Time: 14:49
 */
public class FilterPolicyNumericValues implements FilterPolicy {
    @Override
    public boolean discard(LTable table, int row, int column) {
        if(row==-1 || column==-1)
            return false;
        LTableContentCell content = table.getContentCell(row, column);

        return isNumericValue(content.getText());
    }

    /**
     * @param text
     * @return true if after removing non-alpha-numeric tokens, over 60% of characters are digits
     */
    public static boolean isNumericValue(String text) {
        text = text.replaceAll("[^a-zA-Z0-9]", "");
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (Character.isDigit(text.charAt(i)))
                count++;
        }
        return (double) count / text.length() > 0.6;
    }
}
