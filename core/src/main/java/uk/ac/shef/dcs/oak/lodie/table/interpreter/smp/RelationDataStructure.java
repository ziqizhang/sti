package uk.ac.shef.dcs.oak.lodie.table.interpreter.smp;

import uk.ac.shef.dcs.oak.util.ObjObj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zqz on 21/04/2015.
 */
public class RelationDataStructure {

    private Map<String, Map<Integer, List<ObjObj<String, Double>>>> allRelationCandidates;

    public RelationDataStructure() {
        allRelationCandidates = new HashMap<String, Map<Integer, List<ObjObj<String, Double>>>>();
    }

    protected Map<String, Map<Integer, List<ObjObj<String, Double>>>> getAllRelationCandidates(){
        return allRelationCandidates;
    }

    public Map<Integer, List<ObjObj<String, Double>>> getRCBetweenColumns(int subject, int object) {
        return allRelationCandidates.get(getKey(subject, object));
    }

    public List<ObjObj<String, Double>> getRCBetweenColumnsOnRow(int subject, int object, int row) {
        Map<Integer, List<ObjObj<String, Double>>> relationsBetweenColumns=getRCBetweenColumns(subject, object);
        if(relationsBetweenColumns==null)
            return null;
        return relationsBetweenColumns.get(row);
    }

    public void addRCBetweenColumnsOnRow(int subject, int object, int row, List<ObjObj<String, Double>> relations) {
        Map<Integer, List<ObjObj<String, Double>>> relationCandidates = getRCBetweenColumns(subject, object);
        boolean manualUpdate = false;
        if (relationCandidates == null) {
            relationCandidates = new HashMap<Integer, List<ObjObj<String, Double>>>();
            manualUpdate = true;
        }
        relationCandidates.put(row, relations);
        if (manualUpdate)
            addRCBetweenColumns(subject, object, relationCandidates);
    }

    public void addRCBetweenColumns(int subject, int object, Map<Integer, List<ObjObj<String, Double>>> relationCandidates) {
        this.allRelationCandidates.put(getKey(subject, object), relationCandidates);
    }

    private String getKey(int subjectColumn, int objectColumn) {
        return subjectColumn + "," + objectColumn;
    }

}
