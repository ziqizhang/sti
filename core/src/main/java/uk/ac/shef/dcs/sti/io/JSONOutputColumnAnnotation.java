package uk.ac.shef.dcs.sti.io;
import uk.ac.shef.dcs.sti.core.model.TColumnHeaderAnnotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by - on 23/06/2016.
 */
public class JSONOutputColumnAnnotation implements Serializable {
    private static final long serialVersionUID = -120841111410474692L;
    private int columnIndex;
    private String columnText;
    private List<String[]> candidates;

    public JSONOutputColumnAnnotation(int columnIndex, String columnText){
        this.columnIndex=columnIndex;
        candidates=new ArrayList<>();
        this.columnText=columnText;
    }

    public void add(TColumnHeaderAnnotation candidate){
        String[] values = new String[3];

        values[0] = candidate.getAnnotation().getId();
        values[1] = candidate.getAnnotation().getLabel();
        values[2] = String.valueOf(candidate.getFinalScore());

        candidates.add(values);
    }
}
