package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import uk.ac.shef.dcs.oak.lodie.table.rep.CellAnnotation;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTable;
import uk.ac.shef.dcs.oak.lodie.table.rep.LTableContentCell;
import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zqz on 21/04/2015.
 */
public class RelationMatcher {

    /**
     * This is the adapted implementation of SMP. Only the highest scoring cell entity annotation is used to infer candidate
     * relations.
     *
     * @param subjectCells
     * @param objectCells
     * @return
     */
    public List<ObjObj<String, Double>> match(CellAnnotation[] subjectCells, CellAnnotation[] objectCells,
                                              LTableContentCell subjectCellText, LTableContentCell objectCellText,
                                              LTable table){
        List<ObjObj<String, Double>> result = new ArrayList<ObjObj<String, Double>>();
        if(subjectCells.length==0||objectCells.length==0)
            return result;
        CellAnnotation subjectEntity = subjectCells[0];
        String subText = subjectCellText.getText();
        CellAnnotation objectEntity = objectCells[0];
        String obText = objectCellText.getText();


        return result;
    }
}
