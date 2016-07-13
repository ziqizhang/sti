package uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zqz
 * Date: 23/01/14
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
public class NoStop extends StoppingCriteria {

    @Override
    public boolean stop(Map<Object, Double> state,int max) {
        return false;
    }
}
