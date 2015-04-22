package uk.ac.shef.dcs.oak.lodie.util;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 23/10/12
 * Time: 15:24
 */
public abstract class Scheduler implements Observer {

    protected int maxWorkers = 10;
    protected static final int THREAD_SCALOR = 5;
    protected ThreadPoolExecutor executor;
    protected boolean paused;

    public Scheduler() {
        this(Runtime.getRuntime().availableProcessors() * THREAD_SCALOR);

    }

    public Scheduler(int maxWorkers) {
        this.maxWorkers = maxWorkers;
        executor = new ThreadPoolExecutor(maxWorkers, maxWorkers, 1,
                TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.allowCoreThreadTimeOut(true);
    }

    public void process(WorkerThread worker) {
        while (paused) {
            try {
                //System.out.println("waiting...queue full:"+(executor.getMaximumPoolSize()-executor.getPoolSize()));
                Thread.sleep(1000);
            } catch (Exception ignore) {
            }
        }

        //System.out.println("\tSceduler added...");
        worker.addObserver(this);
        executor.execute(worker);
        if(executor.getPoolSize()==executor.getMaximumPoolSize())
            paused=true;
        //System.out.println("\t remains: "+(executor.getMaximumPoolSize()-executor.getPoolSize()));

    }

    /*
     * o is the observed object, in this case, the WorkThread instance that notifies Scheduler up completing its task;
     * result is the object returned by the "process()" method in WorkThread.
     *
     * See Observor/Observable pattern for details
     *
     * Upon notification of completion, consolidate the result produced by this worker
     */
    public void update(Observable o, Object result) {
        paused=false;
        consolidate(result);
        //System.out.println("\tScheduler notified by worker, queue remaining: "+(executor.getMaximumPoolSize()-executor.getPoolSize()));
    }

    public void stopWorkers() {
        executor.shutdownNow();
    }

    public abstract void consolidate(Object result);

    public abstract Object getResult();

    public abstract boolean stop();


}
