package uk.ac.shef.dcs.sti.parser.table.validator;

import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 08/10/12
 * Time: 13:23
 * <p/>
 * Lanient rules:
 * <p/>
 * -min rows rule: at least X data rows (excl. header)
 * -min colums rule: at least X columns
 * -empty cell rule: more than X% of cells must be NON-empty
 * \t      a cell is empty if: the text within has length 0
 * \t                          the text is embedded by {{}}, a MediaWiki syntax meaning empty or undefined
 * \t                          the text has no english letter or numbers
 * -proper data columns rule: at least X columns are "proper" data
 * \t     a cell is a "proper" data cell if it is NOT lengthy or numeric
 * \t     a column is a proper column if over X% of cells are not lengthy or numeric
 * \t           a cell is lengthy if: 1)for multi-valued cell, there are more than X value items
 * \t                                 2)>50% of each single value in the multi-valued-cell has more than X tokens
 * \t                                 3)the extracted text from the cell is longer than the max allowed, which is calculated as
 * \t                                       #of-items-in-multivalued-cell * max-allowed-tokens-per-item (see above, 2)
 * \t           a cell is numeric if: 1)the text has no letter and 2)over 50% of chars in the text (excl. white space) are digits
 * -non-lengthy-table rule: (apart from the above), no more than X% of cells are "lengthy"
 * Additionally, a warning is issued when a multi-valued cell is found
 */
public class TableValidatorForWikipediaGSLanient extends TableValidatorGeneric {

    protected final static int THRESHOLD_MIN_ROWS = 3;
    protected final static int THRESHOLD_MIN_COLS = 2;

    protected final static double THRESHOLD_MAX_EMPTY_CELLS_IN_COLUMNS = 0.5;

    protected final static int THRESHOLD_MIN_PROPER_DATA_COLUMNS = 2;//minimum # of "proper" data columns to keep a table
    protected final static double THRESHOLD_MAX_LENGTHY_CELLS_IN_COLUMN = 0.5; //50% of all elements in a column are "lengthy" then this column is lengthy
    protected final static double THRESHOLD_MAX_NUMERIC_CELLS_IN_COLUMN = 0.5; //similar as above

    protected final static double THRESHOLD_MAX_LENGHTY_CELLS_IN_TABLE = 0.5; //% of lengthy cells allowed in a "valid" table



    @Override
    public boolean validate(Table table) {
        int countEmpty = 0, countLengthy = 0;
        int countLengthyColumns = 0, countNumericColumns = 0;

        for (int c = 0; c < table.getNumCols(); c++) {
            for (int r = 0; r < table.getNumRows(); r++) {
                TCell ltc =table.getContentCell(r, c);
                String tcText = ltc.getText();
                if (isEmptyMediaWikiString(tcText))
                    countEmpty++;
            }

        }
        if (table.getNumRows() < THRESHOLD_MIN_ROWS)
            return false;
        if (table.getNumCols() < THRESHOLD_MIN_COLS)
            return false;
        if (countEmpty > table.size() * THRESHOLD_MAX_LENGHTY_CELLS_IN_TABLE)
            return false;

        if (table.getNumCols() - (countLengthyColumns + countNumericColumns) < THRESHOLD_MIN_PROPER_DATA_COLUMNS)
            return false;

        if (countLengthy > table.size() * THRESHOLD_MAX_LENGHTY_CELLS_IN_TABLE)
            return false;
        return true;
    }

    protected boolean isLengthyCell(TCell tc) {
        int textLength = tc.getText().split("\\s+").length;
        return textLength >  THRESHOLD_LENGTHY_CELL_MAXSINGLEVALUELENGTH;
    }



}
