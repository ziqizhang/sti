package uk.ac.shef.dcs.sti.algorithm.tm.sampler;

import uk.ac.shef.dcs.sti.misc.DataTypeClassifier;
import uk.ac.shef.dcs.sti.rep.Table;
import uk.ac.shef.dcs.sti.rep.TContentCell;

import java.util.*;

/**
 */
@Deprecated
public class LTableContentCell_Ranker_nonEmpty extends TContentRowRanker {
    @Override
    public int[] select(Table table) {
        int[] rs = new int[table.getNumRows()];

        final Map<Integer, Integer> scores = new LinkedHashMap<Integer, Integer>();
        for (int i = 0; i < table.getNumRows(); i++) {
            int count_non_empty = 0;
            for (int col = 0; col < table.getNumCols(); col++) {
                TContentCell tcc = table.getContentCell(i, col);
                if (tcc.getType() != null && !tcc.getType().equals(DataTypeClassifier.DataType.UNKNOWN) &&
                        !tcc.getType().equals(DataTypeClassifier.DataType.EMPTY))
                    count_non_empty++;
                else if(tcc.getType()==null){
                    String cellText = tcc.getText().trim();
                    if(cellText.length()>0)
                        count_non_empty++;
                }
            }
            scores.put(i, count_non_empty);
        }

        List<Integer> list = new ArrayList<Integer>(scores.keySet());
        Collections.sort(list, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return scores.get(o2).compareTo(scores.get(o1));
            }
        });

        for (int i=0; i<list.size(); i++){
            rs[i]=list.get(i);
        }

        return rs;
    }
}
