package uk.ac.shef.dcs.sti.core.algorithm.tmp.sampler;

import uk.ac.shef.dcs.sti.util.DataTypeClassifier;
import uk.ac.shef.dcs.sti.core.model.TCell;
import uk.ac.shef.dcs.sti.core.model.Table;
import uk.ac.shef.dcs.sti.STIConstantProperty;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 11/06/14
 * Time: 17:18
 * To change this template use File | Settings | File Templates.
 */
public class OSPD_random extends TContentCellRanker {
    @Override
    public List<List<Integer>> select(Table table, int fromCol, int subCol) {
        List<List<Integer>> rs = new ArrayList<List<Integer>>();

        if (STIConstantProperty.ENFORCE_OSPD && fromCol!=subCol) {
            //firstly group by one-sense-per-discourse
            Map<String, List<Integer>> grouped = new HashMap<String, List<Integer>>();
            for (int r = 0; r < table.getNumRows(); r++) {
                TCell tcc = table.getContentCell(r, fromCol);
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
                        TCell tcc = table.getContentCell(rows.get(i), c);
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

            Collections.shuffle(rs);

        }
        else{
            final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
            for (int r = 0; r < table.getNumRows(); r++) {
                int count_non_empty = 0;
                TCell tcc_at_focus = table.getContentCell(r, fromCol);
                if(tcc_at_focus.getType().equals(DataTypeClassifier.DataType.EMPTY)){
                    continue;
                }

                for (int c = 0; c < table.getNumCols(); c++) {
                    TCell tcc = table.getContentCell(r, c);
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
            Collections.shuffle(list);

            for (int i=0; i<list.size(); i++){
                List<Integer> block = new ArrayList<Integer>();
                block.add(i);
                rs.add(block);
            }
        }
        return rs;
    }
}
