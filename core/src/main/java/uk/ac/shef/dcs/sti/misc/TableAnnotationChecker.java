package uk.ac.shef.dcs.sti.misc;

import uk.ac.shef.dcs.sti.algorithm.ji.LTableAnnotation_JI_Freebase;
import uk.ac.shef.dcs.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.sti.rep.LTableAnnotation;

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

    public static boolean hasAnnotation(LTableAnnotation_JI_Freebase tab_annotations) {
        for(int col=0; col<tab_annotations.getCols(); col++){
            HeaderAnnotation[] ha=tab_annotations.getHeaderAnnotation(col);
            if(ha.length>0)
                return true;
            for(int row=0; row<tab_annotations.getRows(); row++){
                if (tab_annotations.getContentCellAnnotations(row, col).length>0)
                    return true;
            }
        }
        if(tab_annotations.getRelationAnnotations_across_columns().size()>0)
            return true;

        return false;
    }
}
