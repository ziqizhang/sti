package uk.ac.shef.dcs.oak.sti.algorithm.tm.stopping;

/**

 */
public class StoppingCriteriaInstantiator {

    public static StoppingCriteria instantiate(String stopper_class, String[] params){
        if(stopper_class.equals(EntropyConvergence.class.getName())){
            return new EntropyConvergence(Double.valueOf(params[0]),
                    Integer.valueOf(params[1]),
                    Double.valueOf(params[2]));
        }

        if(stopper_class.equals(NoStop.class.getName())){
            return new NoStop();
        }
        /*if(stopper_class.equals(LongtailPatternConfirmation.class.getName())){
            return new LongtailPatternConfirmation(Double.valueOf(params[0]),
                    Integer.valueOf(params[1]));
        }*/
        if(stopper_class.equals(FixedNumberOfRows.class.getName())){
            return new FixedNumberOfRows(Integer.valueOf(params[0]));
        }
        return null;
    }
}
