package uk.ac.shef.dcs.sti.parser.table.validator;

import uk.ac.shef.dcs.sti.core.model.TCellAnnotation;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 10/10/12
 * Time: 12:06
 * <p/>
 * implements following policies:
 * - Must have no more than 1 empty columns (a column is empty if over 20% of cells are empty)
 * - Must have no more than 1 lengthy columns (a column is lengthy if it has more than 10% cells lengthy(more than 5 words, or a multi-valued item))
 * - Must have at least two columns that have over 50% with URIs
 * - Must have at least 2 columns and over 50% of columns satisfying all above conditions (excl. numeric columns)
 * - Must have at least 3 data rows
 */
public class TableValidatorForWikipediaGSStrict extends TableValidatorGeneric {
    protected static final int THRESHOLD_MIN_COLUMNS_WITH_URIS = 2;
    protected static final double THRESHOLD_MIN_FRAC_URICOLUMNS = 0.4;

    protected static final int THRESHOLD_MIN_PROPER_DATA_COLUMNS = 2;
    protected static final double THRESHOLD_MIN_PROPER_DATA_COLUMNS_FRAC = 0.5;
    protected static final int THRESHOLD_MIN_PROPER_DATA_ROWS = 3;


    public TableValidatorForWikipediaGSStrict() {
    }


    @Override
    public boolean validate(Table table) {
        int countEmptyColumns = 0, countLengthyColumns = 0, countNumericColumns = 0, countURIGSColumns = 0;


        if (table.getNumRows() < THRESHOLD_MIN_PROPER_DATA_ROWS)
            return false;

        for (int c = 0; c < table.getNumCols(); c++) {
            int countLengthyPerCol = 0, countNumericPerCol = 0, countEmptyPerCol = 0, countURIGSPerCol = 0;

            for (int r = 0; r < table.getNumRows(); r++) {
                TCell ltc = (TCell) table.getContentCell(r, c);
                String tcText = ltc.getText();
                TCellAnnotation[] annotations = table.getTableAnnotations().getContentCellAnnotations(r, c);
                if (annotations.length > 0) {
                    String uri = annotations[0].getAnnotation().getId();
                    if (isWikiInternalLink(uri)) {

                        countURIGSPerCol++;
                    }
                }

                if (isEmptyMediaWikiString(tcText)) {
                    countEmptyPerCol++;
                }
                if (isLengthyCell(ltc)) {
                    countLengthyPerCol++;
                }

                if (isNumericContent(tcText)) {
                    countNumericPerCol++;
                }
            }

            /*if (countEmptyPerCol > matrix.rows() * THRESHOLD_MAX_ALLOWED_EMPTY_CELLS_IN_COLUMN) {
                countEmptyColumns++;
                table.setColumnDataType(Table.TColumnDataType.EMPTY, c);
            }
            if (countLengthyPerCol > matrix.rows() * THRESHOLD_MAX_ALLOWED_LENGTHY_CELLS_IN_COLUMN) {
                countLengthyColumns++;
                table.setColumnDataType(Table.TColumnDataType.LONGTEXT, c);
            }
            if (countNumericPerCol > matrix.rows() * THRESHOLD_MAX_ALLOWED_NUMERIC_CELLS_IN_COLUMN) {
                countNumericColumns++;
                table.setColumnDataType(Table.TColumnDataType.NUMERIC, c);
            }
            if (countURIGSPerCol > matrix.rows() * THRESHOLD_MIN_FRAC_CELLS_WITH_URIS_IN_COLUMN) {
                countURIGSColumns++;
                table.setColumnDataType(Table.TColumnDataType.LINK, c);
            }*/

        }
        int properDataColumns = table.getNumCols() - (countLengthyColumns + countNumericColumns + countEmptyColumns);
        if (countURIGSColumns < THRESHOLD_MIN_COLUMNS_WITH_URIS ||
                countURIGSColumns < THRESHOLD_MIN_FRAC_URICOLUMNS * table.getNumCols())
            return false;

        if (properDataColumns < THRESHOLD_MIN_PROPER_DATA_COLUMNS)
            return false;
        if (properDataColumns < table.getNumRows() * THRESHOLD_MIN_PROPER_DATA_COLUMNS_FRAC)
            return false;
        if (properDataColumns < countLengthyColumns || properDataColumns < countNumericColumns || properDataColumns < countEmptyColumns)
            return false; //too many lengthy/numeric/empty columns wrt proper columns
        if (countEmptyColumns > 1)
            return false;
        if (countLengthyColumns > 1)
            return false;

        return true;
    }

}
