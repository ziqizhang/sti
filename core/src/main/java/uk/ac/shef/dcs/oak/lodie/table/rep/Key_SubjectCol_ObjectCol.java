package uk.ac.shef.dcs.oak.lodie.table.rep;

import java.io.Serializable;

/**
 */
public class Key_SubjectCol_ObjectCol implements Serializable {
    private int subjectCol;
    private int objectCol;

    public Key_SubjectCol_ObjectCol(int subjectCol, int objectCol) {
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
        if(o instanceof Key_SubjectCol_ObjectCol){
            Key_SubjectCol_ObjectCol k = (Key_SubjectCol_ObjectCol)o;
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
