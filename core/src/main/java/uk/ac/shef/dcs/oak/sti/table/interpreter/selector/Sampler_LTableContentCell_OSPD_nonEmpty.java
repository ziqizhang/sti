package uk.ac.shef.dcs.oak.sti.table.interpreter.selector;

import uk.ac.shef.dcs.oak.sti.table.interpreter.misc.DataTypeClassifier;
import uk.ac.shef.dcs.oak.sti.table.rep.LTable;
import uk.ac.shef.dcs.oak.sti.table.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.sti.test.TableMinerConstants;

import java.util.*;

/**

 */
public class Sampler_LTableContentCell_OSPD_nonEmpty extends CellSelector {
    @Override
    public List<List<Integer>> select(LTable table, int fromCol, int subCol) {
        List<List<Integer>> rs = new ArrayList<List<Integer>>();

        if (TableMinerConstants.ENFORCE_OSPD && fromCol!=subCol) {
            //firstly group by one-sense-per-discourse
            Map<String, List<Integer>> grouped = new HashMap<String, List<Integer>>();
            for (int r = 0; r < table.getNumRows(); r++) {
                LTableContentCell tcc = table.getContentCell(r, fromCol);
                String text = tcc.getText();
                if (text.length() > 0) {
                    List<Integer> group = grouped.get(text);
                    if (group == null)
                        group = new ArrayList<Integer>();
                    group.add(r);
                    grouped.put(text, group);
                }
            }

            final Map<List<Integer>, Integer> countNonEmpty = new HashMap<List<Integer>, Integer>();
            //then make selection
            for (Map.Entry<String, List<Integer>> entry : grouped.entrySet()) {
                List<Integer> rows = entry.getValue();

                int count_non_emtpy = 0;
                for (int i = 0; i < rows.size(); i++) {
                    for (int c = 0; c < table.getNumCols(); c++) {
                        LTableContentCell tcc = table.getContentCell(rows.get(i), c);
                        if (tcc.getType() != null && !tcc.getType().equals(DataTypeClassifier.DataType.UNKNOWN) &&
                                !tcc.getType().equals(DataTypeClassifier.DataType.EMPTY))
                            count_non_emtpy++;
                        else if (tcc.getType() == null) {
                            String text = tcc.getText().trim();
                            if (text.length() > 0)
                                count_non_emtpy++;
                        }
                    }
                }
                countNonEmpty.put(rows, count_non_emtpy);
                if (rows.size() > 0) {
                    rs.add(rows);
                }


            }

            Collections.sort(rs, new Comparator<List<Integer>>() {
                @Override
                public int compare(List<Integer> o1, List<Integer> o2) {
                    return new Integer(countNonEmpty.get(o2)).compareTo(countNonEmpty.get(o1));
                }
            });

        }
        else{
            final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
            for (int r = 0; r < table.getNumRows(); r++) {
                int count_non_empty = 0;
                LTableContentCell tcc_at_focus = table.getContentCell(r, fromCol);
                if(tcc_at_focus.getType().equals(DataTypeClassifier.DataType.EMPTY)){
                    continue;
                }

                for (int c = 0; c < table.getNumCols(); c++) {
                    LTableContentCell tcc = table.getContentCell(r, c);
                    if (tcc.getType() != null && !tcc.getType().equals(DataTypeClassifier.DataType.UNKNOWN) &&
                            !tcc.getType().equals(DataTypeClassifier.DataType.EMPTY))
                        count_non_empty++;
                    else if(tcc.getType()==null){
                        String cellText = tcc.getText().trim();
                        if(cellText.length()>0)
                            count_non_empty++;
                    }
                }
                scores.put(r, count_non_empty);
            }

            List<Integer> list = new ArrayList<Integer>(scores.keySet());
            Collections.sort(list, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return scores.get(o2).compareTo(scores.get(o1));
                }
            });

            for (int i=0; i<list.size(); i++){
                List<Integer> block = new ArrayList<Integer>();
                block.add(i);
                rs.add(block);
            }
        }
        return rs;
    }
}
