package uk.ac.shef.dcs.sti.core.model;

import java.io.Serializable;

/**
 */
public class RelationColumns implements Serializable {
    private static final long serialVersionUID = -7136525814010415943L;

    private int subjectCol;
    private int objectCol;

    public RelationColumns(int subjectCol, int objectCol) {
        this.subjectCol=subjectCol;
        this.objectCol=objectCol;
    }

    public int getSubjectCol() {
        return subjectCol;
    }

    public void setSubjectCol(int subjectCol) {
        this.subjectCol = subjectCol;
    }

    public int getObjectCol() {
        return objectCol;
    }

    public void setObjectCol(int objectCol) {
        this.objectCol = objectCol;
    }

    public boolean equals(Object o){
        if(o instanceof RelationColumns){
            RelationColumns k = (RelationColumns)o;
            return k.getSubjectCol()==getSubjectCol()&& k.getObjectCol()==getObjectCol();
        }
        return false;
    }

    public int hashCode(){
        return subjectCol*19+objectCol*29;
    }

    public String toString(){
        return getSubjectCol()+"-"+getObjectCol();
    }
}
