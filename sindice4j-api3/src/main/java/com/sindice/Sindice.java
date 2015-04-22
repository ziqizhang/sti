/*
 * Copyright 2010 Milan Stankovic <milstan@hypios.com>
 * Hypios.com, STIH, University Paris-Sorbonne &
 * Davide Palmisano,  Fondazione Bruno Kessler <palmisano@fbk.eu>
 * Michele Mostarda,  Fondazione Bruno Kessler <mostarda@fbk.eu>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sindice;

import com.sindice.query.v2.LiveQuery;
import com.sindice.query.v3.CacheQuery;
import com.sindice.query.v2.TermQuery;
import com.sindice.result.CacheResult;
import com.sindice.result.LiveResult;
import com.sindice.result.SearchResults;

/**
 * This class is the main facade towards the <i>Sindice.com</i>
 * Web API.
 * 
 * @author Milan Stankovic (milstan@gmail.com)
 */
@Deprecated
public class Sindice {

    private String endpoint;

    public Sindice(String endpoint)
    {
        this.endpoint=endpoint;
    }


    /**
     * Method for performing term search.
     * Returns all the documents containing the specified term.
     * Returns a page of results in the form of a
     * {@link com.sindice.result.SearchResults} object.
     * For navigating through pages you can use the nextPage() and previousPage()
     * methods of the SearchResults class.
     * {@link com.sindice.result.SearchResults} object is in fact a
     * list of {@link com.sindice.result.SearchResults} objects.
     * Search results are ordered by date, and the first page
     * of results is provided first by default.
     *
     * @param term - the search term
     * @return {@link com.sindice.result.SearchResults} object,
     * first page of search, with results ordered by date is provided by default
     * @throws SindiceException if something goes wrong while contacting <i>Sindice.com</i> and processing its output 
     */
    public SearchResults termSearch(String term) throws SindiceException {
        return termSearch(term, true);
    }


    /**
     * Method for performing term search. Returns all the
     * documents containing the specified term.
     * Returns a page of results in the form of a
     * {@link com.sindice.result.SearchResults} object.
     * For navigating through pages you can use the
     * nextPage() and previousPage() methods of the
     * {@link com.sindice.result.SearchResults} class.
     * {@link com.sindice.result.SearchResults}
     * object is in fact a list of
     * {@link com.sindice.result.SearchResults objects}.
     * First page of results is provided first by default.
     *
     * @param term       - the search term
     * @param sortByDate - true or false depending of the desire to sort results by date
     * @return SearchResults object, first page of search is provided by default
     * @throws SindiceException if something goes wrong while contacting <i>Sindice.com</i> and processing its output 
     */
    public SearchResults termSearch(String term, boolean sortByDate) throws SindiceException {
        return new TermQuery(endpoint, term, sortByDate).doQuery();
    }


    /**
     * Method for performing term search.
     * Returns all the documents containing the specified term.
     * Returns a page of results in the form of a
     * {@link com.sindice.result.SearchResults}
     * object. For navigating through pages you can use
     * the nextPage() and previousPage() methods of the
     * {@link com.sindice.result.SearchResults} class.
     * {@link com.sindice.result.SearchResults} object
     * is in fact a list of SreachResult objects.
     *
     * @param term       - the search term
     * @param sortByDate - true or false depending if you want to order search results by date
     * @param page       - the desired page of search results
     * @return SearchResults object
     * @throws SindiceException if something goes wrong while contacting <i>Sindice.com</i> and processing its output
     */
    public SearchResults termSearch(String term, boolean sortByDate, int page) throws SindiceException {
        return new TermQuery(endpoint, term, sortByDate).doQuery(page);
	}

    /**
     * Method for cache querying.
     * Returns the the result matching the <i>query</i> parameter.
     *
     * @param query the query to be submitted to the cache.
     * @return the query result.
     * @throws SindiceException if an error occurs during the cache quering.
     */


}
