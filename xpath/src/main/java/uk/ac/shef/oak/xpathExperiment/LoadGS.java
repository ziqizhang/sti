package uk.ac.shef.oak.xpathExperiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.shef.oak.gazetteers.TextOperations;

public class LoadGS {
	
	
	private static Logger l4j = Logger.getLogger("Gazeteer");

	//TODO load counts as well
	private  HashMap<String, Set<String>> values;

	public HashMap<String, Set<String>> getValues() {
		return this.values; 
	}

//	public StopWord() {
//		super();
//		this.words = new HashSet<String> () ;
//	}
	public LoadGS(String gsPath) {
		super();
		this.values = new HashMap<String, Set<String>> () ;
		try {
			this.loadFile(gsPath);
		} catch (IOException e) {
			l4j.error("error loading gs file");
			e.printStackTrace();
		}
	}
	
	
	private void loadFile(String gsPath) throws IOException{
		BufferedReader input=new BufferedReader(new FileReader(gsPath));
		String line;
		l4j.info("loading file "+gsPath);
		//skip the header
		//TODO  do smt with the header later
		line=input.readLine();
		line=input.readLine();
		line=input.readLine();

		while (line!=null)
		{
			line=line.trim();
			if (!line.equals("")){
				String [] v = line.split("\t");
				
				int results = 0;
				try{results=Integer.parseInt(v[1]);}catch(Exception e){
					l4j.error("parsing issue with "+line);
				}
				
				Set<String> values = new HashSet<String>();
				
				for (int i=0; i<results;i++){
					try{
						
						values.add(TextOperations.normalizeString(v[i+2]));

//					values.add(v[i+2].toLowerCase());
					}catch(Exception e){
						l4j.error("missing value in "+line);
						
					}
				}
			this.values.put(v[0].toLowerCase(), values);}
			
			line=input.readLine();
			
		}
		HashMap<String, Set<String>> r = this.values;
		
	}
	
	public Set<String> getAllDifferentAnnotations(){
		Set<String> ann = new HashSet<String>();
		
		for (Entry<String, Set<String>> s :this.values.entrySet()){
			for (String a :s.getValue())
				ann.add(a);

		}
		
		return ann;
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		LoadGS fileExt = new LoadGS("/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/groundtruth/book/book-amazon-title.txt");
//		LoadGS fileExt = new LoadGS("/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/groundtruth/book/book-borders-author.txt");
//		LoadGS fileExt = new LoadGS("/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/groundtruth/camera/camera-onsale-model.txt");
		LoadGS fileExt = new LoadGS("/Users/annalisa/Documents/CORPORAandDATASETS/swde-17477/groundtruth/nbaplayer/nbaplayer-usatoday-weight.txt");

		
//		for (String s :fileExt.getValues().values()){
//			System.out.println(s);
//		}
		
		
//		for (Entry<String, Set<String>> s :fileExt.getValues().entrySet()){
//			System.out.println(s.getKey() +" "+s.getValue().size());
//			if (s.getValue().size()>0)
//				System.out.println(s.getValue());
//
//		}
		
		System.out.println(fileExt.getAllDifferentAnnotations());
		/*
		
		HashTagEntropy ss = new HashTagEntropy(args[0]);

		
	File d = new File("/Users/annalisa/SOLR/simon-solr-3.4/data/index");
	if (d.isDirectory()) {
		Directory directory;
		try {
			directory = FSDirectory.open(d);
			List<String> tt = ss.computeTopTermOnGlobalIndex(directory);


			 System.out.println(tt);
			 
		     BufferedWriter outH = new BufferedWriter(new FileWriter("./out/stopword.txt"));

	        	  for (String t:tt) {
					outH.write(t);
					outH.newLine();
				}
			outH.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/

		
	}

	}
