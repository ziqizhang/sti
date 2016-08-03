package uk.ac.shef.dcs.kbsearch.freebase;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

import java.io.IOException;
import java.util.Date;

/**
 */
public class FreebaseQueryInterrupter {

    private Date timestamp_firstQueryToday;
    private Date timestamp_lastQueryToday;
    private int countingQuery_today;
    private Date timestamp_firstQueryInTenSeconds;
    private Date timestamp_lastQueryInTenSeconds;
    private int countingQuery_inTenSeconds;

    private int maxQueriesPerSecond;
    private int maxQueriesPerDay;

    public FreebaseQueryInterrupter(int maxQPerSec, int maxQPerDay) {
        this.maxQueriesPerDay = maxQPerDay;
        this.maxQueriesPerSecond = maxQPerSec;
    }

    public HttpResponse executeQuery(HttpRequest request, boolean forceWait1Second) throws IOException {
        if (countingQuery_inTenSeconds > maxQueriesPerSecond) {
            Date now = new Date();
            if (now.getTime() > timestamp_lastQueryInTenSeconds.getTime()) { //we passed the "n query per second" timing threshold
                //ok reset
                resetSecondLimiter();
            } else {
                System.out.println("Too many requests in a second, wait for 10 seconds.");
                waitFor(10);
                resetSecondLimiter();
            }
        }
        if (countingQuery_today > maxQueriesPerDay) {
            Date now = new Date();
            long wait = timestamp_lastQueryToday.getTime() - now.getTime();
            if (wait < 0) {
                //ok
                resetDayLimiter();
            } else {
                System.out.println("Too many requests in a day, wait for:" + (wait / 1000) + " seconds");
                waitFor(wait / 1000);
                resetDayLimiter();
            }
        }

        if (timestamp_firstQueryInTenSeconds == null) {
            resetSecondLimiter();
        }
        if (timestamp_firstQueryToday == null) {
            resetDayLimiter();
        }


        //successfully completed this query

        countingQuery_inTenSeconds++;
        countingQuery_today++;
        HttpResponse response=request.execute();
        if(forceWait1Second){
            try{
                Thread.sleep(1000);
            }catch (Exception e){}
        }

        return response;
    }

    private void resetSecondLimiter() {
        timestamp_firstQueryInTenSeconds = new Date();
        timestamp_lastQueryInTenSeconds = new Date(timestamp_firstQueryInTenSeconds.getTime() + (10 * 1000));
        countingQuery_inTenSeconds = 0;
    }

    private void resetDayLimiter() {
        timestamp_firstQueryToday = new Date();
        timestamp_lastQueryToday = new Date(timestamp_firstQueryToday.getTime() + (86400 * 1000));
        countingQuery_today = 0;
    }

    private void waitFor(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
        }
    }
}
