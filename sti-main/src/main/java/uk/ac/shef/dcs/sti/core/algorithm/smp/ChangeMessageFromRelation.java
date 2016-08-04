package uk.ac.shef.dcs.sti.core.algorithm.smp;

import java.util.List;

/**
 * Created by zqz on 23/04/2015.
 */
class ChangeMessageFromRelation extends ChangeMessage {
    private int subobjIndicator; //0 - the cell needs changing is the subject in a relation; 1 - the cell needs changing is the object in a relation


    public int getSubobjIndicator() {
        return subobjIndicator;
    }

    public void setSubobjIndicator(int subobjIndicator) {
        this.subobjIndicator = subobjIndicator;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public String toString(){
        return confidence+","+ subobjIndicator +","+labels;
    }
}
