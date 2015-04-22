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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * For the named field, filter to include / exclude only the items matching the given regular
 * expression. This is useful for <code>explicit_content</code> and <code>implicit_content</code>
 * fields to include/exclude only the desired triples. If more than one filter is provided for the
 * same field, an item will be filtered if it matches any query.
 * Filters are not case sensitive.
 *
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class FieldRegexFilter implements HTTPParameter {

    /**
     * Type of filter.
     */
    public enum Type {
        /** Denotes an inclusion filter, all matches are included in result.   */
        include,
        /** Denotes an exclusion filter, all matches are excluded from result. */
        exclude
    }

    private Type filterType;

    /**
     * Name of affected field.
     */
    private String fieldName;

    /**
     * Applied regex filter.
     */
    private String regexFilter;

    /**
     * Constructor accepting the field name and the regex filter.
     *
     * @param filterType the type of filter, for inclusion or exclusion.
     * @param fieldName name of the field to be filtered.
     * @param regexFilter <i>Java</i> regular expression to be applied. 
     */
    public FieldRegexFilter(Type filterType, String fieldName, String regexFilter) {
        if(filterType == null) {
            throw new IllegalArgumentException("filter type cannot be null");
        }
        if(fieldName == null) {
            throw new IllegalArgumentException("field name cannot be null.");
        }
        if(fieldName.length() == 0) {
            throw new IllegalArgumentException("field name cannot be 0 length");
        }
        try {
            Pattern.compile(regexFilter);
        } catch (PatternSyntaxException pse) {
            throw new IllegalArgumentException( String.format("Invalid regex filter '%s'", regexFilter), pse);
        }
        this.filterType  = filterType;
        this.fieldName   = fieldName;
        this.regexFilter = regexFilter;
    }

    public Type getFilterType() {
        return filterType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getRegexFilter() {
        return regexFilter;
    }

    public String getParameters() {
        //filter.explicit_content.include.regex=(<REGEX>)
        //filter.explicit_content.exclude.regex=(<REGEX>)
        return String.format("filter.%s.%s.regex=(%s)", fieldName, filterType, Query.encode(regexFilter) );
    }

}
