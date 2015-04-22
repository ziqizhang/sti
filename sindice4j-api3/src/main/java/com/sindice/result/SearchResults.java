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

package com.sindice.result;

import com.sindice.SindiceException;
import com.sindice.query.Query;
import com.sindice.query.SearchQuery;

import java.util.ArrayList;

/**
 * This class models a list of {@link com.sindice.result.SearchResult}
 * retrieved from <i>Sindice.com</i> to a given {@link com.sindice.query.Query}.
 *
 * @author Milan Stankovic (milstan@gmail.com)
 */
public class SearchResults extends ArrayList<SearchResult> {

	private static final long serialVersionUID = 1L;

	private int currentPage = 1;

	private String errorMessage = "";

	protected SearchQuery query;

	private int totalResults = 0;
	
	private int resultsPerPage=1;
	
	/**
	 * @return number of results per page
	 */
	public int getResultsPerPage() {
		return resultsPerPage;
	}

	/**
	 * setter for the number of results per page
	 * @param resultsPerPage
	 */
	public void setResultsPerPage(int resultsPerPage) {
		this.resultsPerPage = resultsPerPage;
	}

	/**
	 * @return true if there is another page of results next
	 */
	public boolean hasNextPage(){
		return ((currentPage+1) <= (totalResults/resultsPerPage));
	}

	/**
	 * Constructor for the SearchResults object
	 * @param query
	 */
	public SearchResults(SearchQuery query) {
		super();
		this.query = query;
	}

	/**
	 * getter for the current page
	 * @return current page number
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * @return error message if received by Sindice, if not returns empty string
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @return query that is used for obtaining the results
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * @return total number of results
	 */
	public int getTotalResults() {
		return totalResults;
	}

	/**
	 * @return next page of results
	 * @throws SindiceException if something goes wrong while contacting <i>Sindice.com</i> and processing its output
	 */
	public SearchResults nextPage() throws SindiceException {
		return query.doQuery(++currentPage);
	}

	/**
	 * @return previous page of results
	 * @throws SindiceException if something goes wrong while contacting <i>Sindice.com</i> and processing its output
	 */
	public SearchResults previousPage() throws SindiceException {
		return query.doQuery(--currentPage);
	}

	/**
	 * sets the current page to the given number
	 * @param currentPage
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * setter for the error message
	 * @param errorMessage
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * setter for the query object
	 * @param query
	 */
	public void setQuery(SearchQuery query) {
		this.query = query;
	}

	/**
	 * total results setter
	 * @param totalResults
	 */
	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	@Override
	public String toString() {
		return totalResults + " results: " + super.toString()
				+ getErrorMessage();
	}

}
