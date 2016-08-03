package uk.ac.shef.dcs.sti.io;

import uk.ac.shef.dcs.sti.core.model.TColumnColumnRelationAnnotation;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by - on 23/06/2016.
 */
public class JSONOutputRelationAnnotation implements Serializable{

    private static final long serialVersionUID = -120847113231474692L;
    private int fromColumnIndex;
    private int toColumnIndex;
    private List<String[]> candidates;

    public JSONOutputRelationAnnotation(int fromColumnIndex, int toColumnIndex){
        this.fromColumnIndex=fromColumnIndex;
        this.toColumnIndex=toColumnIndex;
        candidates=new ArrayList<>();
    }

    public void add(TColumnColumnRelationAnnotation candidate){
        String[] values = new String[3];

        values[0] = candidate.getRelationURI();
        values[1] = candidate.getRelationLabel();
        values[2] = String.valueOf(candidate.getFinalScore());

        candidates.add(values);
    }
}
