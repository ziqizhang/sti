package uk.ac.shef.dcs.oak.lodie.seeding;

import com.sindice.query.v3.GuruSearchResults;
import com.sindice.result.SearchResult;
import uk.ac.shef.dcs.oak.lodie.architecture.*;
import uk.ac.shef.dcs.oak.triplesearch.TripleSearchException;
import uk.ac.shef.dcs.oak.triplesearch.sindiceapi.SindiceAPIProxy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 23/10/12
 * Time: 10:57
 */
public class SeedIdentifierSindice implements SeedIdentifier {

    private static final Logger logger = Logger.getLogger(SeedIdentifierSindice.class.getName());

    protected String[] documentTypes; //types of documents to be returned, e.g., RDFA etc. see the list on sindice website
    protected int resultsPerPage = 50;
    protected int maxPages = 100;
    protected SindiceAPIProxy proxy;

    protected long taskSchedulerMaxWait;
    protected int taskSchedulerMaxResult;
    protected final int MAX_WORKERS = 1;

    public SeedIdentifierSindice(String... documentTypes) {
        this(50, 100, 60 * 60000, 1000, documentTypes);
    }

    public SeedIdentifierSindice(int resultsPerPage, int maxPages,
                                 long maxSchedulerWait, int maxResults, String... documentTypes) {
        this.resultsPerPage = resultsPerPage;
        this.maxPages = maxPages;
        this.documentTypes = documentTypes;
        this.taskSchedulerMaxWait = maxSchedulerWait;
        this.taskSchedulerMaxResult = maxResults;
        proxy = new SindiceAPIProxy();
    }

    @Override
    public Dataset<Triple> findTSeed(LearningJob job) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Dataset<Document> findDSeed(LearningJob job) {
        List<String> sindiceQueries = toSindiceTripleQuery(job);
        List<String> nextSindiceQueries = new ArrayList<String>();

        SchedulerSindiceResProcessor scheduler = new SchedulerSindiceResProcessor(MAX_WORKERS, taskSchedulerMaxWait, taskSchedulerMaxResult);

        /*
                 record current position and next batch size. each time 100 pages are retrieved
                 do{
                    for each in sindiceQueries
                        query sequentially
                        check #found, greater then current position?
                            yes - nextsindicequeries add this query
                            no  - do nothing
                        pass the results found to the extraction scheduler
                 */
        int count = 0;
        for (int page = 0; page < maxPages + 1; page++) {
            for (String query : sindiceQueries) {
                try {
                    GuruSearchResults results =
                            proxy.search("",
                                    query,
                                    SindiceAPIProxy.toStringDocTypes(documentTypes),
                                    resultsPerPage * page,
                                    resultsPerPage);
                    int total = results.getTotalResults();
                    int currentShowing = results.getCurrentPage() * results.getResultsPerPage();
                    if (total > currentShowing)
                        nextSindiceQueries.add(query);

                    //process results using a multi-thread scheduler
                    Iterator<SearchResult> it = results.iterator();
                    while (it.hasNext()) {
                        SearchResult sr = it.next();
                        WorkerThreadTableProcessor worker = new WorkerThreadTableProcessor(sr, query);//todo fill in keywords
                        worker.setId(String.valueOf(count));
                        //System.out.println(count+" search result adding...");
                        scheduler.process(worker);
                        count++;
                        if (scheduler.stop()) {
                            System.err.println("Scheduler stopping");

                            scheduler.stopWorkers();
                            return (Dataset<Document>) scheduler.getResult();
                        }
                    }
                } catch (TripleSearchException e) {
                    logger.warning("Skipped because of exception occurred on query " + e.getMessage());
                }
            }
            sindiceQueries = new ArrayList<String>(nextSindiceQueries);
            nextSindiceQueries.clear();
        }

        scheduler.stopWorkers();
        return (Dataset<Document>) scheduler.getResult();
    }


    protected List<String> toSindiceTripleQuery(LearningJob job) {
        List<String> queries = new ArrayList<String>();
        String s = null, o = null;
        if (!job.isObjectOpen())
            o = "";
        else if (!job.isSubjectOpen())
            s = "";
        if (s == null && o == null) { //both s and o are open, i.e., "* <relation> *"
            queries.add("* " + job.getPredicate() + " *");
            return queries;
        }

        for (String constraint : job.getConstraints()) {
            if (s == null) {
                queries.add("* " + job.getPredicate() + " " + constraint);
            } else if (o == null) {
                queries.add(constraint + " " + job.getPredicate() + " *");
            }
        }
        return queries;
    }

    protected List<String> toSindiceSPARQLQuery(LearningJob job) {
        //todo
        return new ArrayList<String>();
    }

}
