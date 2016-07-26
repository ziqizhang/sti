package uk.ac.shef.dcs.sti.io;

import uk.ac.shef.dcs.sti.core.model.TCellCellRelationAnotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by - on 23/06/2016.
 */
public class JSONOutputRelationAnnotationPerRow implements Serializable{
    private static final long serialVersionUID = -120847113231474692L;
    private int fromColumnIndex;
    private int toColumnIndex;
    private int rowIndex;
    private List<String[]> candidates;

    public JSONOutputRelationAnnotationPerRow(int fromColumnIndex, int toColumnIndex, int rowIndex){
        this.fromColumnIndex=fromColumnIndex;
        this.toColumnIndex=toColumnIndex;
        this.rowIndex=rowIndex;
        candidates=new ArrayList<>();
    }

    public void add(TCellCellRelationAnotation candidate){
        String[] values = new String[3];

        values[0] = candidate.getRelationURI();
        values[1] = candidate.getRelationLabel();
        values[2] = String.valueOf(candidate.getWinningAttributeMatchScore());

        candidates.add(values);
    }
}
