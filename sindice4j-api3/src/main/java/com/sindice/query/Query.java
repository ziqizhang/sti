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
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Base class for any query performed on the <i>Sindice API</i> endpoint.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public abstract class Query {

    /**
     * Prefix of Sindice endpoint. 
     */
    protected String sindiceEndpoint;// = "http://api.sindice.com";

    public Query(String endpoint){
        this.sindiceEndpoint =endpoint;
    }

    /**
     * URL encodes the given <i>arg</i> string.
     *  
     * @param arg input string to be encoded.
     * @return encoded string result.
     */
    protected static String encode(String arg) {
        String encodedArg;
		try {
			encodedArg = URLEncoder.encode(arg, "UTF-8");
		} catch (UnsupportedEncodingException uee) {
			throw new IllegalStateException("This should never happen.");
		}
        return encodedArg;
	}

    /**
     * Performs a <i>GET</i> request on the given <i>urlString</i> and returns the
     * parsed <i>JSON</i> object result. 
     *
     * @param urlString query string.
     * @return JSON object containing the GET response.
     * @throws SindiceException if an error occurs during the
     *                          connection or parsing of the result. 
     */
    protected static JSONObject performGETQueryAndParseResult(String urlString)
    throws SindiceException {
        final URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new SindiceException("" +
                    "Unexpected exception with the Sindice API URI. " +
                    "This should not happen. " +
                    "Please report a bug on sindice4j project.",
                    e
            );
        }
        URLConnection con;
        try {
            con = url.openConnection();
        } catch (IOException e) {
        	 throw new SindiceException("Something wrong happened on server side.", e);
        }
        con.setRequestProperty("accept", "application/json");
        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } catch (IOException e) {
        	throw new SindiceException("A problem occurred while accessing the output from Sindice.", e);
        }
        try {
            return (JSONObject) JSONValue.parse(in);
        } catch (Exception e) {
            throw new SindiceException("An error occurred while parsing the JSON response.", e);
        }
    }
}
