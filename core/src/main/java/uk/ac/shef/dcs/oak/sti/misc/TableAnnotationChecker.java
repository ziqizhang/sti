package uk.ac.shef.dcs.oak.sti.misc;

import uk.ac.shef.dcs.oak.sti.rep.HeaderAnnotation;
import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;

/**
 * Created by zqz on 16/05/2015.
 */
public class TableAnnotationChecker {
    public static void checkAnnotation(LTableAnnotation annotation){
        ;
        for(int col=0; col<annotation.getCols(); col++){
            HeaderAnnotation[] ha=annotation.getHeaderAnnotation(col);
            if(ha.length==0){
                System.err.println("COL="+0+", is empty");
            }
            for(int row=0; row<annotation.getRows(); row++){

            }
        }
    }
}
