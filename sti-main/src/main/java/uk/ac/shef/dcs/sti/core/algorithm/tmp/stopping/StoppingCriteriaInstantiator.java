package uk.ac.shef.dcs.sti.core.algorithm.tmp.stopping;

/**

 */
public class StoppingCriteriaInstantiator {

    public static StoppingCriteria instantiate(String stopper_class, String[] params)
    throws ClassNotFoundException{
        if(stopper_class.equals(IInf.class.getName())){
            return new IInf(Double.valueOf(params[0].trim()),
                    Integer.valueOf(params[1].trim()),
                    Double.valueOf(params[2].trim()));
        }

        if(stopper_class.equals(NoStop.class.getName())){
            return new NoStop();
        }
        /*if(stopper_class.equals(LongtailPatternConfirmation.class.getName())){
            return new LongtailPatternConfirmation(Double.valueOf(params[0]),
                    Integer.valueOf(params[1]));
        }*/
        if(stopper_class.equals(FixedNumberOfRows.class.getName())){
            return new FixedNumberOfRows(Integer.valueOf(params[0].trim()));
        }
        else{
            throw new ClassNotFoundException("Class:"+stopper_class);
        }
    }
}
