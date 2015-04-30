package uk.ac.shef.dcs.oak.sti.table.xtractor;

import org.apache.any23.extractor.html.DomUtils;
import org.w3c.dom.Node;

import java.util.*;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 05/10/12
 * Time: 11:31
 * <p/>
 * - if a row begins/ends with empty cells, the beginning/ending empty cells are discarded
 * - if a row contains cells with rowspan/colspan, the row is NOT discarded
 * - record # of cells in rows in a row-length table
 * - keep only rows that has most frequenty # of cols; any rows less than this are filled with empty cells; any rows more than this are trimmed
 *
 *
 */
public class TableNormalizerFrequentRowLength_Permissive implements TableNormalizer {


    public TableNormalizerFrequentRowLength_Permissive(){
    }

    @Override
    public List<List<Node>> apply(Node tableNode) {
        Map<Integer, Integer> numCells2Freq = new HashMap<Integer, Integer>();
        List<List<Node>> elements = new ArrayList<List<Node>>();

        List<Node> rows = DomUtils.findAllByTag(tableNode, "TR");

        for (Node element : rows) {  //loop thru each row
            if (element.getTextContent().length() > 0) {  //if this row has content, go ahead
                List<Node> row = new ArrayList<Node>();
                List<Node> cells = DomUtils.findAllByTag(element, "TH");
                if(cells==null||cells.size()==0)
                    cells=DomUtils.findAllByTag(element, "THEAD");
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
                /*************** THIS IS VERY DANGEROUS, CAN DESTROY TABLE STRUCTURE THAT HAS MANY EMPTY CELLS*/
                int lastNonEmpty = row.size() - 1;
                for (int i = row.size() - 1; i > -1; i--) {  //loop through cells on this row
                    if (row.get(i).getTextContent().length() != 0) {
                        lastNonEmpty = i;
                        break;
                    }
                }
                for (int n = row.size() - 1; n > lastNonEmpty; n--) {
                    row.remove(n);
                }
                /****************/


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

        //find most frequenty row-length; if there is a tie, the first encountered frequency by the map iterator is selected
        Map<Integer, Integer> counting = new HashMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> e : numCells2Freq.entrySet()) {
            int cols = e.getValue();
            Integer count = counting.get(cols);
            count=count ==null?0:count;
            count++;
            counting.put(cols,count);
        }
        int max=0;
        for(Integer freq: counting.keySet()){
            if(freq>max)
                max=freq;
        }

        //filter to keep only the rows with the longest length (the length varable above)
        Iterator<List<Node>> it = elements.iterator();
        while (it.hasNext()) {
            List<Node> row = it.next();
            if(row.size()>max){
                for(int i=max;i<row.size();i++){
                    row.remove(row.size()-1);
                }
            }
            if (row.size() <max){
                for(int i=row.size();i<max; i++){
                    row.add(null);
                }
                it.remove();
            }
        }

        return elements;
    }

    //check for col span or row span
    public boolean isValidCell(Node cell) {
        return true;
    }
}

