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

package com.sindice.query.v2;

import com.sindice.query.SearchQuery;

/**
 * This class models a term query.
 * 
 * @author Milan Stancovic (milstan@hypios.com)
 */
public class TermQuery extends SearchQuery {

    private boolean sortByDate; 

	private String term;

	public TermQuery(String sindiceEndpoint, String term, boolean sortByDate) {
		super(sindiceEndpoint + "/v2/search?q=%s&qt=term&page=%s");
        //sindiceEndpoint=
		if(term == null) {
            throw new IllegalArgumentException("Search term cannot be null");
        }
		this.term = term;
		this.sortByDate = sortByDate;
	}

	@Override
	protected String formURL(int page) {
        return String.format(
                sindiceEndpoint,
                encode(term),
                page
        );
	}

}
