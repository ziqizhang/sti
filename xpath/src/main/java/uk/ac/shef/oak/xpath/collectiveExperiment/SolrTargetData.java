package uk.ac.shef.oak.xpath.collectiveExperiment;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.schema.CopyField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.xml.sax.SAXException;

import uk.ac.shef.dcs.oak.util.SysProp;


public class SolrTargetData implements TargetData{



	public SolrTargetData(String propertyFile) {
//		this.propertyFile=propertyFile;
		this.sp = new SysProp(propertyFile);
		this.solrPath= this.sp.getProperties().getProperty("solrInputIndexPath");	
		this.solrSemanticIndexPath= this.sp.getProperties().getProperty("solrSemanticIndexPath");	
		this.fetchsize = Integer.valueOf(this.sp.getProperties().getProperty("fetchsize"));
		System.setProperty("solr.solr.home", this.solrPath);
		
		this.initiSolrServers();
	}


	private static Logger l4j = Logger.getLogger("SolrTargetData");	
//	private String propertyFile;
	private SysProp sp;
	
	private static String SOLR_CORE_TO_WRITE = "tridsDocuments";
	private static final String TAG_FIELD = "user_tag_s";


	
	//solr attributes
	//sorl home for the index to semantify
	private String solrPath;
	//sorl reading server
	private EmbeddedSolrServer readingServer; 
	//sorl home for the semantified index to be generated
	private String solrSemanticIndexPath;
	//solr writing server
	private EmbeddedSolrServer writingServer; 
	//fields not to replicate
	private Set<String> copyFieldNames;
	//petching size
	private Integer fetchsize;

	
	
    //all tags
    private Set<String> allTagStrings;
    
    
	public Set<String> getAllTags() {
		return this.allTagStrings;
	}

	
	private void initiSolrServers(){
		l4j.info("Initializing reading embedded server");
		try {		
			//TODO sorl home has been set at constructor
			
		CoreContainer.Initializer topInitializer = new CoreContainer.Initializer();
		CoreContainer topCoreContainer = topInitializer.initialize();
		this.readingServer = new EmbeddedSolrServer(topCoreContainer, "");
		
//		File f = new File(this.solrPath,"solr.xml");
//		CoreContainer container = new CoreContainer();
//		container.load(this.solrPath,	f);
//		this.readingServer = new EmbeddedSolrServer(container, "");
		
		l4j.info("using embedded server "+readingServer.toString());
		

		//test reading sorl index
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set("q", "*");
			params.set("start", 0);
			params.set("rows", this.fetchsize);
			l4j.info("performing warm up query = " + params);
			QueryResponse response = readingServer.query(params);
			l4j.info("reading sorl index OK "+response);
		}catch (Exception e){
			l4j.error("exception with reading sorl index");
			e.printStackTrace();
		}
		

		
		//populate allTags
		
		this.allTagStrings = new HashSet<String>();
        int start =0;
        l4j.info("\t Getting tags to process...");
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", this.TAG_FIELD+":" + "*");
        params.set("start", start);
        //TODO limit fetch size to this.getFetchSizeTweetIndex() and iterate
        params.set("rows", this.fetchsize);

        //iteratively query the server until all documents containing the tag are retrieved
        try {
            QueryResponse response = this.readingServer.query(params);
            
            SolrDocumentList result = response.getResults();
            while (result.getNumFound() != 0) {
                //TODO limit fetch size to this.getFetchSizeTweetIndex() and iterate
                l4j.info("Fetching " + this.fetchsize+ " documents starting from " + start);

                //concatenate the content
                this.allTagStrings.addAll(this.getTags(result));

                if (result.size() >= this.fetchsize) {
                    start = start + this.fetchsize;
                    params.set("start", start);
                    params.set("rows", this.fetchsize);
                    result = this.readingServer.query(params).getResults();
                }
                else{
                    break;
                }
            }
            
            
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        l4j.info("\t  initialized "+ this.allTagStrings.size()+" tags to processes "+this.allTagStrings);
       
        
        // initialize writing server
        
		try {

			File f = new File(this.solrSemanticIndexPath,"solr.xml");
			CoreContainer container = new CoreContainer();
			container.load(this.solrSemanticIndexPath,	f);
			this.writingServer = new EmbeddedSolrServer(container, "");

			IndexSchema is = container.getCore(this.SOLR_CORE_TO_WRITE).getSchema();
//			IndexSchema is = container.getCores().iterator().next().getSchema();
			Map<String, SchemaField> allF = is.getFields();

			//populate copyfield
			this.copyFieldNames = new HashSet<String>();
			
			l4j.debug(" loading copyfields... ");

			
			for (SchemaField sf : allF.values()){
				List<CopyField> cfv = is.getCopyFieldsList(sf.getName() );
				l4j.debug(sf.toString() +" --> "+cfv.size());
				for (CopyField cf :cfv){
					copyFieldNames.add(cf.getDestination().getName());
				}
			}
			
			l4j.info("loaded copyfields that will not be duplicated during re-indexing "+copyFieldNames);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
    
    private List<String> getTags(SolrDocumentList result) {
        List<String> allTagStrings = new ArrayList<String>();
    	Iterator<SolrDocument> docs = result.iterator();
        while (docs.hasNext()) {
            SolrDocument d = docs.next();
            for(Object string: d.getFieldValues(TAG_FIELD)){
            	allTagStrings.add((String) string);
            }
        }
        return allTagStrings;
    }
    
    
    
    

	/** test method
	 * @param args
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException,
			ParserConfigurationException, SAXException {

		

		l4j.info("Process started");

		TargetData ss = new SolrTargetData(args[0]);
		System.exit(0);
	
		}


	public EmbeddedSolrServer getReadingServer() {
		return this.readingServer;
	}


	public EmbeddedSolrServer getWritingServer() {
		return this.writingServer;
	}


	public int getFetchSise() {
		return this.fetchsize;
	}


	public Set<String> getCopyFieldNames() {
		return this.copyFieldNames;
	}
	
}
