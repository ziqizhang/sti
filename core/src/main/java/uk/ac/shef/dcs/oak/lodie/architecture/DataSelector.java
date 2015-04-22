package uk.ac.shef.dcs.oak.lodie.architecture;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 22/10/12
 * Time: 16:25
 */
public interface DataSelector {

    Dataset select(Dataset data);

    boolean filter(Object o);
}
