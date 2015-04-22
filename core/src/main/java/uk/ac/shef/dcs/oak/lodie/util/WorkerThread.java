package uk.ac.shef.dcs.oak.lodie.util;

import java.util.Observable;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 23/10/12
 * Time: 15:05
 */
public abstract class WorkerThread extends Observable implements Runnable {

    protected String id;

    @Override
    public void run() {
        //System.out.println("\t\tworker started: "+getId());
        Object output = process();
        setChanged();
        notifyObservers(output);
        //System.out.println("\t\tworker processed: " + getId());
    }

    /*
    must be implemented by subclasses. This method deals with actual tasks to be processed
     */
    public abstract Object process();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
