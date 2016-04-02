package uk.ac.shef.dcs.sti.misc;

import uk.ac.shef.dcs.sti.core.algorithm.ji.TAnnotation_JI_Freebase;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zqz on 16/05/2015.
 */
public class TableAnnotationChecker {
    public static void checkAnnotation(TAnnotation annotation){
        Map<String, Integer> countHeader = new HashMap<String, Integer>();
        Map<String, Integer> countCell = new HashMap<String, Integer>();

        for(int col=0; col<annotation.getCols(); col++){
            TColumnHeaderAnnotation[] ha=annotation.getHeaderAnnotation(col);
            countHeader.put(String.valueOf(col), ha.length);
            for(int row=0; row<annotation.getRows(); row++){
                countCell.put(row+","+col, annotation.getContentCellAnnotations(row, col).length);
            }
        }

        System.out.println("header:"+countHeader);
        System.out.println("cell:"+countCell);
        System.out.println("rel:"+annotation.getColumncolumnRelations());
    }

    public static boolean hasAnnotation(TAnnotation_JI_Freebase tab_annotations) {
        for(int col=0; col<tab_annotations.getCols(); col++){
            TColumnHeaderAnnotation[] ha=tab_annotations.getHeaderAnnotation(col);
            if(ha.length>0)
                return true;
            for(int row=0; row<tab_annotations.getRows(); row++){
                if (tab_annotations.getContentCellAnnotations(row, col).length>0)
                    return true;
            }
        }
        if(tab_annotations.getColumncolumnRelations().size()>0)
            return true;

        return false;
    }
}
