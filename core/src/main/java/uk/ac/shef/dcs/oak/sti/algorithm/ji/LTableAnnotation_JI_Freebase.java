package uk.ac.shef.dcs.oak.sti.algorithm.ji;

import uk.ac.shef.dcs.oak.sti.rep.LTableAnnotation;

/**
 * Created by zqz on 01/05/2015.
 */
public class LTableAnnotation_JI_Freebase extends LTableAnnotation {
    public LTableAnnotation_JI_Freebase(int rows, int cols) {
        super(rows, cols);
    }

    public double getScore_entityAndConcept(String entityId, String conceptId){
        //todo
        return 0.0;
    }
}
