package uk.ac.shef.dcs.oak.sti.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 21/02/14
 * Time: 14:04
 * To change this template use File | Settings | File Templates.
 */
public class TableNormalizerDummy implements TableNormalizer {
    @Override
    public List<List<Node>> apply(Node tableNode) {
        List<List<Node>> elements = new ArrayList<List<Node>>();

        List<Node> rows = DomUtils.findAllByTag(tableNode, "TR");

        for (Node element : rows) {  //loop thru each row
            if (element.getTextContent().length() > 0) {  //if this row has content, go ahead
                List<Node> row = new ArrayList<Node>();
                List<Node> cells = DomUtils.findAllByTag(element, "TH");
                if (cells == null || cells.size() == 0)
                    cells = DomUtils.findAllByTag(element, "THEAD");
                boolean hasTH = false;
                if (cells.size() == 0)
                    cells = DomUtils.findAllByTag(element, "TD");
                else
                    hasTH = true;

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

                //check if the row ends with empty cells
                /*int lastNonEmpty = row.size() - 1;
                for (int i = row.size() - 1; i > -1; i--) {  //loop through cells on this row
                    if (row.get(i).getTextContent().length() != 0) {
                        lastNonEmpty = i;
                        break;
                    }
                }
                for (int n = row.size() - 1; n > lastNonEmpty; n--) {
                    row.remove(n);
                }*/

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
