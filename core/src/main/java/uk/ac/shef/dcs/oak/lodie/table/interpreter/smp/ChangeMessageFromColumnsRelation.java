package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

/**
 * Created by zqz on 23/04/2015.
 */
public class ChangeMessageFromColumnsRelation extends ChangeMessage {
    private int flag_subOrObj; //0 - the cell needs changing is the subject in a relation; 1 - the cell needs changing is the object in a relation


    public int getFlag_subOrObj() {
        return flag_subOrObj;
    }

    public void setFlag_subOrObj(int flag_subOrObj) {
        this.flag_subOrObj = flag_subOrObj;
    }
}
