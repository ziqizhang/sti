package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 12/03/13
 * Time: 11:28
 */
public class LTableColumn2ColumnRelation {

    private int mainSubjectCol;
    private int objectCol;
    private List<TripleInTableRow> triples = new ArrayList<TripleInTableRow>();

    public LTableColumn2ColumnRelation(int mainSubjectCol, int objectCol){
        this.mainSubjectCol=mainSubjectCol;
        this.objectCol=objectCol;
    }

    public int getMainSubjectCol() {
        return mainSubjectCol;
    }

    public void setMainSubjectCol(int mainSubjectCol) {
        this.mainSubjectCol = mainSubjectCol;
    }

    public int getObjectCol() {
        return objectCol;
    }

    public void setObjectCol(int objectCol) {
        this.objectCol = objectCol;
    }

    public List<TripleInTableRow> getTriples() {
        return triples;
    }

    public void addTriple(TripleInTableRow t){
        triples.add(t);
    }
}
