package uk.ac.shef.dcs.sti.parser.table.hodetector;

import cern.colt.matrix.ObjectMatrix2D;
import cern.colt.matrix.impl.SparseObjectMatrix2D;
import org.w3c.dom.Node;

import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 05/10/12
 * Time: 11:59
 * <p/>
 * The simplest approach that detects orientation and headers by searching for the "th" html tags in rows/columns
 */
public class TableHODetectorByHTMLTag implements TableHODetector {

    /**
     * @param elements, holds Element (Jsoup) objects in its cells
     * @return an ObjectMatrix2D representation of a HORIZONTAL table (vertical tables will have been transposed.
     *         Object in cells will be Jsoup Element. If no header row is found for this table, the first row in matrix
     *         will have null values in all cells
     */
    @Override
    public ObjectMatrix2D detect(List<List<Node>> elements) {
        int thR = -1, thC = -1;
        boolean foundTH = false;
        for (int i = 0; i < elements.size(); i++) {
            List<Node> row = elements.get(i);
            for (int j = 0; j < row.size(); j++) {
                Node cell = row.get(j);
                if (!foundTH && (cell.getNodeName().equalsIgnoreCase("th")||cell.getNodeName().equalsIgnoreCase("thead"))) {
                    thR = i;
                    thC = j;
                    foundTH = true;
                    break;
                }
            }
            if (foundTH)
                break;
        }

        boolean horizontalHeader = true;

        //count #of <th> on the thR row and the thC column
        if (thR != -1 && thC != -1) {
            List<Node> thRow = elements.get(thR);
            int countRowTh = 0;    //th on the row
            for (Node e : thRow) {
                if (e.getNodeName().equalsIgnoreCase("TH")||e.getNodeName().equalsIgnoreCase("THead"))
                    countRowTh++;
            }

            int countColTh = 0;  //th on the column
            for (List<Node> row : elements) {
                if (row.get(thC).getNodeName().equalsIgnoreCase("TH")||row.get(thC).getNodeName().equalsIgnoreCase("THead"))
                    countColTh++;
            }

            //when there are both th in row and column
            // see http://en.wikipedia.org/wiki/Italy, the determination learn is as follows:
            /*
                if(countRowTh == row length)
                    choose horizontal
             */
            if (countRowTh == elements.get(0).size()) {   //in rare occasions (mostly because of ill-used html tags
                //you can get obscure <th> tags in a row and # of this tags wont computeElementScores
            } else if (countRowTh < countColTh)
                horizontalHeader = false;

            int startRow = thR == -1 ||(countRowTh==1&& elements.get(0).size()>1)? 0 : thR; //if no th row found, or only 1 th found in the row that has more than 1 elements (likely a mis use of th)
            int startCol = thC == -1 ||(countColTh==1&& elements.size()>1)? 0 : thC;

            /*if(countRowTh==1&& elements.get(0).size()>1)
                System.out.print("");
            if(countColTh==1&& elements.size()>1)
                System.out.print("");*/

            ObjectMatrix2D table;
            if (horizontalHeader)
                table = new SparseObjectMatrix2D(elements.size() - startRow, elements.get(0).size() - startCol);
            else
                table = new SparseObjectMatrix2D(elements.get(0).size() - startCol, elements.size() - startRow);


            for (int r = startRow; r < elements.size(); r++) {
                for (int c = startCol; c < elements.get(0).size(); c++) {
                    if (horizontalHeader)
                        table.setQuick(r-startRow, c-startCol, elements.get(r).get(c));
                    else
                        table.setQuick(c-startCol, r-startRow, elements.get(r).get(c));
                }
            }
            return table;
        } else {//no "th" found, simply assume horizontal header and add a "false" header row
            ObjectMatrix2D table =
                    new SparseObjectMatrix2D(elements.size() + 1, elements.get(0).size());

            for (int r = 0; r < elements.size()+1; r++) {
                for (int c = 0; c < elements.get(0).size(); c++) {
                    if (r == 0)
                        table.setQuick(r, c, null);
                    else
                        table.setQuick(r, c, elements.get(r-1).get(c));
                }
            }
            return table;
        }
    }

}
