package uk.ac.shef.dcs.sti.parser.table.normalizer;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;

import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 05/10/12
 * Time: 11:31
 * <p/>
 * - if a row begins/ends with empty cells, the beginning/ending empty cells are discarded
 * - if a row contains cells with rowspan/colspan, the row is discarded
 * - record # of cells in rows in a row-length table
 * - keep only rows that are the longest; any rows that have less than so many cells are dropped
 */
public class TableNormalizerDiscardIrregularRows implements TableNormalizer {

    private boolean validateNonSpanCells = true;

    public TableNormalizerDiscardIrregularRows(boolean validateNonSpanCells) {
        this.validateNonSpanCells = validateNonSpanCells;
    }

    @Override
    public List<List<Node>> normalize(Node tableNode) {
        Map<Integer, Integer> numCells2Freq = new HashMap<>();
        List<List<Node>> elements = new ArrayList<>();

        List<Node> rows = DomUtils.findAllByTag(tableNode, "TR");

        for (Node element : rows) {  //loop thru each row
            if (element.getTextContent().length() > 0) {  //if this row has content, go ahead
                List<Node> row = new ArrayList<>();
                List<Node> cells = DomUtils.findAllByTag(element, "TH");
                if (cells == null || cells.size() == 0)
                    cells = DomUtils.findAllByTag(element, "THEAD");
                if (cells.size() == 0)
                    cells = DomUtils.findAllByTag(element, "TD");
                //boolean started = false; //true if the first non-empty cell is found
                boolean invalid = false; //true if there is a rowspan/colspan
                for (Node td : cells) {
                    /* if (!started && td.getTextContent().length() > 0) { //start from the first non-empty cell
                      started = true;
                  }
                  if (started) {*/
                    if (isValidCell(td))
                        row.add(td); //add this cell on this row
                    else {
                        invalid = true;
                        break;
                    }
                    // }
                }
                if (invalid)
                    continue;

                //add this row
                if (row.size() > 0) {
                    elements.add(row);
                    Integer freq = numCells2Freq.get(row.size());
                    freq = freq == null ? 0 : freq;
                    freq = freq + 1;
                    numCells2Freq.put(row.size(), freq);
                }
            }
        }

        //find max row-length; if there is a tie, the first encountered frequency by the map iterator is selected
        int maxFreq = 0, length = 0;
        for (Map.Entry<Integer, Integer> e : numCells2Freq.entrySet()) {
            if (e.getValue() > maxFreq) {
                maxFreq = e.getValue();
                length = e.getKey();
            }
        }

        //filter to keep only the rows with the longest length (the length varable above)
        Iterator<List<Node>> it = elements.iterator();
        while (it.hasNext()) {
            List<Node> row = it.next();
            if (row.size() != length)
                it.remove();
        }

        return elements;
    }

    //check for col span or row span
    public boolean isValidCell(Node cell) {
        if (!validateNonSpanCells)
            return true;
        String colspan = null;
        try {
            colspan = cell.getAttributes().getNamedItem("colspan").getTextContent();
        } catch (NullPointerException n) {
        }

        String rowspan = null;
        try {
            rowspan = cell.getAttributes().getNamedItem("rowspan").getTextContent();
        } catch (NullPointerException n) {
        }

        if (colspan != null) {
            if (colspan.equals("1") || colspan.equals(""))
                return true;
            else
                return false;
        } else if (rowspan != null) {
            if (rowspan.equals("1") || rowspan.equals(""))
                return true;
            else
                return false;
        } else
            return true;
    }
}
