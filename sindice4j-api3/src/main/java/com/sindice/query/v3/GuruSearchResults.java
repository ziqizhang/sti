package com.sindice.query.v3;

import com.sindice.SindiceException;
import com.sindice.result.SearchResults;

/**
 * Author: Ziqi Zhang (z.zhang@dcs.shef.ac.uk)
 * Date: 24/10/12
 * Time: 11:37
 */
public class GuruSearchResults extends SearchResults {
    /**
     * Constructor for the SearchResults object
     *
     * @param query
     */
    public GuruSearchResults(GuruQuery query) {
        super(query);
    }

    /**
     * @return true if there is another page of results next
     */
    public boolean hasNextPage() {
        return ((GuruQuery) getQuery()).getStart() + getResultsPerPage() < getTotalResults();
    }


    /*
    the param for doQuery of GuruQuery is not used but rather just an indicator of the current page.
    the pagination is instead dealt with by the "start" and "count" params in the GuruQuery class
     */
    public GuruSearchResults nextPage() throws SindiceException {
        GuruQuery q = (GuruQuery) getQuery();
        q.setStart(q.getStart() + getResultsPerPage());
        return q.doQuery(getCurrentPage() + 1);
    }

    /**
     * @return previous page of results
     * @throws SindiceException if something goes wrong while contacting <i>Sindice.com</i> and processing its output
     */
    public GuruSearchResults previousPage() throws SindiceException {
        GuruQuery q = (GuruQuery) getQuery();
        q.setStart(q.getStart() - getResultsPerPage());
        return q.doQuery(getCurrentPage() - 1);
    }
}
