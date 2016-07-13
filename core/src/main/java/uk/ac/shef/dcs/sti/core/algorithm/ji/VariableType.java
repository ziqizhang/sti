package uk.ac.shef.dcs.sti.core.algorithm.ji;

/**
 * Created by zqz on 12/05/2015.
 */
public enum VariableType {

    CELL("cell"),
    HEADER("header"),
    RELATION("relation");

    private String label;
    VariableType(String label){
        this.label=label;
    }
    public String toString(){
        return label;
    }

}
