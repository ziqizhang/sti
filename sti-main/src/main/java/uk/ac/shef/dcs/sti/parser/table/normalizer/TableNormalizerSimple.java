package uk.ac.shef.dcs.sti.parser.table.normalizer;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;

import java.util.*;

/**
 * simply transforms a table structure Node to a representation required
 */
public class TableNormalizerSimple implements TableNormalizer {
    @Override
    public List<List<Node>> normalize(Node tableNode) {
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

                boolean started = false; //true if the first non-empty cell is found
                boolean invalid = false; //true if there is a rowspan/colspan
                for (Node td : cells) {
                    if (!started && td.getTextContent().length() > 0) { //start from the first non-empty cell
                        started = true;
                    }
                    if (started) {
                        if (isValidCell(td))
                            row.add(td); //add this cell on this row
                        else {
                            invalid = true;
                            break;
                        }
                    }
                }
                if (invalid)
                    continue;

                //add this row
                if (row.size() > 0) {
                    elements.add(row);
                }
            }
        }
        return elements;
    }

    public static boolean isValidCell(Node cell) {
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
