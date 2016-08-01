package uk.ac.shef.dcs.sti.parser.table.validator;

import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.parser.ContentValidator;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/10/12
 * Time: 10:47
 * implements following policies:
 * - Must have no more than 1 empty columns (a column is empty if over 20% of cells are empty)
 * - Must have no more than 1 lengthy columns (a column is lengthy if it has more than 10% cells lengthy(more than 5 words, or a multi-valued item))
 * - Must have no more than 1 link columns
 * - Must have at least 2 columns and over 50% of columns satisfying all above conditions (excl. numeric columns)
 * - Must have at least 3 data rows
 */
public class TableValidatorGeneric extends ContentValidator implements TableValidator {
    protected static final double THRESHOLD_MAX_ALLOWED_EMPTY_CELLS_IN_COLUMN = 0.2;
    protected static final double THRESHOLD_MAX_ALLOWED_LENGTHY_CELLS_IN_COLUMN = 0.1;
    protected static final double THRESHOLD_MAX_ALLOWED_NUMERIC_CELLS_IN_COLUMN = 0.1;

    protected static final int THRESHOLD_MIN_PROPER_DATA_ROWS = 3;

    protected static final double THRESHOLD_MAX_ALLOWED_LINK_CELLS_IN_COLUMN = 0.1;

     protected static final int THRESHOLD_LENGTHY_CELL_MAXSINGLEVALUELENGTH = 5; //max # of tokens in a single cell VALUE (ie.
    // if there are multi. links/lists in a cell, this is for each of them AND the entire text cell length cannot be
    // longer than [# of links] * this value

    public TableValidatorGeneric() {
    }


    @Override
    public boolean validate(Table table) {

        if (table.getNumRows() < THRESHOLD_MIN_PROPER_DATA_ROWS)
            return false;

        return true;
    }

    protected boolean isLengthyCell(TCell tc) {
        int textLength = tc.getText().split("\\s+").length;
        return textLength > TableValidatorForWikipediaGSLanient.THRESHOLD_LENGTHY_CELL_MAXSINGLEVALUELENGTH;
    }

    protected boolean tooManyLengthyCellsInColumn(TCell[] cells) {
        int countLengthyPerCol = 0;
        for (TCell ltc : cells) {
            if (isLengthyCell(ltc)) {
                countLengthyPerCol++;
            }
        }
        return countLengthyPerCol > cells.length * THRESHOLD_MAX_ALLOWED_LENGTHY_CELLS_IN_COLUMN;
    }

    protected boolean hasLengthyCellsInColumn(TCell[] cells) {
        for (TCell ltc : cells) {
            if (isLengthyCell(ltc)) {
                return true;
            }
        }
        return false;
    }

    protected boolean tooManyEmptyCellsInColumn(TCell[] cells) {
        int countEmptyPerCol = 0;
        for (TCell ltc : cells) {
            if (isEmptyString(ltc.getText())) {
                countEmptyPerCol++;
            }
        }
        return countEmptyPerCol > cells.length * THRESHOLD_MAX_ALLOWED_EMPTY_CELLS_IN_COLUMN;
    }

    protected boolean hasEmptyCellsInColumn(TCell[] cells) {

        for (TCell ltc : cells) {
            if (isEmptyString(ltc.getText())) {
                return true;
            }
        }
        return false;
    }

    protected boolean tooManyNumericCellsInColumn(TCell[] cells) {
        int countNumericPerCol = 0;
        for (TCell ltc : cells) {
            if (isNumericContent(ltc.getText())) {
                countNumericPerCol++;
            }
        }
        return countNumericPerCol > cells.length * THRESHOLD_MAX_ALLOWED_NUMERIC_CELLS_IN_COLUMN;
    }

    protected boolean hasNumericCellsInColumn(TCell[] cells) {
        for (TCell ltc : cells) {
            if (isNumericContent(ltc.getText())) {
                return true;
            }
        }
        return false;
    }

}
