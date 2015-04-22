package uk.ac.shef.oak.xpath.collectiveExperiment;


import java.util.Set;

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;

public interface TargetData {

	Set<String> getAllTags();
	
	EmbeddedSolrServer getReadingServer(); 

	EmbeddedSolrServer getWritingServer();


}
