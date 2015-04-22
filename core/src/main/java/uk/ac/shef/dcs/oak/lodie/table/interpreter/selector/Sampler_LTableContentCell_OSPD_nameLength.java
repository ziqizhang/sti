package uk.ac.shef.dcs.oak.lodie.table.interpreter.selector;

import uk.ac.shef.dcs.oak.lodie.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.lodie.test.TableMinerConstants;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 27/05/14
 * Time: 11:09
 * To change this template use File | Settings | File Templates.
 */
public class Sampler_LTableContentCell_OSPD_nameLength extends CellSelector {
    @Override
    public List<List<Integer>> select(LTable table, int fromCol, int subCol) {
        List<List<Integer>> rs = new ArrayList<List<Integer>>();


        final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
        for (int r = 0; r < table.getNumRows(); r++) {
            int count_name_length = 0;
            LTableContentCell tcc_at_focus = table.getContentCell(r, fromCol);
            if (tcc_at_focus.getType().equals(DataTypeClassifier.DataType.EMPTY)) {
                scores.put(r, 0);
                continue;
            }

            String text = tcc_at_focus.getText();
            text = text.replaceAll("[\\-_/,]"," ").replace("\\s+"," ").trim();
            count_name_length=text.split("\\s+").length;
            scores.put(r, count_name_length);
        }

        List<Integer> list = new ArrayList<Integer>(scores.keySet());
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return scores.get(o2).compareTo(scores.get(o1));
            }
        });

        for (int i = 0; i < list.size(); i++) {
            List<Integer> block = new ArrayList<Integer>();
            block.add(i);
            rs.add(block);
        }

        return rs;
    }
}
