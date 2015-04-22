package uk.ac.shef.dcs.oak.triplesearch.queryextension;

import java.util.Map;
import java.util.Set;

public class Keyword {

	private String keywordName;
	private Set<String> uris;
	private Map<String, String> prop_label;
	private Map<String, String> label_uri;
	public String getKeywordName() {
		return keywordName;
	}
	public void setKeywordName(String keywordName) {
		this.keywordName = keywordName;
	}
	public Set<String> getUris() {
		return uris;
	}
	public void setUris(Set<String> uris) {
		this.uris = uris;
	}
	public Map<String, String> getProp_label() {
		return prop_label;
	}
	public void setProp_label(Map<String, String> prop_label) {
		this.prop_label = prop_label;
	}
	public Map<String, String> getLabel_uri() {
		return label_uri;
	}
	public void setLabel_uri(Map<String, String> label_uri) {
		this.label_uri = label_uri;
	}
}
