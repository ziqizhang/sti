package uk.ac.shef.dcs.sti.core.algorithm;

import uk.ac.shef.dcs.sti.STIException;
import uk.ac.shef.dcs.sti.core.extension.constraints.Constraints;
import uk.ac.shef.dcs.sti.core.model.TAnnotation;
import uk.ac.shef.dcs.sti.core.model.Table;

import java.util.HashSet;
import java.util.Set;

/**
 */
public abstract class SemanticTableInterpreter {

    private Set<Integer> ignoreCols;
    private Set<Integer> mustdoColumns;

    public SemanticTableInterpreter() {

    }

    public SemanticTableInterpreter(
            int[] ignoreColumns,
            int[] mustdoColumns
    ){
        this.ignoreCols = new HashSet<>();
        for (int i : ignoreColumns)
            this.ignoreCols.add(i);
        this.mustdoColumns = new HashSet<>();
        for(int i: mustdoColumns)
            this.mustdoColumns.add(i);
    }

    public abstract TAnnotation start(Table table, Constraints constraints)
            throws STIException;

    public abstract TAnnotation start(Table table, boolean relationLearning)
            throws STIException;



    protected boolean isCompulsoryColumn(Integer i) {
        if (i != null) {
            return mustdoColumns.contains(i);
        }
        return false;
    }

    protected Set<Integer> getIgnoreColumns(){
        return ignoreCols;
    }
    
    public void setIgnoreColumns(Set<Integer> ignoreCols) {
      this.ignoreCols = ignoreCols;
    }

    protected Set<Integer> getMustdoColumns() {return mustdoColumns;}
}
