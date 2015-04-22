//package uk.ac.shef.oak.xpath.collectiveExperiment;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.apache.log4j.Logger;
//import org.apache.lucene.index.CorruptIndexException;
//import org.apache.lucene.queryParser.ParseException;
//import org.apache.solr.client.solrj.response.QueryResponse;
//import org.apache.solr.client.solrj.util.ClientUtils;
//import org.apache.solr.common.SolrDocument;
//import org.apache.solr.common.SolrDocumentList;
//import org.apache.solr.common.SolrInputDocument;
//import org.apache.solr.common.params.ModifiableSolrParams;
//import org.xml.sax.SAXException;
//
//import uk.ac.shef.dcs.oak.util.SysProp;
//
///**
// * @author annalisa
// *
// */
//public class GenerateIndex {
//	
//	
//	
//	public GenerateIndex(String propertyFile) {
//		this.propertyFile = propertyFile;
//		this.sp = new SysProp(propertyFile);
//		
//		this.predicateLiteralISA=this.sp.getProperties().getProperty("predicateLiteralISA");
//		this.ontologyNameSpace=this.sp.getProperties().getProperty("ontologyNameSpace");
//		this.ontologyURL=this.sp.getProperties().getProperty("ontologyURL");
//			
//		
//
//		this.init();
//		
//	
//	}
//
//	
//	private void init(){
//		l4j.info("using embedded server "+this.td.getReadingServer().toString());
//		this.allTagStrings = this.td.getAllTags();
//		l4j.info("tag to semantify "+allTagStrings.size()+" --> "+ allTagStrings);
//		
//		this.ti = new LuceneTagInventoryReader(this.propertyFile, this.allTagStrings);
//	
//		//generate baseline using TagInventory object
//		try {
//			//TODO check null
//			if (allTagStrings!=null){
//			baseline = this.ti.getBaseline(allTagStrings);}
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//		
//		l4j.info(baseline);
//		
//		//populate distributional model for tag
//		this.tdm = new LuceneTagDistributionalModelReader(this.propertyFile, this.allTagStrings);
//		
//		
//		//generate overlap using TagInventory object and 
//		try {
//			
//			//TODO only consider existing tags
//			this.overlap = this.getBasicOverlapConcept(this.allTagStrings);
//
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//		
//		l4j.info(overlap);
//		
//	}
//	
//
//	//******************************************* class attributes
//	//property managment
//	private String propertyFile;
//	private SysProp sp;
//	
//	//logging managment
//	private static Logger l4j = Logger.getLogger("ReIndexingWithTags");
//
//	//properties needed for dbpedia retrieval
//	private String predicateLiteralISA;
//	private String ontologyNameSpace;
//	private String ontologyURL;
//	
//
//	
//	//solr index to semantify
//	private SolrTargetData td;
//	
//	
//	private Set<String> allTagStrings;
//	private Map<String, String>  baseline;
//	private Map<String, Map<String, Float>>  overlap;
//
//
//	//******************************************* getters and setters
//
//
//	public String getPropertyFile() {
//		return propertyFile;
//	}
//
//
//
//	public void setPropertyFile(String propertyFile) {
//		this.propertyFile = propertyFile;
//	}
//
//
//
//	public SysProp getSp() {
//		return sp;
//	}
//
//
//
//	public void setSp(SysProp sp) {
//		this.sp = sp;
//	}
//
//
//
//	public static Logger getL4j() {
//		return l4j;
//	}
//
//
//
//	public static void setL4j(Logger l4j) {
//		GenerateIndex.l4j = l4j;
//	}
//
//
//
//	public DBPediaResourceClassExtractor getDrce() {
//		return drce;
//	}
//
//
//
//	public void setDrce(DBPediaResourceClassExtractor drce) {
//		this.drce = drce;
//	}
//
//
//
//	public String getPredicateLiteralISA() {
//		return predicateLiteralISA;
//	}
//
//
//
//	public void setPredicateLiteralISA(String predicateLiteralISA) {
//		this.predicateLiteralISA = predicateLiteralISA;
//	}
//
//
//
//	public String getOntologyNameSpace() {
//		return ontologyNameSpace;
//	}
//
//
//
//	public void setOntologyNameSpace(String ontologyNameSpace) {
//		this.ontologyNameSpace = ontologyNameSpace;
//	}
//
//
//
//	public String getOntologyURL() {
//		return ontologyURL;
//	}
//
//
//
//	public void setOntologyURL(String ontologyURL) {
//		this.ontologyURL = ontologyURL;
//	}
//
//
//
//	public TagInventory getTi() {
//		return ti;
//	}
//
//
//
//	public TagDistributionalModel getTdm() {
//		return tdm;
//	}
//
//
//
//
//	public TargetData getTd() {
//		return td;
//	}
//
//	
//
//	
//	//******************************************* class methods
//
//	 public void generateAnnotationIndex(String pathToGazeteer) throws IOException, ParserConfigurationException, SAXException{
//
//
//			try {
//
//				ModifiableSolrParams params = new ModifiableSolrParams();
//				params.set("q", "*");
//				params.set("start", 0);
//				params.set("rows", this.td.getFetchSise());
//
//
////query to get tog tags
//				l4j.info("performing query = " + params);
//				QueryResponse response = this.td.getReadingServer().query(params);
//				l4j.info("response loaded");
//				
////				
////	            SolrDocumentList docs = response.getResults();
////	            Iterator<SolrDocument> itAllDocs = docs.iterator();
//
//		            
//				long offset = 0;
//				long totalResults;
//				if (limit >=0){
//					totalResults = limit;
//				}else{
//					totalResults = response.getResults().getNumFound();
//				}
//
////				}
////				
//
//				int multiplePathTags=0;
//				int tweetsWithTag=0;
//				int tweetsWithoutTag=0;
//				int resolvedTags=0;
//
//				while (offset < totalResults) {
//					Collection<SolrInputDocument> docColl = new ArrayList<SolrInputDocument>();
//
//					l4j.debug("fetching " + this.td.getFetchSise()+" documents starting from "+offset);
//
//					params.set("start", (int) offset);
//					params.set("rows", this.td.getFetchSise());
//
//					SolrDocumentList res = this.td.getReadingServer().query(params).getResults();
//					l4j.debug("converting " + res.size()+" documents to SolrInputDocument");
//
//
//
//
//					for (SolrDocument doc : res) {
//						
//						for (String cf :this.td.getCopyFieldNames()){
//							doc.removeFields(cf);
//						}
//						
//						//TODO this is a quick patch to replicate localisation_ll which is indexed but not stored 
//						
//				        Double latitude = (Double) doc.getFieldValue("latitude_td");
//				        Double longitude = (Double) doc.getFieldValue("longitude_td");
//
//				        if (latitude!=null&longitude!=null){
//							doc.removeFields("localisation_ll");
//							doc.removeFields("localisation_s");
//
//				        }
//						
//
//				        SolrInputDocument semDoc = ClientUtils.toSolrInputDocument(doc);
//						
//						//TODO this is part of the quick patch to replicate localisation_ll which is indexed but not stored 
//				        if (latitude!=null&longitude!=null){
//				            String latlng = latitude + "," + longitude;
//				            semDoc.addField("localisation_ll", latlng);
//				            semDoc.addField("localisation_s", latlng); 
//
//				        }
//						
//						
//						//TODO remove hard-coded field names everywhere in this method
//						//this bit generates the semantic concepts staring from the tags in the document
//				        Collection<Object> tags= doc.getFieldValues("user_tag_s");
//
//				        if(tags!=null){
//				        	tweetsWithTag++;
//				        for (Object tag:tags){
//							String wiki ="";
//				        	String tag_s = tag.toString();
////							String wiki = baseline.get(tag_s);
//							Float concepts_score=(float)0;
//							try{
//							for (String urls: this.overlap.get(tag_s).keySet()){
//								if (!urls.equals("")&urls!=null){
//									wiki=urls;
//									concepts_score=this.overlap.get(tag_s).get(urls);
//								//break as the map only contains 1 entry
//									break;}
//							}}catch(Exception e){
//								try {
//									wiki = this.baseline.get(tag_s);
//								} catch (Exception e1) {
//									l4j.error("error in overlap method for tag "+tag_s+", also baseline "+wiki);
//									e1.printStackTrace();
//								}
//								l4j.error("error in overlap method for tag "+tag_s+", using baseline "+wiki);
//
//							}
//							
//
//						if (wiki!=null){
//							if(!wiki.equals("")){
//
//							resolvedTags++;
//						l4j.debug("loading categories for concept "+wiki);
//						String concepts_s = DBPediaQueryService.toDBPediaResourceName(wiki);
//						if (concepts_s!=null){
//						semDoc.addField("concepts_s", concepts_s);
//						//TODO DEBUG FOR TYPE FLOAT
//						semDoc.addField("concepts_score_f", concepts_score);
//
//						
//						List<String> flat_category_s;
//						List<TreePath> category_path_s;
//						if (!dbpediaCatPath.containsKey(concepts_s)){
////							String cs = URLEncoder.encode(concepts_s).toString();
//				        String result = DBPediaQueryService.fetchDBPediaResource(concepts_s);
//						category_path_s = this.drce.extractClassesTreePaths(result, this.predicateLiteralISA, this.ontologyNameSpace, this.ontologyURL);
////						category_path_s = generateShrotPathWithDilimiter(category_path_s);
//						//this will include as leaves concepts_s and tag_s
//						dbpediaCatPath.put(concepts_s, category_path_s);
//						
//						flat_category_s = this.drce.extractClassesFlat(result, this.predicateLiteralISA, this.ontologyNameSpace);
//						
//						dbpediaCatFlat.put(concepts_s, flat_category_s);
//
//						}else {
//							category_path_s = dbpediaCatPath.get(concepts_s);
//							flat_category_s = dbpediaCatFlat.get(concepts_s);
//						}
//						
//						if (category_path_s.size()>1){
//							multiplePathTags++;
//							l4j.info("tag:" + tag_s+" concept: "+concepts_s+" number of path: "+category_path_s.size());
//
//						}
//							
//						for (TreePath cps:category_path_s){
//							String path_string= generateShrotPathWithDilimiter(cps);
//
//							path_string = path_string+"/"+concepts_s+"/"+tag_s;
////							path_string = path_string+"/"+concepts_s;
//
//						semDoc.addField("category_path_s", path_string);
//						}
//						
//						for (String fcs:flat_category_s)
//						semDoc.addField("flat_category_s", fcs);
//						
//						
////						Date date = null;
//						//TODO add time slot
////					       Date dateHour = new Date(date .getTime() - (date.getTime() % 3600000));
//						}else{
//							l4j.error("Error getting wikipedia resource for page "+wiki);
//						}
//						
//						}
//				        }
//				        }}else{tweetsWithoutTag++;}
//				        
//				        
//				        
//				        
//						docColl.add(semDoc);
//					}
//					
//					l4j.info("Progressive countings: tweets without tags "+tweetsWithoutTag+ " tweets with tags "+ tweetsWithTag+ " resolved tags "+ resolvedTags+" tags with multiple paths "+multiplePathTags);
//					l4j.warn(offset+"-"+(offset+this.td.getFetchSise())+", "+tweetsWithoutTag+ ", "+ tweetsWithTag+ ", "+ resolvedTags+", "+multiplePathTags);
//
//					this.td.getWritingServer().add(docColl);
////					l4j.debug("added "+(offset+this.getFetchsize()) + " documents of "+totalResults);
//
//					this.td.getWritingServer().commit();
//					offset += this.td.getFetchSise();
//					l4j.info("committed "+offset + " documents of "+totalResults);
//
//				}
//				
//				l4j.warn("Final stats: tweets without tags "+tweetsWithoutTag+ " tweets with tags "+ tweetsWithTag+ " resolved tags "+ resolvedTags+" tags with multiple paths "+multiplePathTags);
//				l4j.info("Final stats: tweets without tags "+tweetsWithoutTag+ " tweets with tags "+ tweetsWithTag+ " resolved tags "+ resolvedTags+" tags with multiple paths "+multiplePathTags);
//
//				l4j.info("Generated new index");
//				this.td.getWritingServer().optimize();
//		            
//			} catch (Exception e) {
//				l4j.info("Exception courred, index may be incomplete");
//				e.printStackTrace();
//			}
//
//	 }
//	 
//	
//	 
//	 
//		public Map<String,Map<String,Float>> getBasicOverlapConcept(Set<String > tag2Semantify) throws CorruptIndexException, IOException, ParseException{
//
//			Map<String,Map<String,Float>> results = new HashMap<String,Map<String,Float>>();
//	    	int count =0;
//	    	
//
//			for (String tag: tag2Semantify) {
//				if (this.tdm.getAllTags().contains(tag)){
//					//get keyword from distributional model
//				List<String> context = this.tdm.getTokenizedText(tag);
//				Set<String> dataKeywords = new HashSet<String>(context);
//				l4j.info(count+" - "+tag+" keywords in data = "+dataKeywords);
//
//
//					List<String> candidates = this.ti.getCandidatesIds(tag);
//				
//					String concept = "";
//					int max = 0;
//					float score =0;
//					if (candidates!=null){	
//					for (String url : candidates){
//						
//						Set<String> urlKeywords =  new HashSet<String>(this.ti.getCandidateTokenizedDescription(url));
//						l4j.info(url+" keywords in candidate = "+urlKeywords);
//
//						 Set<String> inters = SetOperations.intersection(dataKeywords, urlKeywords); 
//						l4j.info("intersection "+inters+" size "+inters.size()+"/"+SetOperations.union(dataKeywords, urlKeywords).size()+" =  "+score);
//
//						if (inters.size()>max){
//							max = inters.size();
//							concept = url;
//							score = (float)inters.size()/(float)SetOperations.union(dataKeywords, urlKeywords).size();
//
//						}
//							
//					}}
//
//					Map<String,Float>res = new HashMap<String,Float>();
//					res.put(concept, score);
//					results.put(tag, res);
//					count++;
//			}else{
//				l4j.debug("tag " + tag + "does not have any option in candidate index");
//			}
//				
//			}	
//			return results;
//		}
//		
//		
//		
//		
//		private String generateShrotPathWithDilimiter(
//				TreePath cps) {
//
//			String path = "";
//			
//			String ns = cps.getOntologyNS();
//			for (Concept c :cps.getPaths()){
//				if (!path.equals(""))
//					path = path+"/"; 
//				path = path+ (c.getLabel().substring(ns.length()));
//			}
//			return path;
//		}
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		l4j.warn("Process started, progressive counting will be output as warnings");
//		l4j.warn("Tweets from-to, Tweets without tags, Tweets with tags, resolved tags, tags with multiple paths");
//
//		GenerateIndex ss = new GenerateIndex(args[0]);
//		try {
//			ss.generateSemantifiedIndex(-1); 
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.exit(0);
//	}
//
//}
