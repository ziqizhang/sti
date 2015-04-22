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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class models a single result
 * retrieved from <i>Sindice.com</i>
 * to a given {@link com.sindice.query.Query}.
 *
 * @author Milan Stankovic (milstan@gmail.com)
 */
public class SearchResult implements QueryResult {

	private int bytes;

	private List<String> formats = new ArrayList<String>();

	private String link;

	private int triples;

	private String title;

	private Date updated;

	/**
	 * Adds a format to the list of SearchResult's formats
	 * @param format the format to be added
	 * @return <code>true</code> if format
	 */
	public boolean addFormat(String format) {
		return formats.add(format);
	}

	/**
	 * bytes getter
	 * @return number of bytes
	 */
	public int getBytes() {
		return bytes;
	}

	/**
	 * formats getter
	 * @return list of formats of the SearchResult
	 */
	public List<String> getFormats() {
		return formats;
	}

	/**
	 * link getter
	 * @return link
	 */
	public String getLink() {
		return link;
	}

	
	/**
	 * triples getter
	 * @return number of triples
	 */
	public int getTriples() {
		return triples;
	}

	/**
	 * title getter
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	
	/**
	 * getter of the last update date
	 * @return date of last update
	 */
	public Date getUpdated() {
		return updated;
	}

	/**
	 * bytes setter
	 * @param bytes
	 */
	public void setBytes(int bytes) {
		this.bytes = bytes;
	}

	/**
	 * formats setter
	 * @param formats
	 */
	public void setFormats(List<String> formats) {
		this.formats = formats;
	}

	/**
	 * link setter
	 * @param link
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * triples setter
	 * @param triples
	 */
	public void setTiples(int triples) {
		this.triples = triples;
	}

	/**
	 * title setter
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * setter of date of last update
	 * @param updated
	 */
	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	/**
	 * setter updated date by String
	 * @param updated
	 * @throws ParseException
	 */
	public void setUpdated(String updated) throws ParseException {
		this.updated = new SimpleDateFormat("yyyy/MM/dd").parse(updated);
	}

	@Override
	public String toString() {
		return "SearchResult [bytes=" + bytes + ", formats=" + formats
				+ ", link=" + link + ", triples=" + triples + ", title=" + title
				+ ", updated=" + updated + "]";
	}
}
