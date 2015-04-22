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

import java.util.Date;

/**
 * This class models the result for a {@link com.sindice.query.v3.CacheQuery}.
 *
 * @see com.sindice.query.v3.CacheQuery
 * @author Michele Mostarda (mostarda@fbk.eu)
 */
public class CacheResult implements QueryResult {

    /**
     * All the supported <i>Sindice</i> data sources.
     */
    public enum DataSource {
        /** Data collected by ping manager. */
        ping,
        /** Data collected by crawler. */
        crawl,
        /** Data collected from a dump. */
        dump,
        /** Data collected from a feed. */
        feed,
        /** Data collected using the site-manager. */
        site_manager,
        /** Data collected from sigma. */
        sigma,
        /** Data collected from other source. */
        test,
        /** Unknown source data. */
        unknown
    }

    /**
     * All the supported <i>Sindice</i> representation formats.
     */
    public enum RepresentationFormat {
        /** Original data represented in RDF/XML. */
        rdf,
        /** Original data represented in RDFa. */
        rdfa,
        /** Original data represented with Microformats. */
        microformat,
        /** Unknown format. */
        unknown
    }

    /**
     * <code>url</code>
     */
    @CacheResultMapper.JSONMapping("url")
    private String url;

    /**
     * <code>checksum</code>.
     */
    @CacheResultMapper.JSONMapping("checksum")
    private String checksum;

    /**
     * <code>class</code>.
     */
    @CacheResultMapper.JSONMapping("class")
    private String[] classes;

    /**
     * <code>dataset_uri</code>.
     * <b>WARNING (Ziqi Zhang):</b> This field has been changed to type "String[]" from "String". This is because the
     * most recent Sindice API seems to have introduced this change, i.e., there can be multiple dataset_uris. Accordingly
     * the getter method signature has been changed
     */
    @CacheResultMapper.JSONMapping("dataset_uri")
    private String[] datasetURI;

    /**
     * <code>data_source</code>.
     */
    @CacheResultMapper.JSONMapping("data_source")
    private DataSource dataSource;

    /**
     * <code>domain</code>.
     */
    @CacheResultMapper.JSONMapping("domain")
    private String domain;

    /**
     * <code>etag</code>.
     */
    @CacheResultMapper.JSONMapping("etag")
    private String etag;

    /**
     * <code>explicit_content</code>.
     */
    @CacheResultMapper.JSONMapping("explicit_content")
    private String[] explicitContent;

    /**
     * <code>implicit_context</code>.
     */
    @CacheResultMapper.JSONMapping("implicit_content")
    private String[] implicitContent;

    /**
     * <code>format</code>.
     */
    @CacheResultMapper.JSONMapping("format")
    private RepresentationFormat[] formats;

    /**
     * <code>label</code>.
     */
    @CacheResultMapper.JSONMapping("label")
    private String[] labels;

    /**
     * <code>length</code>.
     */
    @CacheResultMapper.JSONMapping("length")
    private long length;

    /**
     * <code>ontology</code>.
     */
    @CacheResultMapper.JSONMapping("ontology")
    private String[] ontologies;

    /**
     * <code>size</code>.
     */
    @CacheResultMapper.JSONMapping("size")
    private long size;

    /**
     * <code>timestamp</code>
     */
    @CacheResultMapper.JSONMapping("timestamp")
    private Date timestamp;

    protected CacheResult() {}

    /**
     * @return the url which uniquely identifies the indexed resource.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the content checksum.
     */
    @Deprecated
    public String getChecksum() {
        return checksum;
    }

    /**
     * @return the list of class URIs used in the document.
     */
    public String[] getClasses() {
        return classes;
    }

    /**
     * @return  the URI of the dataset to which the URL belongs.
     *          This field is normally only present for urls which
     *          have <code>data_source=DUMP</code>.
     */
    public String[] getDatasetURI() {
        return datasetURI;
    }

    /**
     * @return the means by which the data was obtained.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @return the website domain to which the URL belongs.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @return the <i>HTTP</i> tag for the URL, can be <code>null</code>.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * @return the list of RDF statements extracted for that URL.
     *         Each item of the list is a single statement in <i>N-Triples</i> format.
     */
    public String[] getExplicitContent() {
        return explicitContent;
    }

    /**
     * @return the list of RDF statements inferred for that URL.
     *         Each item of the list is a single statement in <i>N-Triples</i> format.
     */
    public String[] getImplicitContent() {
        return implicitContent;
    }

    /**
     * @return how the original semantic data was exposed.
     */
    public RepresentationFormat[] getFormats() {
        return formats;
    }

    /**
     * @return the list of labels (literals from <code>rdfs:label</code>)
     *         of the indexed resource.
     */
    public String[] getLabels() {
        return labels;
    }

    /**
     * @return the number of bytes in <code>explicit_context</code>.
     */
    public long getLength() {
        return length;
    }

    /**
     * @return the list of the urls of the ontologies imported by the statements.
     */
    public String[] getOntologies() {
        return ontologies;
    }

    /** 
     * @return The number of statements in <code>explicit_content</code>.
     */
    public long getSize() {
        return size;
    }

    /**
     * @return the date of indexing in the form: <code>yyyy-mm-ddThh:mm:ss.SSS</code> .
     */
    public Date getTimestamp() {
        return timestamp;
    }
}
