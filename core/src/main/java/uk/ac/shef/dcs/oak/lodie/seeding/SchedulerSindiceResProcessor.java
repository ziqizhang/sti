package uk.ac.shef.dcs.oak.lodie.seeding;

import uk.ac.shef.dcs.oak.lodie.architecture.Dataset;
import uk.ac.shef.dcs.oak.lodie.architecture.Document;
import uk.ac.shef.dcs.oak.lodie.util.Scheduler;

import java.util.Date;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 24/10/12
 * Time: 12:25
 */
public class SchedulerSindiceResProcessor extends Scheduler {

    private long stopAfterMaxTime; // minute_in_mili * units
    private int stopAfterMaxSize;

    private Date started;
    private Dataset<Document> result;

    public SchedulerSindiceResProcessor(int maxWorkers){

        this(maxWorkers,60000*60, 1000);
    }

    public SchedulerSindiceResProcessor(int maxWorkers, long stopAfterMaxTime, int stopAfterMaxSize){
        super(maxWorkers);
        this.stopAfterMaxTime=stopAfterMaxTime;
        this.stopAfterMaxSize=stopAfterMaxSize;
        started=new Date();
        result= new Dataset<Document>();
    }

    @Override
    public void consolidate(Object newResult) {
        result.add((Document)newResult);
    }

    @Override
    public Object getResult() {
        return result;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean stop() {
        Date current = new Date();
        if(current.getTime()-started.getTime()>=stopAfterMaxTime){
            System.out.println("Scheduler stopping, timeout");
            return true;
        }
        if(result.size()>=stopAfterMaxSize){
            System.out.println("Scheduler stopping, size reached-"+result.size());
            return true;
        }
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
