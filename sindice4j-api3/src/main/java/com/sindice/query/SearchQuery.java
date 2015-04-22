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

package com.sindice.query;

import com.sindice.SindiceException;
import com.sindice.result.SearchResult;
import com.sindice.result.SearchResults;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * It models a generic query to <i>Sindice</i>.
 *
 * @author Milan Stankovic (milstan@gmail.com)
 */
public abstract class SearchQuery extends Query {

    public SearchQuery(String endpoint) {
        super(endpoint);
    }

    public SearchResults doQuery() throws SindiceException {
        return doQuery(1);
    }

    /**
     * Perform the query.
     *
     * @param page the maximum number of results per page
     * @return {@link com.sindice.result.SearchResults} wrapping results.
     */
    public SearchResults doQuery(int page) throws SindiceException {
        SearchResults results = new SearchResults(this);
        results.setCurrentPage(page);
        JSONObject main = performGETQueryAndParseResult(formURL(page));

        results.setTotalResults(((Long) main.get("totalResults")).intValue());
        JSONArray entities = (JSONArray) main.get("entries");
        for (Object object : entities) {
            JSONObject obj = (JSONObject) object;
            SearchResult res = new SearchResult();
            res.setLink((String) obj.get("link"));
            results.add(res);
            try {
                res.setUpdated((String) obj.get("updated"));
            } catch (ParseException e) {
                throw new SindiceException(
                        "A problem occured while processing Sindice output." +
                                " Please report the issue to Sindice.",
                        e
                );
            }
            JSONArray titles = (JSONArray) obj.get("title");
            res.setTitle((String) titles.get(0));
            JSONArray formats = (JSONArray) obj.get("formats");
            for (Object object2 : formats) {
                res.addFormat((String) object2);
            }
            String triplesSize = (String) obj.get("content");
            Pattern p = Pattern
                    .compile("(\\d+) triples in (\\d+) bytes");
            Matcher mathcer = p.matcher(triplesSize);
            if (mathcer.find()) {
                res.setTiples(Integer.parseInt(mathcer.group(1)));
                res.setBytes(Integer.parseInt(mathcer.group(2)));
            }
        }
        results.setResultsPerPage(((Long) main.get("itemsPerPage"))
                .intValue());
        return results;
    }

    protected abstract String formURL(int page);

}