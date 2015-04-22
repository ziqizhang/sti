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

package com.sindice.query.v3;

import com.sindice.SindiceException;
import com.sindice.query.FieldRegexFilter;
import com.sindice.query.HTTPParameter;
import com.sindice.query.Query;
import com.sindice.result.CacheResult;
import com.sindice.result.CacheResultMapper;
import org.json.simple.JSONObject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Specific query implementation to access the <i>Sindice Cache</i> service.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class CacheQuery extends Query {

    /**
     * The response output format.
     */
    public enum ResponseFormat {
        json /** JSON format */
    }

    /**
     * Returns the name of the <i>JSON</i> mapping associated to the given field.
     *
     * @param f input field to be mapped.
     * @return the field mapping.
     */
    protected static String getFieldName(Field f) {
        ParameterMapping pm = f.getAnnotation(ParameterMapping.class);
        if(pm == null) {
            return f.getName();
        }
        final String mapping = pm.value();
        if(mapping.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid parameter mapping for field " + f);
        }
        return mapping;
    }

    /**
     * Creates the <i>HTTP</i> parameters string for the given object.
     * The parameters string is built of a sequence of couples [&lt;param-name&gt;=&lt;param-value&gt;&amp;]+,
     * one for every object field, where <i>param-name</i> is the mapping of the field and <i>param-value</i>
     * is the value of the field.
     *
     * @param obj object used to generate the parameters string.
     * @return string of parameters.
     * @throws IllegalAccessException
     */
    public static String getParametersString(Object obj)
    throws IllegalAccessException {
        final Class objectType = obj.getClass();
        if (objectType.isPrimitive() || objectType.equals(String.class)) {
            return obj.toString();
        }
        Field[] fields = obj.getClass().getDeclaredFields();
        StringBuilder parameters = new StringBuilder();
        for(Field field : fields) {
            field.setAccessible(true);
            final Class fieldType = field.getType();
            final String fieldName = getFieldName(field);
            final Object fieldValue = field.get(obj);
            if(fieldValue == null) {
                continue;
            }
            if (fieldType.isPrimitive() || fieldType.equals(String.class)) {
                parameters.append(fieldName);
                parameters.append('=');
                parameters.append( encode(fieldValue.toString()) );
                parameters.append('&');
            } else if(fieldType.isEnum()) {
                parameters.append(fieldName);
                parameters.append('=');
                parameters.append( encode(fieldValue.toString()) );
                parameters.append('&');
            } else if(fieldType.isArray())  {
                final Object array = field.get(obj);
                for(int i = 0; i < Array.getLength(array); i++) {
                    Object arrayElem =  Array.get(array, i);
                    if(arrayElem instanceof HTTPParameter) {
                         parameters.append( ((HTTPParameter) arrayElem).getParameters() );
                        parameters.append('&');
                    } else {
                        parameters.append(fieldName);
                        parameters.append('=');
                        parameters.append( getParametersString(arrayElem) );
                        parameters.append('&');
                    }
                }
            } else if(fieldType.equals(HTTPParameter.class)) {
                final HTTPParameter parameter = (HTTPParameter) fieldValue;
                parameters.append( encode(parameter.getParameters()) );
                parameters.append('&');
            } else {
                throw new IllegalArgumentException("Unsupported field type " + fieldType);
            }
        }
        return parameters.toString();
    }

    /**
     * The url of the page to be retrieved from the Sindice Data Store.
     * More than one url argument may be provided.
     * <b>Required</b>
     */
    @ParameterMapping("url")
    private String url;

    /**
     * The names of the fields to be included in the results.
     * More than one field argument may be provided.
     * If no field argument is provided all fields will appear in the results.
     * <b>Optional</b>
     */
    @ParameterMapping("field")
    private String[] fields;

    /**
     * The response format.
     * Only <i>JSON</i> output format is supported.
     * <b>Optional</b>
     */
    @ParameterMapping("output")
    private ResponseFormat output;

    /**
     * Whether to format output for easier reading by people.
     * If set to <code>true</code> the output will appear nicely formatted.
     * <b>Optional</b>
     */
    @ParameterMapping("pretty")
    private boolean pretty;

    /**
     * If callback parameter is provided,
     * the result will be wrapped with <code>callback=()</code>.
     * <b>Optional</b>
     */
    @ParameterMapping("callback")
    private String callback;

    /**
     * Regex field filter to mark content inclusion.
     * <b>Optional</b>
     */
    private FieldRegexFilter[] fieldRegexFilters;

    /**
     * Constructor.
     *
     * @param url the page URL to be retrieved.
     * @param fields list of fields to be selected.
     * @param output the output format for the response.
     * @param pretty whether or not the output is prettified.
     * @param callback the callback to be applied to response.
     * @param fieldRegexFilters regex filters to be applied.
     */
    public CacheQuery(
            String sindiceEndpoint,
            String url,
            String[] fields,
            ResponseFormat output,
            boolean pretty,
            String callback,
            FieldRegexFilter... fieldRegexFilters
    ) {
        super(sindiceEndpoint);
        if(url == null) {
            throw new NullPointerException("url parameter cannot be null.");
        }
        if(url.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid url value.");
        }
        if(callback != null && callback.trim().length() == 0) {
            throw new IllegalArgumentException("Invalid callback parameter value.");
        }
        this.url = url;
        this.fields = fields;
        this.output = output;
        this.pretty = pretty;
        this.callback = callback;
        this.fieldRegexFilters = fieldRegexFilters;
    }

    /**
     * Constructor for default <i>JSON</i> response format.
     *
     * @param url the page URL to be retrieved.
     * @param fields list of fields to be selected.
     * @param pretty whether or not the output is prettified.
     * @param callback the callback to be applied to response.
     * @param fieldRegexFilters regex filters to be applied.
     */
    public CacheQuery(
            String sindiceEndpoint,
            String url,
            String[] fields,
            boolean pretty,
            String callback,
            FieldRegexFilter... fieldRegexFilters
    ) {
        this(sindiceEndpoint,url, fields, ResponseFormat.json, pretty, callback, fieldRegexFilters);
    }

    /**
     * Constructor for for default <i>JSON</i> response format and no prettyfication.
     *
     * @param url the page URL to be retrieved.
     * @param fields list of fields to be selected.
     * @param callback the callback to be applied to response.
     * @param fieldRegexFilters regex filters to be applied.
     */
    public CacheQuery(
            String sindiceEndpoint,
            String url,
            String[] fields,
            String callback,
            FieldRegexFilter... fieldRegexFilters
    ) {
        this(sindiceEndpoint,url, fields, false, callback, fieldRegexFilters);
    }

    /**
     * Constructor for for default <i>JSON</i> response format, no prettyfication and no callback.
     *
     * @param url the page URL to be retrieved.
     * @param fields list of fields to be selected.
     * @param fieldRegexFilters regex filters to be applied.
     */
    public CacheQuery(
            String sindiceEndpoint,
            String url,
            String[] fields,
            FieldRegexFilter... fieldRegexFilters
    ) {
        this(sindiceEndpoint,url, fields, null, fieldRegexFilters);
    }

    /**
     * Constructor for for default <i>JSON</i> response format, no prettyfication, no callback
     * and no fields selection.
     *
     * @param url the page URL to be retrieved.
     */
    public CacheQuery(String sindiceEndpoint, String url) {
        this(sindiceEndpoint, url, null);
    }

    /**
     * Performs the query represented by this object.
     *
     * @return the result of the query.
     * @throws SindiceException if an error occurs while querying
     *         the service or while retrieving the result.
     */
    public CacheResult performQuery() throws SindiceException {
        final String queryURL;
        try {
            final String parametesURL = getParametersString(this);
            queryURL = String.format("%s/v3/cache?%s", sindiceEndpoint, parametesURL);
        } catch (Exception e) {
            throw new SindiceException("An error occurred while preparing query.", e);
        }
        JSONObject main = performGETQueryAndParseResult(queryURL);
        if(main.size() != 1) {
            throw new SindiceException("Unexpected size for response object.");
        }
        final JSONObject payload;
        try {
            payload = (JSONObject) main.values().iterator().next();
        } catch (Exception e) {
            throw new SindiceException("Cannot find the object payload.", e);
        }
        return CacheResultMapper.mapToCacheResult(payload);
    }

    /**
     * This annotation allows to declare the mapping of the target
     * field with a <i>JSON</i> object key.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected @interface ParameterMapping {
        String value();
    }
}


