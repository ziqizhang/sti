package uk.ac.shef.oak.xpath.collectiveExperiment;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.CoreContainer;
import org.xml.sax.SAXException;

import uk.ac.shef.dcs.oak.util.SysProp;

public class DomainMulticoreIndex {
	SysProp sp;
	
	String solrPath;
	String solrMulticorePath;

	public String getSolrMulticorePath() {
		return solrMulticorePath;
	}

	public void setSolrMulticorePath(String solrMulticorePath) {
		this.solrMulticorePath = solrMulticorePath;
	}




	private static Logger l4j = Logger.getLogger("PerTagMulticoreIndex");
//	String solrHome;
//	public String getSolrHome() {
//		return solrHome;
//	}
//
//	public void setSolrHome(String solrHome) {
//		this.solrHome = solrHome;
//	}

	int topTagNumber;
	int fetchsize;

	public int getFetchsize() {
		return fetchsize;
	}

	public void setFetchsize(int fetchsize) {
		this.fetchsize = fetchsize;
	}

	public int getTopTagNumber() {
		return topTagNumber;
	}

	public void setTopTagNumber(int topTagNumber) {
		this.topTagNumber = topTagNumber;
	}

	public String getSolrPath() {
		return solrPath;
	}

	public void setSolrPath(String solrPath) {
		this.solrPath = solrPath;
	}

	public DomainMulticoreIndex(String string) {

		this.sp = new SysProp(string);
		this.solrPath= this.sp.getProperties().getProperty("solrPath");	
		this.solrMulticorePath= this.sp.getProperties().getProperty("multicore");	

		this.topTagNumber = Integer.valueOf(this.sp.getProperties().getProperty("topTagNumber"));
		this.fetchsize = Integer.valueOf(this.sp.getProperties().getProperty("fetchsize"));
		System.setProperty("solr.solr.home", this.getSolrPath());

	}

 
	public Set <String> domains = new HashSet<String> ();


	/**
	 * @param args
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException,
			ParserConfigurationException, SAXException {

		
		DomainMulticoreIndex ss = new DomainMulticoreIndex(args[0]);
		ss.generateDomainIndexes();
		System.exit(0);
		
		}

	 public void generateDomainIndexes() throws IOException, ParserConfigurationException, SAXException{

		 //TODO list domain from folder
		 this.domains.add("a");
		 this.domains.add("b");
		 this.domains.add("c");


			l4j.info("Generating indexes");
			
//			CoreContainer.Initializer topInitializer = new CoreContainer.Initializer();
//			try{
//			CoreContainer topCoreContainer = topInitializer.initialize();
//			EmbeddedSolrServer topServer = new EmbeddedSolrServer(topCoreContainer, "");
//			l4j.info("using embedded server "+topServer.toString());
//			}catch (Exception e){
//				e.printStackTrace();
//			}
			
			
			try {

		            //TODO iterate around all documents in test set
		            for(String d:domains){

		                File solrMulticore = new File (this.getSolrMulticorePath());
		                File multi_tag = new File (solrMulticore.getAbsolutePath()+"/"+d);
 
							if (!(multi_tag.exists())) {

								//create the core
								multi_tag.mkdirs();
								FileUtil.copyDirectory(this.getSolrPath()
										+ "/conf", multi_tag.getAbsolutePath()
										+ "/conf");
								FileUtil.addCoreToXml(
										solrMulticore.getAbsolutePath()
												+ "/solr.xml", d);
								
								l4j.info("INDEX " + d
										+ " initialized");
								
								
								//server for querying the single hashtag
								CoreContainer.Initializer initializer = new CoreContainer.Initializer();
								CoreContainer coreContainer = initializer.initialize();
								EmbeddedSolrServer server = new EmbeddedSolrServer(coreContainer, "");
								l4j.info("using embedded server "+server.toString());
								
								File f = new File(solrMulticore, "solr.xml");
								
								
								CoreContainer container = new CoreContainer();
								container.load(solrMulticore.getAbsolutePath(),	f);
								container.getCore(d);
								EmbeddedSolrServer server_writing = new EmbeddedSolrServer(container.getCore(d));

								//TODO loop for each website

									Collection<SolrInputDocument> docColl = new ArrayList<SolrInputDocument>();

									SolrDocument doc = new SolrDocument();

										docColl.add(ClientUtils
												.toSolrInputDocument(doc));

									server_writing.add(docColl);
//									l4j.info("added "+(offset+this.getFetchsize()) + " documents of "+totalResults);

									server_writing.commit();
									
								l4j.info("INDEX " + d
										+ " completed");
							}else{
								l4j.info("INDEX " + d
										+ " already exist");
							}
					
		            }
		            
					l4j.info("indexing completed for "+this.getTopTagNumber()+" hastags ");
		            
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			l4j.info("Generated "+this.getTopTagNumber()+" TopTagIndexes ");

	 }


}
