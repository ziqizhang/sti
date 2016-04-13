package uk.ac.shef.dcs.sti.util;

import uk.ac.shef.dcs.sti.core.algorithm.ji.TAnnotationJI;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zqz on 16/05/2015.
 */
public class TableAnnotationChecker {


    public static boolean hasAnnotation(TAnnotationJI tabAnnotations) {
        for(int col=0; col<tabAnnotations.getCols(); col++){
            TColumnHeaderAnnotation[] ha=tabAnnotations.getHeaderAnnotation(col);
            if(ha.length>0)
                return true;
            for(int row=0; row<tabAnnotations.getRows(); row++){
                if (tabAnnotations.getContentCellAnnotations(row, col).length>0)
                    return true;
            }
        }
        if(tabAnnotations.getColumncolumnRelations().size()>0)
            return true;

        return false;
    }
}
