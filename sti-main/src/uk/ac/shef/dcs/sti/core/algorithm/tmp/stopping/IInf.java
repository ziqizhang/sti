package uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Map;

/**
MUST BE RE-INSTANTIATED FOR EVERY INTERPRETATION TASK, because class variable "current iteration" and "previous
 entropy" does not reset
 */
public class IInf extends StoppingCriteria {

    private double minimum_state_score_sum;
    private double previous_iteration_entropy;
    private int minimum_iterations;
    private int current_iteration;
    private double convergence_threshold = 0.01;

    private static final Logger LOG = Logger.getLogger(IInf.class.getName());

    //minimum #
    public IInf(double minimum_state_score_sum, int minimum_iterations, double convergence_threshold) {
        this.minimum_state_score_sum = minimum_state_score_sum;
        this.minimum_iterations = minimum_iterations;
        current_iteration = 0;
        this.convergence_threshold = convergence_threshold;
    }

    @Override
    public boolean stop(Map<Object, Double> state, int max) {
        current_iteration++;

        //evaluate the state by calculating entropy
        double sum = 0.0;
        for (Double d : new HashSet<>(state.values()))
            sum += d;
        double entropy = 0.0;
        if (state.size() > 1) {
            for (Map.Entry<Object, Double> e : state.entrySet()) {
                if(e.getValue()==0)
                    continue;
                double p_a = e.getValue() / sum;
                double log_p_a = Math.log(p_a);

                entropy = entropy + (0 - p_a * log_p_a);
            }
        }
        //is it converged?
        boolean has_converged = false;

        if (previous_iteration_entropy != 0) {
            has_converged = Math.abs(entropy - previous_iteration_entropy) < convergence_threshold;
        }
        previous_iteration_entropy = entropy;

        if (current_iteration < minimum_iterations)
            return false;
        if (sum < minimum_state_score_sum)
            return false;

        if (has_converged){
            LOG.debug("\tConvergence iteration=" + current_iteration + ", potential max=" + max + ", savings=" + ((double) (max - current_iteration) / max));
            /*if(current_iteration>20)
                System.out.println();*/
            //previous_iteration_entropy=0.0;//reset
            //current_iteration=0;
            return true;
        }

        if(current_iteration==max)
            LOG.debug("\t(negative)Convergence iteration="+current_iteration+", potential max="+max+", savings="+((double)(max-current_iteration)/max));

        return false;
    }
}
