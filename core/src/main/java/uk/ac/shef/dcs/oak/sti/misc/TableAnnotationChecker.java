package uk.ac.shef.dcs.oak.sti.misc;

import uk.ac.shef.dcs.oak.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zqz on 16/05/2015.
 */
public class TableAnnotationChecker {
    public static void checkAnnotation(LTableAnnotation annotation){
        Map<String, Integer> countHeader = new HashMap<String, Integer>();
        Map<String, Integer> countCell = new HashMap<String, Integer>();

        for(int col=0; col<annotation.getCols(); col++){
            HeaderAnnotation[] ha=annotation.getHeaderAnnotation(col);
            countHeader.put(String.valueOf(col), ha.length);
            for(int row=0; row<annotation.getRows(); row++){
                countCell.put(row+","+col, annotation.getContentCellAnnotations(row, col).length);
            }
        }

        System.out.println("header:"+countHeader);
        System.out.println("cell:"+countCell);
        System.out.println("rel:"+annotation.getRelationAnnotations_across_columns());
    }
}
