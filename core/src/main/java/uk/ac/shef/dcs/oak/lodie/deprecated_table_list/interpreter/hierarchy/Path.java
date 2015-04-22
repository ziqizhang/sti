package uk.ac.shef.dcs.oak.lodie.deprecated_table_list.interpreter.hierarchy;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 26/04/13
 * Time: 17:25
 */
public class Path {
    private List<Step> steps = new ArrayList<Step>();

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }
}
